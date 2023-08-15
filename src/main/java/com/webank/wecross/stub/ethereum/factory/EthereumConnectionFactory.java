package com.webank.wecross.stub.ethereum.factory;

import com.webank.wecross.stub.ResourceInfo;
import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.ethereum.EthereumConnection;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author SDKany
 * @ClassName EthereumConnectionFactory
 * @Date 2023/7/18 19:32
 * @Version V1.0
 * @Description
 */
public class EthereumConnectionFactory {
    public static EthereumConnection build(String path, String configFile) {
        EthereumConnection connection = new EthereumConnection();
        //初始化 Connection
        Toml toml = new Toml();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(path + File.separator + configFile);
        try {
            toml =toml.read(resource.getInputStream());
        } catch (IOException e) {

        }
        Map<String, Object> stubConfig = toml.toMap();
        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");
        //connection.setResourceInfoList();
        String url = ((ArrayList<String>)channelServiceConfigValue.get("connectionsStr")).get(0);
        connection.setWeb3j(Web3j.build(new HttpService(url)));
        //A dumb Recource list:
        List<ResourceInfo> ResourceInfoList = new ArrayList<>();
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setName("Nonsense Ethereum Resource");
        resourceInfo.setStubType("EthereumStub");
        resourceInfo.setProperties(null);
        resourceInfo.setChecksum(null);
        ResourceInfoList.add(resourceInfo);
        connection.setResourceInfoList(ResourceInfoList);
        return connection;
    }
}
