package jnucross.stub.ethereum.account;

import com.webank.wecross.stub.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Map;

public class EthereumAccount implements Account {
    // Ethereum version geth v1.10.16

    private String name;
    private String type;
    private BigInteger publicKey;
    private ECKeyPair ecKeyPair;

    private int keyID;

    private boolean isDefault;

    private static final Logger logger = LoggerFactory.getLogger(EthereumAccount.class);

    public EthereumAccount(String name, String type, ECKeyPair ecKeyPair) {
        this.name = name;
        this.type = type;
        this.publicKey = ecKeyPair.getPublicKey();
        this.ecKeyPair = ecKeyPair;
    }

    public EthereumAccount(Map<String, Object> properties) {
        String name = (String) properties.get("name");
        String pubKeyStr = (String) properties.get("publicKey");
        String priKeyStr = (String) properties.get("privateKey");
        String type = (String) properties.get("type");
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
            return;
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
        return Keys.getAddress(ecKeyPair);
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
