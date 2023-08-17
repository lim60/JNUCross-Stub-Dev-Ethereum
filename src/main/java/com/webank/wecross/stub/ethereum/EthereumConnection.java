package com.webank.wecross.stub.ethereum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.ethereum.common.EthereumType;
import com.webank.wecross.stub.ethereum.contract.Contract;
import com.webank.wecross.stub.ethereum.protpcol.TransactionParams;
import com.webank.wecross.stub.ethereum.utils.BlockUtils;
import com.webank.wecross.stub.ethereum.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import static com.webank.wecross.stub.ethereum.common.Config.ADMIN;
import static com.webank.wecross.stub.ethereum.common.EthereumConstants.*;
import static com.webank.wecross.stub.ethereum.common.EthereumType.CHAIN_ID;
import static com.webank.wecross.stub.ethereum.common.EthereumType.StatusCode.*;


/**
 * sdk调用，与区块链发生真实的交互
 * 必须的接口
 */
public class EthereumConnection implements Connection {
    private List<ResourceInfo> resourcesCache = null;
    private static final Logger logger = LoggerFactory.getLogger(EthereumConnection.class);
    private List<ResourceInfo> resourceInfoList;
    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private ConnectionEventHandler eventHandler = null;
    private Web3j web3j = null;
    private final ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpEthConn"));

    public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
        this.resourceInfoList = resourceInfoList;
    }

    public List<ResourceInfo> getResourceInfoList() {
        return resourceInfoList;
    }

    @Override
    public void asyncSend(Request request, Callback callback) {
        logger.debug("in geth connection asyncSend");
        if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_SEND_TRANSACTION) {
            handleAsyncTransactionRequest(request, callback);
        }
        else if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_NUMBER) {
            handleAsyncGetBlockNumberRequest(request, callback);
        }
        else if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_BY_NUMBER) {
            handleAsyncGetBlockByNumberRequest(request, callback);
        }
        else if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_TRANSACTION) {
            handleAsyncGetTransactionRequest(request, callback);
        } else if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_CALL) {
            handleAsyncCallRequest(request, callback);
        } else {
            // Does not support asynchronous operation, async to sync
            logger.warn(" unrecognized request type, type: {}", request.getType());
            Response response = new Response();
            response.setErrorCode(EthereumType.StatusCode.UnrecognizedRequestType);
            response.setErrorMessage(
                    EthereumType.StatusCode.getStatusMessage(EthereumType.StatusCode.UnrecognizedRequestType)
                            + " ,type: "
                            + request.getType());
            callback.onResponse(response);
        }
    }

    public void handleAsyncTransactionRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            TransactionParams transaction =
                    objectMapper.readValue(request.getData(), TransactionParams.class);
            if (logger.isDebugEnabled()) {
                logger.debug(
                        " from: {}, to: {}, tx: {} ",
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getData());
            }
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    ADMIN.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = new BigInteger("900000");
            //生成RawTransaction交易对象
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, transaction.getTo(), new BigInteger("1"), transaction.getData());//可以额外带数据
            //使用Credentials对象对RawTransaction对象进行签名
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, CHAIN_ID, ADMIN);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            if (Objects.isNull(ethSendTransaction.getTransactionHash()) || "".equals(ethSendTransaction.getTransactionHash()) || new BigInteger(
                    ethSendTransaction.getTransactionHash()
                            .substring(2), 16)
                    .compareTo(BigInteger.ZERO) == 0) {
                response.setErrorCode(TransactionReceiptNotExist);
                response.setErrorMessage("错误的交易hash");
            } else {
                try {
                    response.setErrorCode(Success);
                    response.setErrorMessage("发送成功");
                    response.setData(objectMapper.writeValueAsBytes(ethSendTransaction));
                } catch (JsonProcessingException e) {
                    logger.error(" e:", e);
                    response.setErrorCode(HandleSendTransactionFailed);
                    response.setErrorMessage(e.getMessage());
                }
            }
            callback.onResponse(response);
            // trigger resources sync after cns updated
            if (transaction.getTransactionRequest() != null
                    && (transaction
                    .getTransactionRequest()
                    .getMethod()
                    .equals(PROXY_METHOD_DEPLOY)
                    || transaction
                    .getTransactionRequest()
                    .getMethod()
                    .equals(PPROXY_METHOD_REGISTER))) {
                scheduledExecutorService.schedule(
                        this::noteOnResourcesChange, 1, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            logger.error("handleAsyncTransaction exception:", e);
            response.setErrorCode(HandleSendTransactionFailed);
            response.setErrorMessage(e.getMessage());
            callback.onResponse(response);
        }
    }

    private void noteOnResourcesChange() {
        synchronized (this) {
            List<ResourceInfo> resources = getResources();
            if (!resources.equals(resourcesCache) && !resources.isEmpty()) {
                resourcesCache = resources;
                if (logger.isDebugEnabled()) {
                    logger.debug(" resources notify, resources: {}", resources);
                }
            }
        }
    }

    private void handleAsyncGetBlockNumberRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            logger.debug("&&&&&&&&&&&&&\n In handleAsyncGetBlockNumberRequest");
            logger.debug(" blockNumber: {}", blockNumber);
            response.setErrorCode(Success);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(Success));
            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);
        } catch (Exception e) {
            logger.warn("handleGetBlockNumberRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    private void handleAsyncGetTransactionRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            logger.debug("in handleAsyncGetTransactionRequest");
            Map<Object, Object> map = request.getResourceInfo().getProperties();
            Long blockNumber = (Long) map.get("blockNumber");
            String transactionHash = (String) map.get("transactionHash");
            logger.debug("transactionHash: {}", transactionHash);
            boolean isVerified = (boolean) map.get("isVerified");
            Transaction transaction = web3j.ethGetTransactionByHash(transactionHash).send().getTransaction().get();
            logger.debug("transaction: {}", transaction);
            response.setErrorCode(Success);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(Success));
            ObjectMapper objectMapper = new ObjectMapper();
            com.webank.wecross.stub.Transaction weCrossTransaction = TransactionUtils.covertToTransaction(transaction);
            response.setData(objectMapper.writeValueAsBytes(weCrossTransaction));
            logger.debug("get transaction by transactionHash : {}, transaction : {}", transactionHash, transaction.getRaw());
        } catch (Exception e) {
            logger.warn("handleAsyncGetTransactionRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    private void handleAsyncGetBlockByNumberRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            logger.debug("in handleAsyncGetBlockByNumberRequest");
            logger.debug("request : {}", request);
            logger.debug("request.getResourceInfo() : {}", request.getResourceInfo());
            logger.debug("request.getData() : {}", request.getData());
            BigInteger blockNumber = BigInteger.valueOf((Long) request.getResourceInfo().getProperties().get("blockNumber"));
            logger.debug("blockNumber = {}", blockNumber);
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
            response.setErrorCode(Success);
            logger.debug("block = {}", block);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(Success));
            ObjectMapper objectMapper = new ObjectMapper();
            Block weCrossBlock = BlockUtils.covertToBlock(block);
            logger.debug("weCrossBlock = {}", weCrossBlock);
            response.setData(objectMapper.writeValueAsBytes(weCrossBlock));
            logger.debug("get block by number : {}, block : {}", blockNumber, block);
        } catch (Exception e) {
            //logger.warn("###"+request.toString());
            logger.warn("handleAsyncGetBlockByNumberRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    public void handleAsyncCallRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            TransactionParams transaction =
                    objectMapper.readValue(request.getData(), TransactionParams.class);
            org.web3j.protocol.core.methods.request.Transaction transaction1 =
                    org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                            transaction.getFrom(), transaction.getTo(), transaction.getData()
                    );
            EthCall callOutput = web3j.ethCall(transaction1, DefaultBlockParameterName.LATEST).sendAsync().get();

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " accountAddress: {}, contractAddress: {}, data: {}, output: {}",
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getData(),
                        callOutput.getValue());
            }
            response.setErrorCode(callOutput.getError().getCode());
            response.setErrorMessage(String.valueOf(Success));
            response.setData(objectMapper.writeValueAsBytes(callOutput));
        } catch (Exception e) {
            logger.warn("handleCallRequest Exception:", e);
            response.setErrorCode(HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    //todo:test
    public List<ResourceInfo> getResources() {
        List<ResourceInfo> resources =
                new ArrayList<ResourceInfo>() {
                    {
                        addAll(resourceInfoList);
                    }
                };
        String[] paths = listPaths();
        logger.debug("EthereumConnection getResources(), paths: {}", paths);
        if (Objects.nonNull(paths)) {
            for (String path : paths) {
                logger.debug("EthereumConnection getResources(), path: {}", path);
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setStubType(properties.get(GETH_STUB_TYPE));
                resourceInfo.setName(path.split("\\.")[2]);
                Map<Object, Object> resourceProperties = new HashMap<>();
                resourceProperties.put(
                        GETH_GROUP_ID, properties.get(GETH_GROUP_ID));
                resourceProperties.put(
                        GETH_CHAIN_ID, properties.get(GETH_CHAIN_ID));
                resourceInfo.setProperties(resourceProperties);
                resources.add(resourceInfo);
            }
        }
        return resources;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public void setProperty(Map<String, String> properties) {
        this.properties = properties;
    }

    private Map<String, String> properties = new HashMap<>();

    /**
     * list paths stored in proxy contract
     */
    public String[] listPaths() {
        try {
            logger.debug(" in listPaths listPaths listPaths ");
            List paths = Contract.proxy.getPaths().sendAsync().get();
            logger.debug(" listPaths path : {}", paths);
            if (Objects.nonNull(paths) && paths.size() != 0) {
                Set<String> set = new LinkedHashSet<>();
                for (int i = paths.size() - 1; i >= 0; i--) {
                    set.add(String.valueOf(paths.get(i)));
                }
                set.add("a.b." + GETH_PROXY_NAME);
                set.add("a.b." + GETH_HUB_NAME);
                return set.toArray(new String[0]);
            } else {
                Set<String> set = new LinkedHashSet<>();
                set.add("a.b." + GETH_PROXY_NAME);
                set.add("a.b." + GETH_HUB_NAME);
                logger.debug("No path found and add system resources");
                return set.toArray(new String[0]);
            }
        } catch (Exception e) {
            logger.warn(" listPaths failed,", e);
            return null;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    Web3j getWeb3j() {
        return this.web3j;
    }
}
