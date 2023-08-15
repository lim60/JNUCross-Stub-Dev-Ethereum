package com.webank.wecross.stub.ethereum.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.ethereum.common.CnsInfo;
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

    public static List<CnsInfo> convertString2List(String json) throws JsonProcessingException {
        return objectMapper.readValue(
                json,
                objectMapper
                        .getTypeFactory()
                        .constructCollectionType(
                                List.class, CnsInfo.class));
    }

    public static Transaction covertToTransaction(org.web3j.protocol.core.methods.response.Transaction transaction) {
        com.webank.wecross.stub.Transaction trans = new Transaction();
        TransactionRequest transactionRequest = new TransactionRequest();
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setBlockNumber(transaction.getBlockNumber().longValue());
        transactionResponse.setHash(transaction.getHash());
        //transactionResponse.setResult();
        //transactionResponse.setErrorCode();
        //transactionResponse.setMessage();
        //transactionResponse.setExtraHashes(transaction);
        trans.setAccountIdentity(transaction.getFrom());
        trans.setTransactionRequest(transactionRequest);
        if (transaction.getRaw() != null) trans.setTxBytes(Numeric.hexStringToByteArray(transaction.getRaw()));
        trans.setTransactionResponse(transactionResponse);
        return trans;
    }
}
