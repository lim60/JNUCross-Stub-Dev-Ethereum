package com.webank.wecross.stub.ethereum.protpcol;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class RevertMessage {
    public static final String RevertMethod = "08c379a0";
    public static final String RevertMethodWithHexPrefix = "0x08c379a0";

    public static final String SMRevertMethod = "c703cb12";
    public static final String SMRevertMethodWithHexPrefix = "0xc703cb12";
    private static final Logger logger = LoggerFactory.getLogger(RevertMessage.class);

    /**
     * @param message
     * @return
     */
    public static String getErrorMessage(String message) {
        int errorIndex = message.indexOf("error:");
        if (errorIndex > 0) {
            int errorCode = Integer.parseInt(message.substring(errorIndex + 6).trim());
            errorCode = (errorCode > 0 ? -errorCode : errorCode);
            if (logger.isDebugEnabled()) {
                logger.debug(" errorCode: {}, errorMessage: {}", errorCode, message);
            }
            return message;
        }

        return "";
    }

    private static boolean hasRevertMessage(String status, String output) {
        if (StringUtils.isEmpty(status) || StringUtils.isEmpty(output)) {
            return false;
        }
        try {
            BigInteger statusQuantity = Numeric.decodeQuantity(status);
            return !BigInteger.ZERO.equals(statusQuantity) && (output.startsWith(RevertMethodWithHexPrefix)
                    || output.startsWith(SMRevertMethodWithHexPrefix)
                    || (output.startsWith(RevertMethod) || output.startsWith(SMRevertMethod)));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * try to resolve revert message, supports recursive operations
     *
     * @param status
     * @param data
     * @return
     */
    public static Tuple2<Boolean, String> tryParserRevertMessage(String status, String data) {
        try {
            int revertLoop = 0;

            while (hasRevertMessage(
                    status, Numeric.cleanHexPrefix(data).substring((128 + 8) * revertLoop))) {
                revertLoop += 1;
            }
            if (revertLoop > 0) {
                Utf8String utf8String =
                        TypeDecoder.decode(
                                Numeric.cleanHexPrefix(data).substring((128 + 8) * revertLoop - 64),
                                0,
                                Utf8String.class);
                String revertMessage = utf8String.toString().trim();
                //简化
                String errorMessage = getErrorMessage(revertMessage);
                if (!errorMessage.isEmpty()) {
                    revertMessage = revertMessage + " ,message: " + errorMessage;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(" revertMessage: {}", revertMessage);
                }

                return new Tuple2<>(true, revertMessage);
            }

            return new Tuple2<>(false, null);
        } catch (Exception e) {
            return new Tuple2<>(false, null);
        }
    }
}
