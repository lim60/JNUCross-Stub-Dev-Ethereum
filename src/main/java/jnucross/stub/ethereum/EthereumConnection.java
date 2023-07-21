package jnucross.stub.ethereum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import jnucross.stub.ethereum.common.EthereumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Map;

public class EthereumConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(EthereumConnection.class);

    private Web3j web3j = null;

    @Override
    public void asyncSend(Request request, Callback callback) {
        if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_NUMBER) {
            handleAsyncGetBlockNumberRequest(request, callback);
        } if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_BY_NUMBER) {
            handleAsyncGetBlockByNumberRequest(request, callback);
        } if (request.getType() == EthereumType.ConnectionMessage.ETHEREUM_GET_TRANSACTION) {
            handleAsyncGetTransactionRequest(request, callback);
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


    public void handleAsyncGetBlockNumberRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            response.setErrorCode(EthereumType.StatusCode.Success);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(EthereumType.StatusCode.Success));

            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);
        } catch (Exception e) {
            logger.warn("handleGetBlockNumberRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    public void handleAsyncGetTransactionRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            Map<Object, Object> map = request.getResourceInfo().getProperties();
            Long blockNumber = (Long) map.get("blockNumber");
            String transactionHash = (String) map.get("transactionHash");
            boolean isVerified = (boolean) map.get("isVerified");
            Transaction transaction = web3j.ethGetTransactionByHash(transactionHash).send().getTransaction().get();
            response.setErrorCode(EthereumType.StatusCode.Success);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(EthereumType.StatusCode.Success));
            ObjectMapper objectMapper = new ObjectMapper();
            response.setData(objectMapper.writeValueAsBytes(transaction));
            logger.debug("get transaction by transactionHash : {}, transaction : {}", transactionHash, transaction.getRaw());
        } catch (Exception e) {
            logger.warn("handleGetBlockByNumberRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    public void handleAsyncGetBlockByNumberRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = BigInteger.valueOf((Long) request.getResourceInfo().getProperties().get("blockNumber"));
            EthBlock.Block block = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
            response.setErrorCode(EthereumType.StatusCode.Success);
            response.setErrorMessage(EthereumType.StatusCode.getStatusMessage(EthereumType.StatusCode.Success));
            ObjectMapper objectMapper = new ObjectMapper();
            response.setData(objectMapper.writeValueAsBytes(block));
            logger.debug("get block by number : {}, block : {}", blockNumber, block);
        } catch (Exception e) {
            logger.warn("handleGetBlockByNumberRequest Exception:", e);
            response.setErrorCode(EthereumType.StatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }


    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {

    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    void setWeb3j(Web3j web3j){
        this.web3j = web3j;
    }
    Web3j getWeb3j(){
        return this.web3j;
    }
}
