package com.webank.wecross.stub.ethereum.common;

/**
 * @author SDKany
 * @ClassName EthereumType
 * @Date 2023/7/18 18:58
 * @Version V1.0
 * @Description Ethereum types
 */
public class EthereumType {
    public static final String STUB_NAME = "Ethereum1.10.16";
    public static final long CHAIN_ID = 1337;

    public static final class ConnectionMessage {
        // Connection send message type
        public static final int ETHEREUM_CALL = 1001;
        public static final int ETHEREUM_SEND_TRANSACTION = 1002;
        public static final int ETHEREUM_GET_BLOCK_NUMBER = 1004;
        public static final int ETHEREUM_GET_BLOCK_BY_NUMBER = 1005;
        public static final int ETHEREUM_GET_TRANSACTION = 1006;
    }

    public static final class StatusCode {
        public static final int Success = 2000;
        public static final int InvalidParameter = 2001;
        public static final int UnrecognizedRequestType = 2002;
        public static final int InvalidEncodedBlockHeader = 2003;
        public static final int TransactionProofVerifyFailed = 2004;
        public static final int TransactionReceiptProofVerifyFailed = 2005;

        public static final int TransactionReceiptNotExist = 2010;
        public static final int TransactionNotExist = 2011;
        public static final int BlockNotExist = 2012;
        public static final int TransactionProofNotExist = 2013;
        public static final int TransactionReceiptProofNotExist = 2014;

        public static final int HandleSendTransactionFailed = 2021;
        public static final int HandleCallRequestFailed = 2022;
        public static final int HandleGetBlockNumberFailed = 2023;
        public static final int HandleGetBlockFailed = 2024;
        public static final int HandleGetTransactionProofFailed = 2025;
        public static final int RegisterContractFailed = 2027;

        public static final int CallNotSuccessStatus = 2030;
        public static final int SendTransactionNotSuccessStatus = 2031;

        public static final int ABINotExist = 2040;
        public static final int EncodeAbiFailed = 2041;
        public static final int MethodNotExist = 2042;

        public static final int UnsupportedRPC = 2050;
        public static final int UnclassifiedError = 2100;
        public static final String CONSTRUCTOR_TYPE = "constructor";
        public static final String FUNCTION_TYPE = "function";
        public static final String EVENT_TYPE = "event";
        public static final String FALLBACK_TYPE = "fallback";
        public static final String RECEIVE_TYPE = "receive";

        public static String getStatusMessage(int status) {
            String message = "";
            switch (status) {
                case Success:
                    message = "success";
                    break;
                case InvalidParameter:
                    message = "invalid parameter";
                    break;
                case UnrecognizedRequestType:
                    message = "unrecognized request type";
                case TransactionReceiptNotExist:
                    message = "transaction receipt not exist";
                    break;
                case InvalidEncodedBlockHeader:
                    message = "invalid encoded block header";
                    break;
                case TransactionProofVerifyFailed:
                    message = " transaction verify failed";
                    break;
                case TransactionReceiptProofVerifyFailed:
                    message = " transaction receipt verify failed";
                    break;
                case TransactionNotExist:
                    message = "transaction not exist";
                    break;
                case TransactionProofNotExist:
                    message = "transaction proof not exist";
                    break;
                case TransactionReceiptProofNotExist:
                    message = "transaction receipt proof not exist";
                    break;
                case BlockNotExist:
                    message = "block not exist";
                    break;
                default:
                    message = "unrecognized status: " + status;
                    break;
            }

            return message;
        }
    }

}
