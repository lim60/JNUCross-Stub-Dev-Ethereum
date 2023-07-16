package wecross.stub.ethereum;

import com.webank.wecross.stub.StubFactory;
import org.junit.Assert;
import org.junit.Test;

public class DemoStubTest {
    @Test
    public void DemoStubFactoryTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }
}
