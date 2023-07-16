package wecross.stub.ethereum;

import com.webank.wecross.stub.Account;

public class EthereumAccount implements Account {
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
