package com.webank.wecross.stub.ethereum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.ethereum.EthereumDriver;
import com.webank.wecross.stub.ethereum.account.LRUCache;
import com.webank.wecross.stub.ethereum.common.CnsInfo;
import com.webank.wecross.stub.ethereum.common.EthereumConstants;
import com.webank.wecross.stub.ethereum.common.EthereumType;
import com.webank.wecross.stub.ethereum.contract.Contract;
import com.webank.wecross.stub.ethereum.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.web3j.protocol.core.RemoteFunctionCall;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AsyncCnsService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCnsService.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private LRUCache<String, String> abiCache = new LRUCache<>(32);
    private ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory("AsyncCnsService-"));
    private static final long CLEAR_EXPIRES = 30L * 60L; // 30 min
    private Semaphore queryABISemaphore = new Semaphore(1, true);

    private EthereumDriver ethDriver = null;

    public EthereumDriver getEthDriver() {
        return ethDriver;
    }

    public void setEthDriver(EthereumDriver ethDriver) {
        this.ethDriver = ethDriver;
    }

    public AsyncCnsService() {
        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> abiCache.clear(), CLEAR_EXPIRES, CLEAR_EXPIRES, TimeUnit.SECONDS);
    }

    public interface QueryCallback {
        void onResponse(Exception e, String info);
    }

    public void queryABI(
            String name, Driver driver, Connection connection, QueryCallback callback) {
        try {
            /*WeCrossProxy ABI */
            if (EthereumConstants.GETH_PROXY_NAME.equals(name)) {
                String proxyABI = connection.getProperties().get(EthereumConstants.GETH_PROXY_ABI);
                if (logger.isTraceEnabled()) {
                    logger.trace("ProxyABI: {}", proxyABI);
                }
                callback.onResponse(null, proxyABI);
                return;
            }
            String abi = abiCache.get(name);
            if (abi != null) {
                callback.onResponse(null, abi);
                return;
            }
            // Only 1 thread can query abi remote
            queryABISemaphore.acquire(1);
            abi = abiCache.get(name);
            if (abi != null) {
                queryABISemaphore.release();
                callback.onResponse(null, abi);
                return;
            }
            String result = Contract.cns.selectByName(name).sendAsync().get();
            queryABISemaphore.release();
            if (Objects.isNull(result)) {
                callback.onResponse(new Exception("调用CNS的selectByName合约异常"), null);
                return;
            }
            List<CnsInfo> infoList = TransactionUtils.convertString2List(result);
            if (Objects.isNull(infoList) || infoList.isEmpty()) {
                callback.onResponse(null, null);
            } else {
                int size = infoList.size();
                String currentAbi = infoList.get(size - 1).getAbi();
                addAbiToCache(name, currentAbi);
                if (logger.isDebugEnabled()) {
                    logger.debug("queryABI name:{}, abi:{}", name, currentAbi);
                }
                callback.onResponse(null, currentAbi);
            }
        } catch (Exception e) {
            queryABISemaphore.release();
            callback.onResponse(e, null);
        }
    }

    public interface SelectCallback {
        void onResponse(Exception e, List<CnsInfo> infoList);
    }

    public void selectByNameAndVersion(
            String name,
            String version,
            Connection connection,
            Driver driver,
            SelectCallback callback) {
        select(name, version, connection, driver, callback);
    }

    public void selectByName(
            String name, Connection connection, Driver driver, SelectCallback callback) {
        select(name, null, connection, driver, callback);
    }

    private void select(
            String name,
            String version,
            Connection connection,
            Driver driver,
            SelectCallback callback) {

        TransactionRequest transactionRequest = new TransactionRequest();
        if (Objects.isNull(version)) {
            transactionRequest.setArgs(new String[]{name});
            transactionRequest.setMethod("selectByName");
        } else {
            transactionRequest.setArgs(new String[]{name});
            transactionRequest.setMethod("selectByNameAndVersion");
        }

        Path path = new Path();
        path.setResource(EthereumConstants.GETH_PROXY_NAME);

        TransactionContext transactionContext = new TransactionContext(null, path, null, null);

        driver.asyncCall(
                transactionContext,
                transactionRequest,
                true,
                connection,
                (transactionException, connectionResponse) -> {
                    try {
                        if (Objects.nonNull(transactionException)) {
                            callback.onResponse(
                                    new Exception(transactionException.getMessage()), null);
                            return;
                        }

                        if (connectionResponse.getErrorCode() != EthereumType.StatusCode.Success) {
                            callback.onResponse(
                                    new Exception(connectionResponse.getMessage()), null);
                            return;
                        }

                        List<CnsInfo> infoList =
                                objectMapper.readValue(
                                        connectionResponse.getResult()[0],
                                        objectMapper
                                                .getTypeFactory()
                                                .constructCollectionType(
                                                        List.class, CnsInfo.class));
                        callback.onResponse(null, infoList);

                    } catch (Exception e) {
                        logger.warn("exception occurs", e);
                        callback.onResponse(new Exception(e.getMessage()), null);
                    }
                });
    }

    public interface InsertCallback {
        void onResponse(Exception e);
    }

    public void registerCNSByProxy(
            Path path,
            String address,
            String version,
            String abi,
            Account account,
            BlockManager blockManager,
            Connection connection,
            InsertCallback callback) {

        Path proxyPath = new Path();
        proxyPath.setResource(EthereumConstants.GETH_PROXY_NAME);

        TransactionRequest transactionRequest =
                new TransactionRequest(
                        EthereumConstants.PPROXY_METHOD_REGISTER,
                        Arrays.asList(path.toString(), version, address, abi)
                                .toArray(new String[0]));

        TransactionContext requestTransactionContext =
                new TransactionContext(account, proxyPath, null, blockManager);

        ethDriver.asyncSendTransaction(
                requestTransactionContext,
                transactionRequest,
                true,
                connection,
                (exception, res) -> {
                    if (Objects.nonNull(exception)) {
                        logger.error(" registerCNS e: ", exception);
                        callback.onResponse(exception);
                        return;
                    }

                    if (res.getErrorCode() != EthereumType.StatusCode.Success) {
                        logger.error(
                                " deployAndRegisterCNS, error: {}, message: {}",
                                res.getErrorCode(),
                                res.getMessage());
                        callback.onResponse(new Exception(res.getMessage()));
                        return;
                    }

                    addAbiToCache(path.getResource(), abi);

                    logger.info(
                            " registerCNS successfully, name: {}, version: {}, address: {} ",
                            path.getResource(),
                            version,
                            address);

                    callback.onResponse(null);
                });
    }

    public LRUCache<String, String> getAbiCache() {
        return abiCache;
    }

    public void setAbiCache(LRUCache<String, String> abiCache) {
        this.abiCache = abiCache;
    }

    public void addAbiToCache(String name, String abi) {
        this.abiCache.put(name, abi);
    }
}
