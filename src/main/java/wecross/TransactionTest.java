package wecross;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Optional;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;
import org.web3j.utils.Numeric;

public class TransactionTest {

    public static void main(String[] args) {

        System.out.println("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService("http://10.154.24.12:8545"));
        System.out.println("Successfuly connected to Ethereum");

        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        try {
            String  privetKey= "38193597071435092073330816640302480135932035477809105103919058804900169766293"; // Add a private key here
            // Decrypt private key into Credential object
            Credentials credentials = Credentials.create(privetKey);
            System.out.println("Account address: " + credentials.getAddress());
            System.out.println("Balance: "
                    + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send().getBalance().toString(), Unit.ETHER));

            // Get the latest nonce of current account
            EthGetTransactionCount ethGetTransactionCount = web3
                    .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            // Recipient address
            String recipientAddress = "0x18032fb1bb6731060bed83316db4aab0c97e45b4";
            // Value to transfer (in wei)
            BigInteger value = Convert.toWei("2", Unit.ETHER).toBigInteger();

            // Prepare the rawTransaction
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT,
                    recipientAddress, value);

            // Sign the transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
//
//            // Send transaction
//            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
//            String transactionHash = ethSendTransaction.getTransactionHash();
//            System.out.println("transactionHash: " + transactionHash);
//
//            // Wait for transaction to be mined
//            Optional<TransactionReceipt> transactionReceipt = null;
//            do {
//                System.out.println("checking if transaction " + transactionHash + " is mined....");
//                EthGetTransactionReceipt ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash)
//                        .send();
//                transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
//                Thread.sleep(3000); // Wait for 3 sec
//            } while (!transactionReceipt.isPresent());
//
//            System.out.println("Transaction " + transactionHash + " was mined in block # "
//                    + transactionReceipt.get().getBlockNumber());
//            System.out.println("Balance: "
//                    + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
//                    .send().getBalance().toString(), Unit.ETHER));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}