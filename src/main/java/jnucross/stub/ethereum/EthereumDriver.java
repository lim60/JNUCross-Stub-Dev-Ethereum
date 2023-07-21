package jnucross.stub.ethereum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import jnucross.stub.ethereum.common.EthereumType;
import jnucross.stub.ethereum.utils.BlockUtils;
import jnucross.stub.ethereum.utils.TransactionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EthereumDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(EthereumDriver.class);

    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        return null;
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        return null;
    }

    @Override
    public void asyncCall(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {

    }

    @Override
    public void asyncSendTransaction(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {

    }

    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_NUMBER, "");
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
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
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        EthBlock.Block block = null;
                        try {
                            block = objectMapper.readValue(response.getData(), EthBlock.Block.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, BlockUtils.covertToBlock(block));
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
                    if (response.getErrorCode() != 0) {
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

    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        return new byte[0];
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        return false;
    }
}
