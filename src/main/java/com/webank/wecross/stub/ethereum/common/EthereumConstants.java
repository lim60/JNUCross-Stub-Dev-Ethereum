package com.webank.wecross.stub.ethereum.common;

import com.webank.wecross.stub.StubConstant;

public interface EthereumConstants {
    String ADMIN_ACCOUNT = "admin";

    /**
     * geth contract resource
     */
    String RESOURCE_TYPE_GETH_CONTRACT = "GETH_CONTRACT";
    /**
     *
     */
    String GETH_ACCOUNT = "GETH";
    /**
     *
     */
    String GETH_SM_ACCOUNT = "GM_GETH2.0";

    String GETH_GROUP_ID = "GETH_PROPERTY_GROUP_ID";
    String GETH_CHAIN_ID = "GETH_PROPERTY_CHAIN_ID";
    String GETH_STUB_TYPE = "GETH_PROPERTY_STUB_TYPE";
    String GETH_NODE_VERSION = "GETH_PROPERTY_NODE_VERSION";

    String GETH_SEALER_LIST = "VERIFIER";
    int GETH_NODE_ID_LENGTH = 128;

    String GETH_PROXY_ABI = "WeCrossProxyABI";
    String GETH_PROXY_NAME = StubConstant.PROXY_NAME;
    String GETH_HUB_NAME = StubConstant.HUB_NAME;
    String CUSTOM_COMMAND_DEPLOY = "deploy";
    String CUSTOM_COMMAND_REGISTER = "register";
    String CNS_METHOD_SELECTBYNAME = "selectByName";
    String PROXY_METHOD_DEPLOY = "deployContractWithRegisterCNS";
    String PPROXY_METHOD_REGISTER = "registerCNS";
    String PROXY_METHOD_GETPATHS = "getPaths";
}
