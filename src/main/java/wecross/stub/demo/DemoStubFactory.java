package wecross.stub.demo;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;

import java.util.Map;

@Stub("StubDemo")
public class DemoStubFactory implements StubFactory {
    @Override
    public void init(WeCrossContext context) {

    }

    @Override
    public Driver newDriver() {
        return null;
    }

    @Override
    public Connection newConnection(String path) {
        return null;
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        return null;
    }

    @Override
    public void generateAccount(String path, String[] args) {

    }

    @Override
    public void generateConnection(String path, String[] args) {

    }
}
