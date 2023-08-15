package com.webank.wecross.stub.ethereum;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ethereum.account.EthereumAccount;
import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.IOException;

/**
 * @author SDKany
 * @ClassName SignVerifyTest
 * @Date 2023/7/22 16:21
 * @Version V1.0
 * @Description
 */
public class SignVerifyTest {
    @Test
    public void SignAndVerifyTest() throws CipherException, IOException {
        String message = "hello world!";
        Driver driver = new EthereumDriver();

        Credentials credentials = WalletUtils.loadCredentials("123456", "./src/test/resources/UTC--2023-07-20T13-48-18.332000000Z--6ef422e32d17207d14c4dd1cb7cccb7450c67842.json");
        System.out.println("public key = " + credentials.getEcKeyPair().getPublicKey());
        System.out.println("private key = " + credentials.getEcKeyPair().getPrivateKey());
        System.out.println("address = " + credentials.getAddress());
        Account account = new EthereumAccount("ethereum", "user", credentials.getEcKeyPair());
        byte[] signedMessage = driver.accountSign(account, message.getBytes());
        //System.out.println(Numeric.toHexString(signedMessage));

        System.out.println(driver.accountVerify(credentials.getAddress(), signedMessage, message.getBytes()));

    }
}
