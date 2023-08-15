package com.webank.wecross.stub.ethereum.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.ethereum.common.ABIObject;
import com.webank.wecross.stub.ethereum.common.ContractABIDefinition;
import com.webank.wecross.stub.ethereum.wrapper.Bytes;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Int256;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.webank.wecross.stub.ethereum.common.EthereumType.StatusCode.*;

/**
 * @author SDKany
 * @ClassName BlockUtils
 * @Date 2023/7/19 21:09
 * @Version V1.0
 * @Description Block Utils
 */
public class BlockUtils {

    public static ObjectMapper objectMapper = new ObjectMapper();

    private static JsonNode decode(ABIObject abiObject) {
        JsonNodeFactory jsonNodeFactory = objectMapper.getNodeFactory();

        switch (abiObject.getType()) {
            case VALUE: {
                switch (abiObject.getValueType()) {
                    case BOOL: {
                        return jsonNodeFactory.booleanNode(
                                abiObject.getBoolValue().getValue());
                    }
                    case INT:
                    case UINT: {
                        return jsonNodeFactory.numberNode(
                                abiObject.getNumericValue().getValue());
                    }
                    case ADDRESS: {
                        return jsonNodeFactory.textNode(
                                abiObject.getAddressValue().toString());
                    }
                    case BYTES: {
                        return jsonNodeFactory.binaryNode(
                                abiObject.getBytesValue().getValue());
                    }
                    case DBYTES: {
                        return jsonNodeFactory.binaryNode(
                                abiObject.getDynamicBytesValue().getValue());
                    }
                    case STRING: {
                        return jsonNodeFactory.textNode(
                                abiObject.getStringValue().getValue());
                    }
                }
                break;
            }
            case LIST: {
                ArrayNode arrayNode = jsonNodeFactory.arrayNode();

                for (ABIObject listObject : abiObject.getListValues()) {
                    arrayNode.add(decode(listObject));
                }

                return arrayNode;
            }
            case STRUCT: {
                ArrayNode structNode = jsonNodeFactory.arrayNode();

                for (ABIObject listObject : abiObject.getStructFields()) {
                    structNode.add(decode(listObject));
                }

                return structNode;
            }
        }
        return null;
    }

    private static byte[] formatBytesN(ABIObject abiObject) {
        if (abiObject.getBytesLength() > 0
                && abiObject.getBytesValue().getValue().length > abiObject.getBytesLength()) {
            byte[] value = new byte[abiObject.getBytesLength()];
            System.arraycopy(
                    abiObject.getBytesValue().getValue(), 0, value, 0, abiObject.getBytesLength());
            return value;
        } else {
            return abiObject.getBytesValue().getValue();
        }
    }

    public static List<String> decode(ABIObject template, String buffer) {
        buffer = Numeric.cleanHexPrefix(buffer);
        ABIObject abiObject = template.decode(buffer);
        JsonNode jsonNode = decode(abiObject);
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < abiObject.getStructFields().size(); ++i) {
            ABIObject argObject = abiObject.getStructFields().get(i);
            JsonNode argNode = jsonNode.get(i);
            switch (argObject.getType()) {
                case VALUE: {
                    switch (argObject.getValueType()) {
                        case BOOL: {
                            result.add(String.valueOf(argObject.getBoolValue().getValue()));
                            break;
                        }
                        case UINT:
                        case INT: {
                            result.add(argObject.getNumericValue().getValue().toString());
                            break;
                        }
                        case ADDRESS: {
                            result.add(
                                    String.valueOf(argObject.getAddressValue().toString()));
                            break;
                        }
                        case BYTES: {
                            byte[] value = formatBytesN(argObject);
                            byte[] base64Bytes = Base64.getEncoder().encode(value);
                            result.add("base64://" + new String(base64Bytes));
                            break;
                        }
                        case DBYTES: {
                            byte[] base64Bytes =
                                    Base64.getEncoder()
                                            .encode(
                                                    argObject
                                                            .getDynamicBytesValue()
                                                            .getValue());
                            result.add("base64://" + new String(base64Bytes));
                            break;
                        }
                        case STRING: {
                            result.add(
                                    String.valueOf(argObject.getStringValue().getValue()));
                            break;
                        }
                        default: {
                            throw new UnsupportedOperationException(
                                    " Unsupported valueType: " + argObject.getValueType());
                        }
                    }
                    break;
                }
                case LIST:
                case STRUCT: {
                    // Note: when the argNode is text data, toPrettyString output the text data
                    //       if the argNode is binary data, toPrettyString output the
                    // base64-encoded data
                    result.add(argNode.toPrettyString());
                    break;
                }
                default: {
                    throw new UnsupportedOperationException(
                            " Unsupported objectType: " + argObject.getType());
                }
            }
        }

        return result;
    }

    public static Block covertToBlock(EthBlock.Block block) {
        Block stubBlock = new Block();
        stubBlock.setBlockHeader(extractBlockHeader(block));
        List<EthBlock.TransactionResult> transactions = block.getTransactions();
        List<String> transactionsHashes = new ArrayList<>();
        System.out.println("transactions.size()" + transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            EthBlock.TransactionResult transactionHash = transactions.get(i);
            System.out.println(i + ":" + ((EthBlock.TransactionObject) (transactionHash.get())).get().getValueRaw());
            transactionsHashes.add(((EthBlock.TransactionObject) (transactionHash.get())).get().getHash());
        }
        stubBlock.setTransactionsHashes(transactionsHashes);
        //TODO: byte[] rawBytes = objectMapper.writeValueAsBytes(block);
        stubBlock.setRawBytes(new byte[] {});
        return stubBlock;
    }

    private static void errorReport(String path, String expected, String actual)
            throws InvalidParameterException {
        String errorMessage =
                "Arguments mismatch: " + path + ", expected: " + expected + ", actual: " + actual;
        throw new InvalidParameterException(errorMessage);
    }

    private static void errorReport(String errorMessage) {
        throw new InvalidParameterException(errorMessage);
    }

    private static void errorReport(String path, String expected, String actual, String exceptionReason)
            throws InvalidParameterException {
        String errorMessage =
                "Arguments mismatch: "
                        + path
                        + ", expected: "
                        + expected
                        + ", actual: "
                        + actual
                        + ", exception reason:"
                        + exceptionReason;
        throw new InvalidParameterException(errorMessage);
    }

    private static byte[] tryDecodeInputData(String inputData) {
        if (inputData.startsWith("base64://")) {
            return Base64.getDecoder()
                    .decode(inputData.substring("base64://".length()));
        } else if (inputData.startsWith("hex://")) {
            String hexString = inputData.substring("hex://".length());
            if (hexString.startsWith("0x")) {
                return Hex.decode(hexString.substring(2));
            } else {
                return Hex.decode(hexString);
            }
        }
        return null;
    }

    private static ABIObject encodeNode(String path, ABIObject template, JsonNode node) {
        ABIObject abiObject = template.newObject();

        switch (abiObject.getType()) {
            case VALUE: {
                if (!node.isValueNode()) {
                    errorReport(
                            path,
                            abiObject.getType().toString(),
                            node.getNodeType().toString());
                }

                switch (template.getValueType()) {
                    case BOOL: {
                        if (!node.isBoolean()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        abiObject.setBoolValue(new Bool(node.asBoolean()));
                        break;
                    }
                    case INT: {
                        if (!node.isNumber() && !node.isBigInteger()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        if (node.isNumber()) {
                            abiObject.setNumericValue(new Int256(node.asLong()));
                        } else {
                            abiObject.setNumericValue(new Int256(node.bigIntegerValue()));
                        }

                        break;
                    }
                    case UINT: {
                        if (!node.isNumber() && !node.isBigInteger()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        if (node.isNumber()) {
                            abiObject.setNumericValue(new Uint256(node.asLong()));
                        } else {
                            abiObject.setNumericValue(new Uint256(node.bigIntegerValue()));
                        }

                        break;
                    }
                    case ADDRESS: {
                        if (!node.isTextual()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        try {
                            abiObject.setAddressValue(new Address(node.asText()));
                        } catch (Exception e) {
                            errorReport(
                                    "Invalid address value",
                                    template.getValueType().toString(),
                                    node.asText());
                        }
                        break;
                    }
                    case BYTES: {
                        if (!node.isTextual()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }
                        String value = node.asText();
                        byte[] bytesValue = tryDecodeInputData(value);
                        if (bytesValue == null) {
                            bytesValue = value.getBytes();
                        }
                        if (abiObject.getBytesLength() > 0
                                && bytesValue.length != abiObject.getBytesLength()) {
                            errorReport(
                                    "Invalid input bytes, required length: "
                                            + abiObject.getBytesLength()
                                            + ", input data length:"
                                            + bytesValue.length);
                        }
                        abiObject.setBytesValue(new Bytes(bytesValue.length, bytesValue));
                        break;
                    }
                    case DBYTES: {
                        if (!node.isTextual()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }
                        String value = node.asText();
                        byte[] bytesValue = tryDecodeInputData(value);
                        if (bytesValue == null) {
                            bytesValue = value.getBytes();
                        }
                        abiObject.setDynamicBytesValue(new DynamicBytes(bytesValue));
                        break;
                    }
                    case STRING: {
                        if (!node.isTextual()) {
                            errorReport(
                                    path,
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        abiObject.setStringValue(new Utf8String(node.asText()));
                        break;
                    }
                }
                break;
            }
            case LIST: {
                if (!node.isArray()) {
                    errorReport(
                            path,
                            abiObject.getType().toString(),
                            node.getNodeType().toString());
                }

                if ((abiObject.getListType() == ABIObject.ListType.FIXED)
                        && (node.size() != abiObject.getListLength())) {
                    errorReport(
                            "fixed list arguments size",
                            String.valueOf(abiObject.getListLength()),
                            String.valueOf(node.size()));
                }

                int i = 0;
                Iterator<JsonNode> iterator = node.iterator();
                while (iterator.hasNext()) {
                    abiObject
                            .getListValues()
                            .add(
                                    encodeNode(
                                            path + ".<" + String.valueOf(i) + ">",
                                            abiObject.getListValueType(),
                                            iterator.next()));
                }

                break;
            }
            case STRUCT: {
                if (!node.isArray() && !node.isObject()) {
                    errorReport(
                            path,
                            abiObject.getType().toString(),
                            node.getNodeType().toString());
                }

                if (node.size() != abiObject.getStructFields().size()) {
                    errorReport(
                            "struct arguments size",
                            String.valueOf(abiObject.getListLength()),
                            String.valueOf(node.size()));
                }

                if (node.isArray()) {
                    for (int i = 0; i < abiObject.getStructFields().size(); i++) {
                        ABIObject field = abiObject.getStructFields().get(i);
                        abiObject
                                .getStructFields()
                                .set(
                                        i,
                                        encodeNode(
                                                path + "." + field.getName(),
                                                field,
                                                node.get(i)));
                    }
                } else {
                    for (int i = 0; i < abiObject.getStructFields().size(); ++i) {
                        ABIObject field = abiObject.getStructFields().get(i);
                        JsonNode structNode = node.get(field.getName());

                        if (structNode == null) {
                            errorReport(
                                    path + "miss field value, field name: " + field.getName(),
                                    template.getValueType().toString(),
                                    node.getNodeType().toString());
                        }

                        abiObject
                                .getStructFields()
                                .set(
                                        i,
                                        encodeNode(
                                                path + "." + field.getName(),
                                                field,
                                                structNode));
                    }
                }

                break;
            }
        }

        return abiObject;
    }

    public static ABIObject encode(ABIObject template, List<String> inputs) throws IOException {

        ABIObject abiObject = template.newObject();

        // check parameters match
        if (inputs.size() != abiObject.getStructFields().size()) {
            errorReport(
                    "arguments size",
                    String.valueOf(abiObject.getStructFields().size()),
                    String.valueOf(inputs.size()));
        }

        for (int i = 0; i < abiObject.getStructFields().size(); ++i) {
            ABIObject argObject = abiObject.getStructFields().get(i).newObject();
            String value = inputs.get(i);
            switch (argObject.getType()) {
                case VALUE: {
                    try {
                        switch (argObject.getValueType()) {
                            case BOOL: {
                                argObject.setBoolValue(new Bool(Boolean.valueOf(value)));
                                break;
                            }
                            case UINT: {
                                argObject.setNumericValue(
                                        new Uint256(Numeric.decodeQuantity(value)));
                                break;
                            }
                            case INT: {
                                argObject.setNumericValue(
                                        new Int256(Numeric.decodeQuantity(value)));
                                break;
                            }
                            case ADDRESS: {
                                argObject.setAddressValue(new Address(value));
                                break;
                            }
                            case BYTES: {
                                // Binary data requires base64 encoding
                                byte[] bytesValue = tryDecodeInputData(value);
                                if (bytesValue == null) {
                                    bytesValue = value.getBytes();
                                }
                                if (argObject.getBytesLength() > 0
                                        && bytesValue.length
                                        != argObject.getBytesLength()) {
                                    errorReport(
                                            "Invalid input bytes, required length: "
                                                    + argObject.getBytesLength()
                                                    + ", input data length:"
                                                    + bytesValue.length);
                                }
                                argObject.setBytesValue(
                                        new Bytes(bytesValue.length, bytesValue));
                                break;
                            }
                            case DBYTES: {
                                // Binary data requires base64 encoding
                                byte[] bytesValue = tryDecodeInputData(value);
                                if (bytesValue == null) {
                                    bytesValue = value.getBytes();
                                }
                                argObject.setDynamicBytesValue(
                                        new DynamicBytes(bytesValue));
                                break;
                            }
                            case STRING: {
                                argObject.setStringValue(new Utf8String(value));
                                break;
                            }
                            default: {
                                throw new UnsupportedOperationException(
                                        "Unrecognized valueType: "
                                                + argObject.getValueType());
                            }
                        }
                    } catch (Exception e) {
                        errorReport(
                                "ROOT",
                                argObject.getValueType().toString(),
                                value,
                                e.getMessage());
                    }
                    break;
                }
                case STRUCT:
                case LIST: {
                    JsonNode argNode = objectMapper.readTree(value.getBytes());
                    argObject = encodeNode("ROOT", argObject, argNode);
                    break;
                }
            }
            abiObject.getStructFields().set(i, argObject);
        }
        return abiObject;
    }
    public static RawTransaction buildTransaction(
            String contractAddress,
            BigInteger groupId,
            BigInteger chainId,
            BigInteger blockNumber,
            String abi) {
        Random r = ThreadLocalRandom.current();
        BigInteger randomid = new BigInteger(250, r);
        return RawTransaction.createTransaction(
                randomid,
                BigInteger.valueOf(300000000000L),
                BigInteger.valueOf(300000000000L),
                contractAddress,
                BigInteger.ZERO,
                abi);
    }
    public static BlockHeader extractBlockHeader(EthBlock.Block block) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(block.getHash());
        blockHeader.setNumber(block.getNumber().longValue());
        blockHeader.setPrevHash(block.getParentHash());
        blockHeader.setReceiptRoot(block.getReceiptsRoot());
        blockHeader.setStateRoot(block.getStateRoot());
        blockHeader.setTransactionRoot(block.getTransactionsRoot());
        return blockHeader;
    }

    public static ContractABIDefinition loadABI(String abi) {
        try {
            AbiDefinition[] abiDefinitions =
                    ObjectMapperFactory.getObjectMapper().readValue(abi, AbiDefinition[].class);
            ContractABIDefinition contractABIDefinition = new ContractABIDefinition();
            for (AbiDefinition abiDefinition : abiDefinitions) {
                switch (abiDefinition.getType()) {
                    case CONSTRUCTOR_TYPE:
                        contractABIDefinition.setConstructor(abiDefinition);
                        break;
                    case FUNCTION_TYPE:
                        contractABIDefinition.addFunction(abiDefinition.getName(), abiDefinition);
                        break;
                    case EVENT_TYPE:
                        contractABIDefinition.addEvent(abiDefinition.getName(), abiDefinition);
                        break;
                    case FALLBACK_TYPE:
                        if (contractABIDefinition.hasFallbackFunction()) {
                            throw new RuntimeException("only single fallback is allowed");
                        }
                        contractABIDefinition.setFallbackFunction(abiDefinition);
                        break;
                    case RECEIVE_TYPE:
                        if (contractABIDefinition.hasReceiveFunction()) {
                            throw new RuntimeException("only single receive is allowed");
                        }
                        if (!"payable".equals(abiDefinition.getStateMutability())
                                && !abiDefinition.isPayable()) {
                            throw new RuntimeException("the statemutability of receive can only be payable");
                        }
                        contractABIDefinition.setReceiveFunction(abiDefinition);
                        break;
                    default:
                        // skip and do nothing
                        break;
                }
            }
            if (contractABIDefinition.getConstructor() == null) {
                contractABIDefinition.setConstructor(createDefaultConstructorABIDefinition());
            }
            return contractABIDefinition;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static AbiDefinition createDefaultConstructorABIDefinition() {
        return new AbiDefinition(
                false, new ArrayList<>(), null, null, "constructor", false, "nonpayable");
    }
}
