package jnucross.stub.ethereum.common;

/**
 * @author SDKany
 * @ClassName EthereumType
 * @Date 2023/7/18 18:58
 * @Version V1.0
 * @Description Ethereum types
 */
public class EthereumType {
    public static final String STUB_NAME = "Ethereum1.10.16";

    public static final class ConnectionMessage {
        // Connection send message type
        public static final int ETHEREUM_CALL = 1001;
        public static final int ETHEREUM_SEND_TRANSACTION = 1002;
        public static final int ETHEREUM_GET_BLOCK_NUMBER = 1004;
        public static final int ETHEREUM_GET_BLOCK_BY_NUMBER = 1005;
        public static final int ETHEREUM_GET_TRANSACTION = 1006;
    }

    public static final class StatusCode{
        public static final int Success = 2000;
        public static final int InvalidParameter = 2001;
        public static final int UnrecognizedRequestType = 2002;

        public static final int HandleCallRequestFailed = 2022;

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
                default:
                    message = "unrecognized status: " + status;
                    break;
            }

            return message;
        }
    }

}
