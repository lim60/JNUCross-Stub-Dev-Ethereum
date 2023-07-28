package jnucross.stub.ethereum;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * @author SDKany
 * @ClassName CrossTest
 * @Date 2023/7/28 21:53
 * @Version V1.0
 * @Description
 */
public class CrossTest {
    static String gethURL = "http://10.154.24.12:8545";
    static Web3j web3j = Web3j.build(new HttpService(gethURL));
    static String citaURL = "http://10.154.24.5:1337";
    static CITAj citaj = CITAj.build(new com.citahub.cita.protocol.http.HttpService(citaURL));

    @Test
    public void CITAToGeth() throws TransactionException, CipherException, IOException, ExecutionException, InterruptedException {
        System.out.println("Get data from CITA chain");
        byte[] data = readFromCITA();

        System.out.println("Write data to Geth");
        writeToGeth(data);
    }

    public static byte[] readFromCITA() throws IOException {
        // 读取链的时候不需要账户
        AppBlock.Block block = citaj.appGetBlockByNumber(DefaultBlockParameter.valueOf("latest"), true).send().getBlock();
        System.out.println("获取到的data为：" + block.getHash());
        byte[] blockHash = Numeric.hexStringToByteArray(block.getHash());
        return blockHash;
    }

    public static void writeToGeth(byte[] data) throws CipherException, IOException, ExecutionException, InterruptedException, TransactionException {
        // Load an account
        Credentials credentials = WalletUtils.loadCredentials("lix", "./src/test/resources/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");

        EthGetTransactionCount ethGetTransactionCount = web3j
                .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        // 获取账户余额，没钱写不了链
//        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
//        System.out.println("balance1 : " + ethGetBalance.getBalance());
//        System.out.println("账号余额(eth)1：" + Convert.fromWei(String.valueOf(ethGetBalance.getBalance()), Convert.Unit.ETHER) + "ETH");
        // 交易给自己
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, new BigInteger("50"), new BigInteger("3000000"),
                credentials.getAddress(), new BigInteger("100"), Numeric.toHexString(data));

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 1337,credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();

        if(ethSendTransaction.getError() != null)
            System.out.println("error : " + ethSendTransaction.getError().getMessage());

        System.out.println("transHash = " + ethSendTransaction.getTransactionHash());

        System.out.println("hexValue : " + hexValue);

        // 等待交易被挖矿
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                web3j,
                TransactionManager.DEFAULT_POLLING_FREQUENCY,
                TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
        TransactionReceipt txReceipt = receiptProcessor.waitForTransactionReceipt(ethSendTransaction.getTransactionHash());
        System.out.println("success mined!!!!");
        System.out.println(txReceipt);
        System.out.println("本数据已被写入Geth：" + web3j.ethGetTransactionByHash(txReceipt.getTransactionHash()).send().getTransaction().get().getInput());

    }
}
