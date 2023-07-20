package jnucross.stub.ethereum;

import com.moandjiezana.toml.Toml;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author SDKany
 * @ClassName EthereumConnectionFactory
 * @Date 2023/7/18 19:32
 * @Version V1.0
 * @Description
 */
public class EthereumConnectionFactory {
    public static EthereumConnection build(String path, String configFile) {
        // path = "http://10.154.24.12:8545";
        EthereumConnection connection = new EthereumConnection();
        // TODO, 初始化 Connection
        Toml toml = new Toml();
        toml = toml.read(new File(path + File.separator + configFile));
        Map<String, Object> stubConfig = toml.toMap();

        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");

        String url = ((ArrayList<String>)channelServiceConfigValue.get("connectionsStr")).get(0);
        connection.setWeb3j(Web3j.build(new HttpService(url)));
        return connection;
    }
}
