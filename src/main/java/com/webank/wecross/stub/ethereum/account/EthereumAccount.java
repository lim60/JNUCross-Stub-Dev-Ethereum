package com.webank.wecross.stub.ethereum.account;

import com.webank.wecross.stub.Account;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Map;

/**
 * 区块链账号，用于交易的签名
 */
public class EthereumAccount implements Account {
    // Ethereum version geth v1.10.16

    private String name;
    private String type;
    private BigInteger publicKey;
    private ECKeyPair ecKeyPair;

    private int keyID;

    /**
     * 针对公私钥对解析地址超过128字节的异常问题，增加直接填写
     */
    private String address;
    private boolean isDefault;

    private static final Logger logger = LoggerFactory.getLogger(EthereumAccount.class);

    public EthereumAccount(String name, String type, ECKeyPair ecKeyPair) {
        this.name = name;
        this.type = type;
        this.publicKey = ecKeyPair.getPublicKey();
        this.ecKeyPair = ecKeyPair;
    }

    public EthereumAccount(Map<String, Object> properties) {
        String name = (String) properties.get("username");
        String pubKeyStr = (String) properties.get("pubKey");
        String priKeyStr = (String) properties.get("secKey");
        String type = (String) properties.get("type");
        String address = (String) properties.get("ext0");
        if (name == null || name.length() == 0) {
            logger.error("name has not given");
            return;
        }

        if (pubKeyStr == null || pubKeyStr.length() == 0) {
            logger.error("publicKey has not given");
            return;
        }

        if (priKeyStr == null || priKeyStr.length() == 0) {
            logger.error("privateKey has not given");
            return;
        }

        if (type == null || type.length() == 0) {
            logger.error("type has not given");
            return;
        }

        if (address != null && address.length() != 0) {
            this.address = address;
        }

        try {
            BigInteger publicKey = Numeric.toBigInt(pubKeyStr);
            BigInteger privateKey = Numeric.toBigInt(priKeyStr);
            logger.info("New account: {} type:{}", name, type);
            ECKeyPair ecKeyPair = new ECKeyPair(privateKey, publicKey);

            this.name = name;
            this.type = type;
            this.publicKey = publicKey;
            this.ecKeyPair = ecKeyPair;
        } catch (Exception e) {
            logger.error("EthereumAccount exception: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIdentity() {
        return StringUtils.isBlank(address) ? Keys.getAddress(ecKeyPair) : address;
    }

    @Override
    public int getKeyID() {
        return keyID;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setKeyID(int keyID) {
        this.keyID = keyID;
    }

    public ECKeyPair getEcKeyPair() {
        return ecKeyPair;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }
}
