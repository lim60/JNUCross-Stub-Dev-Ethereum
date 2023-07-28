package jnucross.stub.ethereum;

import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author SDKany
 * @ClassName EthereumTransferTest
 * @Date 2023/7/20 23:10
 * @Version V1.0
 * @Description
 */
public class EthereumTransferTest {
    Web3j web3j = Web3j.build(new HttpService("http://10.154.24.12:8545"));
    //Web3j web3j = Web3j.build(new HttpService("https://rpc.ankr.com/eth_goerli"));

    @Test
    public void TransferTest() throws Exception {
        System.out.println(web3j.web3ClientVersion().send().getWeb3ClientVersion());

        Credentials credentials = WalletUtils.loadCredentials("lix", "./src/test/resources/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");
        System.out.println(credentials.getEcKeyPair().getPrivateKey());
        //System.out.println(credentials.getEcKeyPair().getPublicKey());
        System.out.println(credentials.getAddress());

        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        System.out.println("balance1 : " + ethGetBalance.getBalance());
        System.out.println("账号余额(eth)1：" + Convert.fromWei(String.valueOf(ethGetBalance.getBalance()), Convert.Unit.ETHER) + "ETH");

        //System.out.println(web3j.ethAccounts().send().getAccounts());


        Credentials credentials2 = WalletUtils.loadCredentials("123456", "./src/test/resources/UTC--2023-07-20T13-48-18.332000000Z--6ef422e32d17207d14c4dd1cb7cccb7450c67842.json");
        //System.out.println(credentials2.getEcKeyPair().getPrivateKey());
        //System.out.println(credentials2.getEcKeyPair().getPublicKey());
        System.out.println(credentials2.getAddress());

        EthGetBalance ethGetBalance2 = web3j.ethGetBalance(credentials2.getAddress(), DefaultBlockParameterName.LATEST).send();
        System.out.println("balance2 : " + ethGetBalance2.getBalance());
        System.out.println("账号余额(eth)2：" + Convert.fromWei(String.valueOf(ethGetBalance2.getBalance()), Convert.Unit.ETHER) + "ETH");

        BigInteger value = Convert.toWei("1.0", Convert.Unit.ETHER).toBigInteger();


        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(
                web3j, credentials,
                "0x2022bcbca2a9b9e98f485dc8c80b02745943bd8f", //toAddress
                BigDecimal.ONE.valueOf(1), //value
                Convert.Unit.ETHER, //unit
                BigInteger.valueOf(8_000_000), //gasLimit
                DefaultGasProvider.GAS_LIMIT, //maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
                BigInteger.valueOf(3_100_000_000L) //maxFeePerGas (max fee transaction willing to pay)
        ).send();

        System.out.println("transactionReceipt.getType()");
        System.out.println(transactionReceipt.getType());

        System.out.println("transactionReceipt.getEffectiveGasPrice()");
        System.out.println(transactionReceipt.getEffectiveGasPrice());


//        TransactionReceipt transactionReceipt = Transfer.sendFundsEIP1559(
//                web3j, credentials,
//                credentials2.getAddress(), //toAddress
//                BigDecimal.ONE.valueOf(1), //value
//                Convert.Unit.ETHER, //unit
//                BigInteger.valueOf(8000000), //gasLimit
//                DefaultGasProvider.GAS_LIMIT, //maxPriorityFeePerGas (max fee per gas transaction willing to give to miners)
//                BigInteger.valueOf(3100000000L) //maxFeePerGas (max fee transaction willing to pay)
//        ).send();

        //System.out.println("transactionReceipt : " + transactionReceipt);
    }

    @Test
    public void GetAccountsTest(){
        List<String> addressList = null;
        try {
            addressList = web3j.ethAccounts().send().getAccounts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(addressList);
        for (String i : addressList) {
            System.out.println("account : " + i);
        }
    }

    @Test
    public void TransferTest2() throws CipherException, IOException, TransactionException, InterruptedException, ExecutionException {// Connect to the node

        // Load an account
        Credentials credentials = WalletUtils.loadCredentials("lix", "./src/test/resources/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");

        EthGetTransactionCount ethGetTransactionCount = web3j
                .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        EthGetBalance ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        System.out.println("balance1 : " + ethGetBalance.getBalance());
        System.out.println("账号余额(eth)1：" + Convert.fromWei(String.valueOf(ethGetBalance.getBalance()), Convert.Unit.ETHER) + "ETH");

        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, new BigInteger("10"), new BigInteger("3000000"),
               "0x6ef422e32d17207d14c4dd1cb7cccb7450c67842", new BigInteger("99"), "0x01234567890ABCDEF");

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 1337,credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();

        if(ethSendTransaction.getError() != null)
            System.out.println("error : " + ethSendTransaction.getError().getMessage());

        System.out.println("transHash = " + ethSendTransaction.getTransactionHash());

        System.out.println("hexValue : " + hexValue);

        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                web3j,
                TransactionManager.DEFAULT_POLLING_FREQUENCY,
                TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
        TransactionReceipt txReceipt = receiptProcessor.waitForTransactionReceipt(ethSendTransaction.getTransactionHash());
        System.out.println("success mined!!!!");
        System.out.println(txReceipt.getTransactionHash());
        System.out.println(txReceipt.getBlockHash());
        System.out.println(txReceipt.getBlockNumber());
        System.out.println(txReceipt);

        System.out.println(web3j.ethGetTransactionByHash(txReceipt.getTransactionHash()).send().getTransaction().get().getInput());

    }
}
