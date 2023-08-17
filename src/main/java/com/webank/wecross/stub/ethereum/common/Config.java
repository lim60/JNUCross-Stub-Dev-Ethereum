package com.webank.wecross.stub.ethereum.common;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;

import java.io.IOException;
import java.math.BigInteger;

/**
 * 一些硬编码的参数，可能后面会有变动
 *
 * @author DELL
 */
public class Config {
    public static Credentials ADMIN;

    static {
        try {
            ADMIN = WalletUtils.loadCredentials("", "./conf/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");
        } catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    public static String HUB_ADDRESS = "0x940fb9eaf35b92bbf156fd0df75aab238df3c6de";
    public static String PROXY_ADDRESS = "0x1628c9a26079ba00d3c4749b709e03bb72a82a31";
    public static String CNS_ADDRESS = "0xc0ae66c325a990f5e3F6b537Ae54880561FB7831";
    public static String ETH_URL = "http://10.154.24.12:8545"; //"http://81.71.46.41:8546";
    public static ContractGasProvider contractGasProvider = new ContractGasProvider() {

        @Override
        public BigInteger getGasPrice(String s) {
            try {
                return web3j.ethGasPrice().send().getGasPrice();
            } catch (IOException e) {
                e.printStackTrace();
                return BigInteger.valueOf(10);
            }
        }

        @Override
        public BigInteger getGasPrice() {
            try {
                return web3j.ethGasPrice().send().getGasPrice();
            } catch (IOException e) {
                e.printStackTrace();
                return BigInteger.valueOf(10);
            }
        }

        @Override
        public BigInteger getGasLimit(String s) {
            return BigInteger.valueOf(900000);
        }

        @Override
        public BigInteger getGasLimit() {
            return BigInteger.valueOf(900000);
        }
    };
    public static Web3j web3j = Web3j.build(new HttpService(ETH_URL));
}
