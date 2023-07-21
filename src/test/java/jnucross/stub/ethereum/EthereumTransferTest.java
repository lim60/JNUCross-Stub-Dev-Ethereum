package jnucross.stub.ethereum;

import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

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
        //System.out.println(credentials.getEcKeyPair().getPrivateKey());
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
}
