package wecross.stub.demo;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;

import java.util.Map;

public class DemoConnection implements Connection {
    @Override
    public void asyncSend(Request request, Callback callback) {

    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {

    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }
}
