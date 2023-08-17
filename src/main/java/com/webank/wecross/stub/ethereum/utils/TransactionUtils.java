package com.webank.wecross.stub.ethereum.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.ethereum.EthereumConnection;
import com.webank.wecross.stub.ethereum.common.CnsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author SDKany
 * @ClassName TransactionUtils
 * @Date 2023/7/21 21:21
 * @Version V1.0
 * @Description
 */
public class TransactionUtils {
    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtils.class);


    public static List<CnsInfo> convertString2List(String json) throws JsonProcessingException {
        return objectMapper.readValue(
                json,
                objectMapper
                        .getTypeFactory()
                        .constructCollectionType(
                                List.class, CnsInfo.class));
    }

    public static Transaction covertToTransaction(org.web3j.protocol.core.methods.response.Transaction transaction) {
        logger.debug("in TransactionUtils covertToTransaction");
        com.webank.wecross.stub.Transaction trans = new Transaction();
        TransactionRequest transactionRequest = new TransactionRequest();
        TransactionResponse transactionResponse = new TransactionResponse();
        logger.debug("in TransactionUtils covertToTransaction in-transaction : {} ", transaction);
        logger.debug("in TransactionUtils covertToTransaction transaction.getBlockNumber().longValue() : {} ", transaction.getBlockNumber().longValue());
        transactionResponse.setBlockNumber(transaction.getBlockNumber().longValue());
        logger.debug("in TransactionUtils covertToTransaction 2");
        transactionResponse.setHash(transaction.getHash());
        logger.debug("in TransactionUtils covertToTransaction 3");
        //transactionResponse.setResult();
        //transactionResponse.setErrorCode();
        //transactionResponse.setMessage();
        //transactionResponse.setExtraHashes(transaction);
        trans.setAccountIdentity(transaction.getFrom());
        logger.debug("in TransactionUtils covertToTransaction 4");
        trans.setTransactionRequest(transactionRequest);
        logger.debug("in TransactionUtils covertToTransaction 5");
        if (transaction.getRaw() != null) trans.setTxBytes(Numeric.hexStringToByteArray(transaction.getRaw()));
        logger.debug("in TransactionUtils covertToTransaction 6");
        trans.setTransactionResponse(transactionResponse);
        logger.debug("in TransactionUtils covertToTransaction 7");
        return trans;
    }
}
