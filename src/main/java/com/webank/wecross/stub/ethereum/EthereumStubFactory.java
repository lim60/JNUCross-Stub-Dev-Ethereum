package com.webank.wecross.stub.ethereum;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.ethereum.account.EthereumAccount;
import com.webank.wecross.stub.ethereum.factory.EthereumConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;
import org.web3j.crypto.Credentials;

/**
 * 组件实例化工厂
 * 必须的接口
 * 已完成测试
 */
@Stub("EthereumStub")
public class EthereumStubFactory implements StubFactory {

    private static final Logger logger = LoggerFactory.getLogger(EthereumStubFactory.class);


    @Override
    public void init(WeCrossContext context) {
        //未发现调用，不予实现
    }

    @Override
    public Driver newDriver() {
        // 初始化默认eth-Driver
        return new EthereumDriver();
    }

    @Override
    public Connection newConnection(String path) {
        logger.info("New connection: {}", path);
        return EthereumConnectionFactory.build(path, "stub.toml");
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        // 初始化 Account
        return new EthereumAccount(properties);
    }

    @Override
    public void generateAccount(String path, String[] args) {
        try {
            Credentials cred = WalletUtils.loadCredentials("", "./conf/UTC--2023-07-16T15-59-49.181165420Z--18032fb1bb6731060bed83316db4aab0c97e45b4");
            logger.error("!!!ETH pubKey:" + cred.getEcKeyPair().getPublicKey().toString(16));
            logger.error("!!!ETH secKey:" + cred.getEcKeyPair().getPrivateKey().toString(16));
        }catch (IOException | CipherException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void generateConnection(String path, String[] args) {

    }
}
