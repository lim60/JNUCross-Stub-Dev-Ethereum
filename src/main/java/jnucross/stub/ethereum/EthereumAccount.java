package jnucross.stub.ethereum;

import com.webank.wecross.stub.Account;

public class EthereumAccount implements Account {
    // Ethereum version geth v1.10.16


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getIdentity() {
        return null;
    }

    @Override
    public int getKeyID() {
        return 0;
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
