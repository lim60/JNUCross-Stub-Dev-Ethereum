package com.webank.wecross.stub.ethereum.contract;

import static com.webank.wecross.stub.ethereum.common.Config.*;

/**
 * 屏蔽底层的合约参数细节
 *
 * @author DELL
 */
public class Contract {
    public static CNS_sol_CNS cns = CNS_sol_CNS.load(CNS_ADDRESS, web3j, ADMIN, contractGasProvider);
    public static Hub_sol_WeCrossHub hub = Hub_sol_WeCrossHub.load(HUB_ADDRESS,web3j,ADMIN,contractGasProvider);
    public static WeCrossProxy_sol_WeCrossProxy proxy = WeCrossProxy_sol_WeCrossProxy.load(PROXY_ADDRESS,web3j,ADMIN,contractGasProvider);
}
