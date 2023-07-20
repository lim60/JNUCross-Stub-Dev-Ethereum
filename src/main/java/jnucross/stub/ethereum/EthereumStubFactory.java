package jnucross.stub.ethereum;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import jnucross.stub.ethereum.account.EthereumAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Stub("EthereumStub")
public class EthereumStubFactory implements StubFactory {

    private static final Logger logger = LoggerFactory.getLogger(EthereumStubFactory.class);


    @Override
    public void init(WeCrossContext context) {

    }

    @Override
    public Driver newDriver() {
        Driver driver = new EthereumDriver();
        // TODO, 初始化 Driver
        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        logger.info("New connection: {}", path);
        return EthereumConnectionFactory.build(path, "stub.toml");
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        Account account = new EthereumAccount(properties);
        // TODO, 初始化 Account
        return account;
    }

    @Override
    public void generateAccount(String path, String[] args) {

    }

    @Override
    public void generateConnection(String path, String[] args) {

    }
}
