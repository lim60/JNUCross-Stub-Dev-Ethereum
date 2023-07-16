package jnucross.stub.ethereum;

import com.webank.wecross.stub.StubFactory;
import org.junit.Assert;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;

public class EthereumStubTest {
    @Test
    public void EthereumStubFactoryTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }

    @Test
    public void EthereumConnectionTest(){
        System.out.println("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService("https://rpc.sepolia.org"));
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
        } catch (IOException ex) {
            throw new RuntimeException("Error whilst sending json-rpc requests", ex);
        }
    }
}
