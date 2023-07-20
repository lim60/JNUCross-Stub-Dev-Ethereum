package jnucross.stub.ethereum;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.StubFactory;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EthereumStubTest {
    @Test
    public void EthereumStubFactoryTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }

    @Test
    public void EthereumConnectionTest(){
        // https://nodes.mewapi.io/rpc/eth
        System.out.println("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService("https://nodes.mewapi.io/rpc/eth"));
        System.out.println("Successfuly connected to Ethereum");
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

            for (int i = 17733137; i < 17733237; i++) {
                EthBlock.Block block = web3.ethGetBlockByNumber(
                        DefaultBlockParameter.valueOf(BigInteger.valueOf(i)), true).send().getBlock();
                List<EthBlock.TransactionResult> transactions = block.getTransactions();
                /*if (transactions.isEmpty())
                    continue;*/
                System.out.println(i + "\t - current hash : " + block.getHash() + ", pre hash : " + block.getParentHash());
                for (EthBlock.TransactionResult tr:transactions) {
                    Transaction trans = ((EthBlock.TransactionObject)tr).get();
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
    public void Test1(){
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
    public void TestToml(){
        String path = "./src/test/resources";
        String fileName = "stub-test.toml";

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
}
