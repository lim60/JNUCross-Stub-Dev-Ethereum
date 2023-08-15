package com.webank.wecross.stub.ethereum;


import com.webank.wecross.stub.*;
import com.webank.wecross.stub.ethereum.account.EthereumAccount;
import com.webank.wecross.stub.ethereum.common.EthereumConstants;
import com.webank.wecross.stub.ethereum.contract.Contract;
import org.junit.Before;
import org.junit.Test;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class EthereumDriverTest {
    Web3j web3 = Web3j.build(new HttpService("http://81.71.46.41:8546"));
    Credentials credentials = WalletUtils.loadCredentials("", "./src/test/resources/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");
    Admin admin = Admin.build(new HttpService("http://81.71.46.41:8546"));
    private Connection connection = null;
    private Driver driver = null;
    private EthereumAccount account = null;
    private ResourceInfo resourceInfo = null;

    public EthereumDriverTest() throws CipherException, IOException {
    }

    @Before
    public void initializer() throws Exception {
        EthereumStubFactory ethereumStubFactory = new EthereumStubFactory();
        Path path = Path.decode("a.b.c");
        driver = ethereumStubFactory.newDriver();
        Map<String, Object> accountmap = new HashMap<>();
        accountmap.put("name", "admin");
        accountmap.put("publicKey", credentials.getEcKeyPair().getPublicKey().toString());
        accountmap.put("privateKey", credentials.getEcKeyPair().getPrivateKey().toString());
        accountmap.put("type", EthereumConstants.GETH_ACCOUNT);
        accountmap.put("address", credentials.getAddress());
        account = new EthereumAccount(accountmap);
        connection = ethereumStubFactory.newConnection("./src/test/resources");
        //resourceInfo = ((EthereumConnection) connection).getResourceInfoList().get(0);
    }

    @Test
    public void testBase() throws ExecutionException, InterruptedException {
        String ss = Contract.cns.selectByName("test").sendAsync().get();
        System.out.println(ss);
    }
}
