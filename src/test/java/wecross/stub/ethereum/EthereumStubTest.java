package wecross.stub.ethereum;

import com.webank.wecross.stub.StubFactory;
import org.junit.Assert;
import org.junit.Test;

public class EthereumStubTest {
    @Test
    public void EthereumStubFactoryTest() {
        StubFactory stubFactory = new EthereumStubFactory();
        Assert.assertNotNull("stubFactory object is null", stubFactory);
    }
}
