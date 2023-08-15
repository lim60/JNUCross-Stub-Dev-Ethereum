package com.webank.wecross.stub.ethereum;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.ethereum.account.EthereumAccount;
import com.webank.wecross.stub.ethereum.common.*;
import com.webank.wecross.stub.ethereum.protpcol.CallOutput;
import com.webank.wecross.stub.ethereum.protpcol.RevertMessage;
import com.webank.wecross.stub.ethereum.protpcol.TransactionParams;
import com.webank.wecross.stub.ethereum.service.AsyncCnsService;
import com.webank.wecross.stub.ethereum.utils.BlockUtils;
import com.webank.wecross.stub.ethereum.utils.FunctionUtility;
import com.webank.wecross.stub.ethereum.utils.TransactionUtils;
import com.webank.wecross.stub.ethereum.wrapper.ABIObjectFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.*;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import static com.webank.wecross.stub.ethereum.common.Config.ADMIN;
import static com.webank.wecross.stub.ethereum.common.EthereumConstants.*;
import static com.webank.wecross.stub.ethereum.common.EthereumType.ConnectionMessage.ETHEREUM_CALL;
import static com.webank.wecross.stub.ethereum.common.EthereumType.ConnectionMessage.ETHEREUM_SEND_TRANSACTION;
import static com.webank.wecross.stub.ethereum.common.EthereumType.StatusCode.*;
import static com.webank.wecross.stub.ethereum.utils.BlockUtils.loadABI;
import static com.webank.wecross.stub.ethereum.utils.BlockUtils.objectMapper;

/**
 * 区块链上信息的编码解码，stub的接口，connection的上层
 * 必须的接口
 */
public class EthereumDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(EthereumDriver.class);
    public static final BigInteger ONE = BigInteger.valueOf(1);
    public static final BigInteger EIGHT = BigInteger.valueOf(8);
    private AsyncCnsService asyncCnsService = null;

    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        int requestType = request.getType();
        /* check if transaction request */
        if ((requestType != ETHEREUM_CALL)
                && (requestType != ETHEREUM_SEND_TRANSACTION)) {
            return new ImmutablePair<>(false, null);
        }
        try {
            TransactionParams transactionParams = objectMapper.readValue(request.getData(), TransactionParams.class);
            TransactionRequest plainRequest = transactionParams.getTransactionRequest();
            return new ImmutablePair<>(true, plainRequest);
        } catch (Exception e) {
            logger.error("decodeTransactionRequest error: ", e);
            return new ImmutablePair<>(true, null);
        }
    }

    public AsyncCnsService getAsyncCnsService() {
        return asyncCnsService;
    }

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        if (connection instanceof EthereumConnection) {
            return ((EthereumConnection) connection).getResources();
        }
        logger.error(" Not ETH connection, connection name: {}", connection.getClass().getName());
        return new ArrayList<>();
    }

    @Override
    public void asyncCall(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {
        asyncCallByProxy(context, request, connection, callback);
    }

    @Override
    public void asyncSendTransaction(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {
        asyncSendTransactionByProxy(context, request, connection, callback);
    }


    /**
     * @param context
     * @param request
     * @param connection
     * @param callback
     */
    private void asyncSendTransactionByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {
        TransactionResponse transactionResponse = new TransactionResponse();
        try {
            Map<String, String> properties = connection.getProperties();
            // input validation
            //checkTransactionRequest(context, request);
            checkProperties(properties);
            // contractAddress
            String contractAddress = properties.get(GETH_PROXY_NAME);
            // groupId
            int groupId = Integer.parseInt(properties.get(GETH_GROUP_ID));
            // chainId
            int chainId = Integer.parseInt(properties.get(GETH_CHAIN_ID));
            // bcos node version
            String nodeVersion = properties.get(GETH_NODE_VERSION);
            context.getBlockManager()
                    .asyncGetBlockNumber(
                            (blockNumberException, blockNumber) -> {
                                if (Objects.nonNull(blockNumberException)) {
                                    callback.onTransactionResponse(
                                            new TransactionException(
                                                    HandleGetBlockNumberFailed,
                                                    blockNumberException.getMessage()),
                                            null);
                                    return;
                                }
                                // BCOSAccount to get credentials to sign the transaction
                                EthereumAccount bcosAccount = (EthereumAccount) context.getAccount();
                                Path path = context.getPath();
                                String name = path.getResource();
                                // query abi
                                asyncCnsService.queryABI(
                                        name,
                                        this,
                                        connection,
                                        (queryABIException, abi) -> {
                                            try {
                                                if (Objects.nonNull(queryABIException)) {
                                                    throw new EthStubException(
                                                            ABINotExist,
                                                            queryABIException.getMessage());
                                                }
                                                if (abi == null) {
                                                    throw new EthStubException(
                                                            ABINotExist,
                                                            "resource:" + name + " not exist");
                                                }
                                                // encode
                                                String[] args = request.getArgs();
                                                String method = request.getMethod();
                                                ContractABIDefinition contractABIDefinition =
                                                        loadABI(abi);
                                                List<AbiDefinition> functions =
                                                        contractABIDefinition
                                                                .getFunctions()
                                                                .get(method);
                                                if (Objects.isNull(functions)
                                                        || functions.isEmpty()) {
                                                    throw new EthStubException(
                                                            MethodNotExist,
                                                            "Method not found in abi, method: "
                                                                    + method);
                                                }
                                                ABIObject inputObj =
                                                        ABIObjectFactory.createInputObject(
                                                                functions.get(0));
                                                String encodedArgs = "";
                                                if (!Objects.isNull(args)) {
                                                    ABIObject encodedObj =
                                                            BlockUtils.encode(
                                                                    inputObj, Arrays.asList(args));
                                                    encodedArgs = encodedObj.encode();
                                                }
                                                String uniqueID =
                                                        (String)
                                                                request.getOptions()
                                                                        .get(
                                                                                StubConstant
                                                                                        .TRANSACTION_UNIQUE_ID);
                                                String uid =
                                                        Objects.nonNull(uniqueID)
                                                                ? uniqueID
                                                                : UUID.randomUUID()
                                                                .toString()
                                                                .replaceAll("-", "");
                                                String transactionID =
                                                        (String)
                                                                request.getOptions()
                                                                        .get(
                                                                                StubConstant
                                                                                        .XA_TRANSACTION_ID);
                                                Long transactionSeq =
                                                        (Long)
                                                                request.getOptions()
                                                                        .get(
                                                                                StubConstant
                                                                                        .XA_TRANSACTION_SEQ);
                                                Long seq =
                                                        Objects.isNull(transactionSeq)
                                                                ? 0
                                                                : transactionSeq;
                                                Function function;
                                                if (Objects.isNull(transactionID)
                                                        || transactionID.isEmpty()
                                                        || "0".equals(transactionID)) {
                                                    function =
                                                            FunctionUtility
                                                                    .newSendTransactionProxyFunction(
                                                                            uid,
                                                                            path.getResource(),
                                                                            ContractABIDefinition.getMethodSignatureAsString(functions.get(0)),
                                                                            encodedArgs);
                                                } else {
                                                    function =
                                                            FunctionUtility
                                                                    .newSendTransactionProxyFunction(
                                                                            uid,
                                                                            transactionID,
                                                                            seq,
                                                                            path.toString(),
                                                                            ContractABIDefinition.getMethodSignatureAsString(functions.get(0)),
                                                                            encodedArgs);
                                                }

                                                String encodedAbi =
                                                        FunctionEncoder.encode(function);
                                                // get signed transaction hex string
                                                RawTransaction rawTransaction =
                                                        BlockUtils.buildTransaction(
                                                                contractAddress,
                                                                BigInteger.valueOf(groupId),
                                                                BigInteger.valueOf(chainId),
                                                                BigInteger.valueOf(blockNumber),
                                                                encodedAbi);
                                                EthereumAccount ethereumAccount = (EthereumAccount) context.getAccount();
                                                //String signTx = accountSign(ethereumAccount,rawTransaction);
                                                TransactionParams transaction =
                                                        new TransactionParams(
                                                                request,
                                                                null,
                                                                TransactionParams.SUB_TYPE
                                                                        .SEND_TX_BY_PROXY);

                                                transaction.setAbi(abi);
                                                Request req =
                                                        Request.newRequest(
                                                                ETHEREUM_SEND_TRANSACTION,
                                                                objectMapper.writeValueAsBytes(
                                                                        transaction));

                                                if (logger.isDebugEnabled()) {
                                                    logger.debug(
                                                            "asyncSendTransactionByProxy, uid: {}, tid: {}, seq: {}, path: {}, abi: {}",
                                                            uid,
                                                            transactionID,
                                                            seq,
                                                            path,
                                                            abi);
                                                }
                                                connection.asyncSend(
                                                        req,
                                                        response -> {
                                                            try {
                                                                if (response.getErrorCode()
                                                                        != Success) {
                                                                    throw new EthStubException(
                                                                            response.getErrorCode(),
                                                                            response
                                                                                    .getErrorMessage());
                                                                }

                                                                TransactionReceipt receipt =
                                                                        objectMapper.readValue(
                                                                                response.getData(),
                                                                                TransactionReceipt
                                                                                        .class);
                                                                if (logger.isDebugEnabled()) {
                                                                    logger.debug(
                                                                            "TransactionReceipt: {}",
                                                                            receipt);
                                                                }

                                                                if (receipt.isStatusOK()) {

                                                                    BlockManager blockManager =
                                                                            context
                                                                                    .getBlockManager();

                                                                    blockManager.asyncGetBlock(
                                                                            Numeric.decodeQuantity(
                                                                                    String.valueOf(receipt
                                                                                            .getBlockNumber()))
                                                                                    .longValue(),
                                                                            (blockException,
                                                                             block) -> {
                                                                                try {
                                                                                    if (Objects
                                                                                            .nonNull(
                                                                                                    blockException)) {
                                                                                        callback
                                                                                                .onTransactionResponse(
                                                                                                        new TransactionException(
                                                                                                                HandleGetBlockNumberFailed,
                                                                                                                blockException
                                                                                                                        .getMessage()),
                                                                                                        null);
                                                                                        return;
                                                                                    }
                                                                                    transactionResponse
                                                                                            .setBlockNumber(
                                                                                                    Numeric
                                                                                                            .decodeQuantity(
                                                                                                                    String.valueOf(receipt
                                                                                                                            .getBlockNumber()))
                                                                                                            .longValue());
                                                                                    transactionResponse
                                                                                            .setHash(
                                                                                                    receipt
                                                                                                            .getTransactionHash());
                                                                                    // decode
                                                                                    String output = null;
                                                                                    //receipt.getOutput().substring(130);

                                                                                    ABIObject
                                                                                            outputObj =
                                                                                            ABIObjectFactory
                                                                                                    .createOutputObject(
                                                                                                            functions
                                                                                                                    .get(
                                                                                                                            0));
                                                                                    transactionResponse
                                                                                            .setResult(BlockUtils.decode(
                                                                                                    outputObj,
                                                                                                    output)
                                                                                                    .toArray(
                                                                                                            new String
                                                                                                                    [0]));

                                                                                    transactionResponse
                                                                                            .setErrorCode(Success);
                                                                                    transactionResponse
                                                                                            .setMessage(getStatusMessage(Success));
                                                                                    callback
                                                                                            .onTransactionResponse(
                                                                                                    null,
                                                                                                    transactionResponse);
                                                                                    if (logger
                                                                                            .isDebugEnabled()) {
                                                                                        logger
                                                                                                .debug(
                                                                                                        " hash: {}, response: {}",
                                                                                                        receipt
                                                                                                                .getTransactionHash(),
                                                                                                        transactionResponse);
                                                                                    }
                                                                                } catch (
                                                                                        Exception
                                                                                                e) {
                                                                                    logger.warn(
                                                                                            " e: ",
                                                                                            e);
                                                                                    callback
                                                                                            .onTransactionResponse(
                                                                                                    new TransactionException(UnclassifiedError, e.getMessage()),
                                                                                                    null);
                                                                                }
                                                                            });

                                                                } else {
                                                                    transactionResponse
                                                                            .setErrorCode(
                                                                                    SendTransactionNotSuccessStatus);
                                                                    if ("0x16"
                                                                            .equals(
                                                                                    receipt
                                                                                            .getStatus())) {
                                                                        Tuple2<Boolean, String>
                                                                                booleanStringTuple2 =
                                                                                RevertMessage
                                                                                        .tryParserRevertMessage(
                                                                                                receipt
                                                                                                        .getStatus(),
                                                                                                null);
                                                                        if (booleanStringTuple2
                                                                                .getValue1()
                                                                                .booleanValue()) {
                                                                            transactionResponse
                                                                                    .setMessage(
                                                                                            booleanStringTuple2
                                                                                                    .getValue2());
                                                                        } else {
                                                                            // return revert message
                                                                            transactionResponse
                                                                                    .setMessage(
                                                                                            null);
                                                                        }
                                                                    } else {
                                                                        transactionResponse
                                                                                .setMessage(
                                                                                        getStatusMessage(
                                                                                                Integer.parseInt(receipt.getStatus())));
                                                                    }

                                                                    callback.onTransactionResponse(
                                                                            null,
                                                                            transactionResponse);
                                                                }
                                                            } catch (EthStubException e) {
                                                                logger.warn(" e: ", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(
                                                                                e.getErrorCode(),
                                                                                e.getMessage()),
                                                                        null);
                                                            } catch (Exception e) {
                                                                logger.warn(" e: ", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(

                                                                                UnclassifiedError,
                                                                                e.getMessage()),
                                                                        null);
                                                            }
                                                        });

                                            } catch (EthStubException e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                e.getErrorCode(), e.getMessage()),
                                                        null);
                                            } catch (Exception e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                UnclassifiedError,
                                                                e.getMessage()),
                                                        null);
                                            }
                                        });
                            });

        } catch (EthStubException e) {
            logger.warn(" e: ", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        }
    }


    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_NUMBER, "");
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != EthereumType.StatusCode.Success) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), -1);
                    } else {
                        BigInteger blockNumber = new BigInteger(response.getData());
                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, blockNumber.longValue());
                    }
                });
    }

    @Override
    public void asyncGetBlock(long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_BY_NUMBER, "");
        HashMap hashMap = new HashMap<>();
        hashMap.put("blockNumber", blockNumber);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setProperties(hashMap);
        request.setResourceInfo(resourceInfo);
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != EthereumType.StatusCode.Success) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        //忽略多余字段
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        Block block = null;
                        try {
                            block = objectMapper.readValue(response.getData(), Block.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, block);
                    }
                });
    }

    @Override
    public void asyncGetTransaction(
            String transactionHash,
            long blockNumber,
            BlockManager blockManager,
            boolean isVerified,
            Connection connection,
            GetTransactionCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_TRANSACTION, "");
        HashMap hashMap = new HashMap<>();
        hashMap.put("blockNumber", blockNumber);
        hashMap.put("transactionHash", transactionHash);
        hashMap.put("isVerified", isVerified);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setProperties(hashMap);
        request.setResourceInfo(resourceInfo);
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != EthereumType.StatusCode.Success) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        org.web3j.protocol.core.methods.response.Transaction transaction = null;
                        try {
                            transaction = objectMapper.readValue(response.getData(), org.web3j.protocol.core.methods.response.Transaction.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        com.webank.wecross.stub.Transaction weCrossTransaction = TransactionUtils.covertToTransaction(transaction);
                        logger.debug("get transaction by transactionHash : {}, transaction : {}", transactionHash, weCrossTransaction.toString());
                        callback.onResponse(null, weCrossTransaction);
                    }
                });
    }

    @Override
    public void asyncCustomCommand(String command, Path path, Object[] args, Account account, BlockManager blockManager, Connection connection, CustomCommandCallback callback) {
        //TODO: 定制化请求，应该可以不实现
    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        ECKeyPair ecKeyPair = ((EthereumAccount) account).getEcKeyPair();
        byte[] hashMsg = Hash.sha3(message);
        Sign.SignatureData signatureData = Sign.signMessage(hashMsg, ecKeyPair, false);
        return concatenateSignature(signatureData.getR(), signatureData.getS(), signatureData.getV());
    }

    public static byte[] concatenateSignature(byte[] r, byte[] s, byte[] v) {
        byte[] signature = new byte[65];
        System.out.println("r.length : " + r.length);
        System.out.println("s.length : " + s.length);
        System.out.println("v.length : " + v.length);
        System.arraycopy(r, 0, signature, 0, r.length);
        System.arraycopy(s, 0, signature, 32, s.length);
        signature[64] = v[0];
        return signature;
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        byte[] hashMsg = Hash.sha3(message);
        byte[] r = Arrays.copyOfRange(signBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signBytes, 32, 64);
        byte v = signBytes[64];
        int recId = v - 27;
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        BigInteger recoverPubKey = Sign.recoverFromSignature(recId, sig, hashMsg);
        String recoverAddress = Keys.getAddress(recoverPubKey);
        return Numeric.toBigInt(recoverAddress).equals(Numeric.toBigInt(identity));
    }


    /**
     * @param properties
     * @throws
     */
    public void checkProperties(Map<String, String> properties) throws EthStubException {
        if (!properties.containsKey(GETH_PROXY_NAME)) {
            throw new EthStubException(
                    EthereumType.StatusCode.InvalidParameter,
                    "Proxy contract address not found, resource: " + GETH_PROXY_NAME);
        }

        if (!properties.containsKey(GETH_GROUP_ID)) {
            throw new EthStubException(
                    EthereumType.StatusCode.InvalidParameter,
                    "Group id not found, resource: " + GETH_GROUP_ID);
        }

        if (!properties.containsKey(GETH_CHAIN_ID)) {
            throw new EthStubException(
                    EthereumType.StatusCode.InvalidParameter,
                    "Chain id not found, resource: " + GETH_CHAIN_ID);
        }
    }

    /**
     * @param context
     * @param request
     * @param connection
     * @param callback
     */
    private void asyncCallByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {
        TransactionResponse transactionResponse = new TransactionResponse();
        Path path = context.getPath();
        String name = path.getResource();
        // query abi
        asyncCnsService.queryABI(
                name,
                this,
                connection,
                (queryABIException, abi) -> {
                    try {
                        if (Objects.nonNull(queryABIException)) {
                            throw new EthStubException(
                                    ABINotExist, queryABIException.getMessage());
                        }
                        // encode
                        String[] args = request.getArgs();
                        String method = request.getMethod();
                        List<AbiDefinition> functions =
                                loadABI(abi).getFunctions().get(method);
                        String transactionID = (String) request.getOptions().get(StubConstant.XA_TRANSACTION_ID);
                        String encodedArgs = "";
                        ABIObject inputObj =
                                ABIObjectFactory.createInputObject(functions.get(0));
                        if (!Objects.isNull(args)) {
                            ABIObject encodedObj =
                                    BlockUtils.encode(inputObj, Arrays.asList(args));
                            encodedArgs = encodedObj.encode();
                        }
                        Function function;
                        if (Objects.isNull(transactionID)
                                || transactionID.isEmpty()
                                || "0".equals(transactionID)) {
                            function = FunctionUtility.newConstantCallProxyFunction(
                                    path.getResource(),
                                    ContractABIDefinition.getMethodSignatureAsString(functions.get(0)),
                                    encodedArgs);
                        } else {
                            function =
                                    FunctionUtility.newConstantCallProxyFunction(
                                            transactionID,
                                            path.toString(),
                                            ContractABIDefinition.getMethodSignatureAsString(functions.get(0)),
                                            encodedArgs);
                        }

                        // 默认用超管账户地址
                        String from = ADMIN.getAddress();
                        if (Objects.nonNull(context.getAccount())) {
                            EthereumAccount ethAccount = (EthereumAccount) context.getAccount();
                            from = ethAccount.getIdentity();
                        }
                        TransactionParams transaction =
                                new TransactionParams(
                                        request,
                                        FunctionEncoder.encode(function),
                                        TransactionParams.SUB_TYPE.CALL_BY_PROXY);
                        transaction.setFrom(from);
                        transaction.setTo(Config.PROXY_ADDRESS);
                        transaction.setAbi(abi);
                        Request req =
                                Request.newRequest(
                                        ETHEREUM_CALL,
                                        objectMapper.writeValueAsBytes(transaction));

                        connection.asyncSend(
                                req,
                                connectionResponse -> {
                                    try {
                                        if (connectionResponse.getErrorCode()
                                                != EthereumType.StatusCode.Success) {
                                            throw new EthStubException(
                                                    connectionResponse.getErrorCode(),
                                                    connectionResponse.getErrorMessage());
                                        }

                                        CallOutput callOutput =
                                                objectMapper.readValue(
                                                        connectionResponse.getData(),
                                                        CallOutput.class);

                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    " call result, status: {}, blk: {}",
                                                    callOutput.getStatus(),
                                                    callOutput.getCurrentBlockNumber());
                                        }

                                        if ("0x0".equals(callOutput.getStatus())) {
                                            transactionResponse.setErrorCode(
                                                    EthereumType.StatusCode.Success);
                                            transactionResponse.setMessage(
                                                    EthereumType.StatusCode.getStatusMessage(
                                                            EthereumType.StatusCode.Success));

                                            ABIObject outputObj =
                                                    ABIObjectFactory.createOutputObject(
                                                            functions.get(0));

                                            // decode outputs
                                            String output =
                                                    callOutput.getOutput().substring(130);
                                            transactionResponse.setResult(
                                                    BlockUtils
                                                            .decode(outputObj, output)
                                                            .toArray(new String[0]));
                                        } else if (String.valueOf(
                                                EthereumType.StatusCode.CallNotSuccessStatus)
                                                .equals(callOutput.getStatus())) {
                                            transactionResponse.setErrorCode(
                                                    EthereumType.StatusCode.CallNotSuccessStatus);
                                            transactionResponse.setMessage(
                                                    callOutput.getOutput());
                                        } else {
                                            transactionResponse.setErrorCode(
                                                    EthereumType.StatusCode.CallNotSuccessStatus);

                                            Tuple2<Boolean, String> booleanStringTuple2 =
                                                    RevertMessage.tryParserRevertMessage(
                                                            callOutput.getStatus(),
                                                            callOutput.getOutput());
                                            if (booleanStringTuple2
                                                    .getValue1()) {
                                                transactionResponse.setMessage(
                                                        booleanStringTuple2.getValue2());
                                            } else {
                                                transactionResponse.setMessage(
                                                        (EthereumType.StatusCode.getStatusMessage(Integer.parseInt(callOutput.getStatus()))));
                                            }
                                        }

                                        callback.onTransactionResponse(null, transactionResponse);
                                    } catch (EthStubException e) {
                                        logger.warn(" e: ", e);
                                        callback.onTransactionResponse(
                                                new TransactionException(
                                                        e.getErrorCode(), e.getMessage()),
                                                null);
                                    } catch (Exception e) {
                                        logger.warn(" e: ", e);
                                        callback.onTransactionResponse(
                                                new TransactionException(
                                                        EthereumType.StatusCode.UnclassifiedError,
                                                        e.getMessage()),
                                                null);
                                    }
                                });
                    } catch (EthStubException bse) {
                        logger.warn(" e: ", bse);
                        callback.onTransactionResponse(
                                new TransactionException(bse.getErrorCode(), bse.getMessage()),
                                null);
                    } catch (Exception e) {
                        logger.warn(" e: ", e);
                        callback.onTransactionResponse(
                                new TransactionException(
                                        EthereumType.StatusCode.UnclassifiedError, e.getMessage()),
                                null);
                    }
                });

    }
}
