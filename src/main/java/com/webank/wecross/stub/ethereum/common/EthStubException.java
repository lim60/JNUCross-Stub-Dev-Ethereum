package com.webank.wecross.stub.ethereum.common;

import com.webank.wecross.exception.WeCrossException;

public class EthStubException extends WeCrossException {
    public EthStubException(Integer errorCode, String message) {
        super(errorCode, message);
    }
}
