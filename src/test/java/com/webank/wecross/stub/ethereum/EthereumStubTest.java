package com.webank.wecross.stub.ethereum;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.ethereum.common.EthereumConstants;
import com.webank.wecross.stub.ethereum.factory.EthereumConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class EthereumStubTest {
    public EthereumStubTest() throws CipherException, IOException {
    }

    @Test
    public void EthereumStubFactoryTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }


    Map<String, Object> account = new HashMap<>();
    Web3j web3 = Web3j.build(new HttpService("http://81.71.46.41:8546"));
    Credentials credentials = WalletUtils.loadCredentials("", "./src/test/resources/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");
    Admin admin = Admin.build(new HttpService("http://81.71.46.41:8546"));

    //对照wecross的写入
    @Before
    public void init() {
        account.put("name", "admin");
        account.put("publicKey", credentials.getEcKeyPair().getPublicKey().toString());
        account.put("privateKey", credentials.getEcKeyPair().getPrivateKey().toString());
        account.put("type", EthereumConstants.GETH_ACCOUNT);
        account.put("address", credentials.getAddress());
    }

    @Test
    public void EthereumAccountTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Account ethereumAccount = stubFactory.newAccount(account);
        System.out.println(ethereumAccount.getIdentity());
    }


    @Test
    public void EthereumDriverAsyncGetBlockTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        stubFactory.newDriver().asyncGetBlock(22L, true, stubFactory.newConnection("./src/test/resources"), ((e, block) -> {
            assertNull(e);
            BlockHeader blockHeader = block.getBlockHeader();
            List<String> transactionsHashes = block.getTransactionsHashes();
            assertEquals(1, transactionsHashes.size());
            assertTrue(Objects.nonNull(transactionsHashes.get(0)));
            assertTrue(block.getRawBytes().length > 1);
            assertTrue(Objects.nonNull(blockHeader));
            assertTrue(Objects.nonNull(blockHeader.getHash()));
            assertTrue(Objects.nonNull(blockHeader.getReceiptRoot()));
            assertTrue(Objects.nonNull(blockHeader.getTransactionRoot()));
            assertTrue(Objects.nonNull(blockHeader.getPrevHash()));
            assertTrue(Objects.nonNull(blockHeader.getStateRoot()));
        }));
    }
    @Test
    public void EthereumConnectionTest() {
        // https://nodes.mewapi.io/rpc/eth
        System.out.println("Connecting to Ethereum ...");
        System.out.println("Successfully connected to Ethereum ");
        try {
            // web3_clientVersion returns the current client version.
            Web3ClientVersion clientVersion = web3.web3ClientVersion().send();

            System.out.println("Client version: " + clientVersion.getWeb3ClientVersion());

            // eth_blockNumber returns the number of most recent block.
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();

            System.out.println("Block number: " + blockNumber.getBlockNumber());

            // eth_gasPrice, returns the current price per gas in wei.
            EthGasPrice gasPrice = web3.ethGasPrice().send();

            System.out.println("Gas price: " + gasPrice.getGasPrice());

            for (int i = blockNumber.getBlockNumber().intValue() - 5; i < blockNumber.getBlockNumber().intValue() + 2; i++) {
                EthBlock.Block block = web3.ethGetBlockByNumber(
                        DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), true).send().getBlock();
                List<EthBlock.TransactionResult> transactions = block.getTransactions();
                /*if (transactions.isEmpty())
                    continue;*/
                System.out.println(i + "\t - current hash : " + block.getHash() + ", pre hash : " + block.getParentHash());
                for (EthBlock.TransactionResult tr : transactions) {
                    Transaction trans = ((EthBlock.TransactionObject) tr).get();
                    System.out.println("\t Block Hash :" + trans.getBlockHash());
                    System.out.println("\t BlockNumber :" + trans.getBlockNumber());
                    System.out.println("\t Hash : " + trans.getHash());
                    System.out.println("\t From :" + trans.getFrom());
                    System.out.println("\t To :" + trans.getTo());
                    System.out.println("\t Input : " + trans.getInput());
                    System.out.println("\t Value :" + trans.getValue());
                    System.out.println("\t Value Raw :" + trans.getValueRaw());
                    System.out.println("\t---------");
                }


                //System.out.println(i + "\t - current hash : " + block.getHash() + ", pre hash : " + block.getParentHash());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error whilst sending json-rpc requests", ex);
        }
    }

    @Test
    public void Test1() {
        try {
            Web3j web3j = Web3j.build(new HttpService("http://10.154.24.12:8545"));
            //通常情况下，以太坊节点是不会让其他人用它上面的账户，我们要自己创建钱包，用自己的私钥对交易签名，
            //下面的私钥是ganache上第一个账户的私钥，虽然交易发起者和上面的交易一样，但两者本质是不同的。
            //Credentials mysigner = Credentials.create("f6b3f11ac120b04581d0273faeacbed5be08f4a0cdc3ddb82ed522f9a344b350");
            List<String> addressList = web3j.ethAccounts().send().getAccounts();
            for (String i : addressList) {
                System.out.println("account : " + i);
            }
            //String txhash = Transfer.sendFunds(web3j, mysigner, addressList.get(1), BigDecimal.valueOf(1.0), Convert.Unit.ETHER).send().getTransactionHash();
            //System.out.println("txhash:"+txhash);
            //TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txhash).send().getTransactionReceipt().get();
            //System.out.println(receipt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConnection() throws IOException, ExecutionException, InterruptedException {
        //tokenDeal(web3,credentials,"0x5B38Da6a701c568545dCfcB03FcB875f56beddC4", 1,"test");
        //加载本地KeyStore文件生成Credentials对象
        //Credentials credentials = WalletUtils.loadCredentials(password,keyStore);
        //web3.ethGetTransactionByHash("0x39cdc8f93969ec04757bb2d8dcc130a6f278990c9481e834109e6a522dc354f4");
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        //BigInteger balance = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        BigInteger gasPrice = web3.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = new BigInteger("900000");
        web3.ethGasPrice().send().getGasPrice();
        long chainId = 1337;
        admin.personalUnlockAccount(credentials.getAddress(), "");
        //生成RawTransaction交易对象 RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, amountToTransferInWei, data);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, "0x5B38Da6a701c568545dCfcB03FcB875f56beddC4", new BigInteger("1"), "abcde123");//可以额外带数据
        //使用Credentials对象对RawTransaction对象进行签名
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println(transactionHash);
    }

    @Test
    public void TestToml() {
        String path = "./src/test/resources";
        String fileName = "stub.toml";

        EthereumConnection connection = EthereumConnectionFactory.build(path, fileName);
        List<String> addressList = null;
        try {
            addressList = connection.getWeb3j().ethAccounts().send().getAccounts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String i : addressList) {
            System.out.println("account : " + i);
        }
        System.out.println("success");
    }

    @Test
    public void WalletTest() throws Exception {
        /*String fileName = WalletUtils.generateNewWalletFile(
                "123456",
                new File("./src/test/resources/"));*/

        Credentials credentials = WalletUtils.loadCredentials("123456", "./src/test/resources/UTC--2023-07-20T11-49-41.564000000Z--6cc775daaf3687624cd97c94ce7bf45daef12c45.json");
        System.out.println(credentials.getEcKeyPair().getPrivateKey());
        System.out.println(credentials.getEcKeyPair().getPublicKey());
        System.out.println(credentials.getAddress());

        Web3j web3j = Web3j.build(new HttpService("http://10.154.24.12:8545"));

        Request<?, EthGetBalance> ethGetBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST);
        System.out.println("balance1 : " + ethGetBalance.send().getBalance());
        System.out.println("账号余额(eth)1：" + Convert.fromWei(String.valueOf(ethGetBalance.send().getBalance()), Convert.Unit.ETHER) + "ETH");

        Credentials credentials2 = WalletUtils.loadCredentials("123456", "./src/test/resources/UTC--2023-07-20T13-48-18.332000000Z--6ef422e32d17207d14c4dd1cb7cccb7450c67842.json");
        System.out.println(credentials2.getEcKeyPair().getPrivateKey());
        System.out.println(credentials2.getEcKeyPair().getPublicKey());
        System.out.println(credentials2.getAddress());

        Request<?, EthGetBalance> ethGetBalance2 = web3j.ethGetBalance(credentials2.getAddress(), DefaultBlockParameterName.LATEST);
        System.out.println("balance2 : " + ethGetBalance2.send().getBalance());
        System.out.println("账号余额(eth)2：" + Convert.fromWei(String.valueOf(ethGetBalance2.send().getBalance()), Convert.Unit.ETHER) + "ETH");

        EthGetTransactionReceipt transactionReceipt = tokenDeal(web3j, credentials, credentials2.getAddress(), 500000000, "123");

        System.out.println("transactionHash" + transactionReceipt.getResult().getTransactionHash());
        //System.out.println(transactionReceipt.getTransactionReceipt());

        // EthBlock.Block block = web3j.ethGetBlockByNumber(
        //        DefaultBlockParameter.valueOf(), true).send().getBlock();
//        List<EthBlock.TransactionResult> transactions = block.getTransactions();
//                /*if (transactions.isEmpty())
//                    continue;*/
//        System.out.println("\t - current hash : " + block.getHash() + ", pre hash : " + block.getParentHash());
//        for (EthBlock.TransactionResult tr:transactions) {
//            Transaction trans = ((EthBlock.TransactionObject)tr).get();
//            System.out.println("\t Block Hash :" + trans.getBlockHash());
//            System.out.println("\t BlockNumber :" + trans.getBlockNumber());
//            System.out.println("\t Hash : " + trans.getHash());
//            System.out.println("\t From :" + trans.getFrom());
//            System.out.println("\t To :" + trans.getTo());
//            System.out.println("\t Input : " + trans.getInput());
//            System.out.println("\t Value :" + trans.getValue());
//            System.out.println("\t Value Raw :" + trans.getValueRaw());
//            System.out.println("\t---------");
//        }
    }

    public EthGetTransactionReceipt tokenDeal(Web3j web3j, Credentials credentials, String to, int value, String data) {
        try {
            //获取交易笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
            if (ethGetTransactionCount == null) {
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            //手续费
            BigInteger gasPrice;
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            if (ethGasPrice == null) {
                return null;
            }
            gasPrice = ethGasPrice.getGasPrice();
            //注意手续费的设置，这块很容易遇到问题
            BigInteger gasLimit = BigInteger.valueOf(600000000L);

            BigInteger amountToTransferInWei = Convert.toWei(
                    String.valueOf(value), Convert.Unit.ETHER).toBigInteger();

            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, amountToTransferInWei, data);

            System.out.println("rawTransaction " + rawTransaction);

            //进行签名操作
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, Long.parseLong(web3.netVersion().send().getNetVersion()), credentials);
            String hexValue = Numeric.toHexString(signMessage);

            System.out.println("hexValue " + hexValue);

            //发起交易
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            EthGetTransactionReceipt send = web3j.ethGetTransactionReceipt(ethSendTransaction.getTransactionHash()).send();
            if (send != null) {
                System.out.println("交易成功");
                System.out.println(send.getTransactionReceipt());
                send.getTransactionReceipt().get();
            }
            return send;
        } catch (Exception ex) {
            ex.printStackTrace(); //报错应进行错误处理
        }
        return null;
    }
}
