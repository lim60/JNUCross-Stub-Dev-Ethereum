package jnucross.stub.ethereum.utils;

import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

/**
 * @author SDKany
 * @ClassName TransactionUtils
 * @Date 2023/7/21 21:21
 * @Version V1.0
 * @Description
 */
public class TransactionUtils {
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
        if(transaction.getRaw() != null)trans.setTxBytes(Numeric.hexStringToByteArray(transaction.getRaw()));
        trans.setTransactionResponse(transactionResponse);
        return trans;
    }
}
