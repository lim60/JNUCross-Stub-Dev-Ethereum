package com.webank.wecross.stub.ethereum.wrapper;


import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webank.wecross.stub.ethereum.common.ABIObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.utils.Numeric;

public class ABIObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(ABIObjectFactory.class);

    public static ABIObject createInputObject(AbiDefinition abiDefinition) {
        return createObject(
                abiDefinition.getName(), abiDefinition.getType(), abiDefinition.getInputs());
    }

    public static ABIObject createOutputObject(AbiDefinition abiDefinition) {
        return createObject(
                abiDefinition.getName(), abiDefinition.getType(), abiDefinition.getOutputs());
    }

    private static ABIObject createObject(
            String name, String type, List<AbiDefinition.NamedType> namedTypes) {
        try {
            ABIObject abiObject = new ABIObject(ABIObject.ObjectType.STRUCT);

            for (AbiDefinition.NamedType namedType : namedTypes) {
                abiObject.getStructFields().add(buildTypeObject(namedType));
            }

            logger.info(" name: {}", name);

            return abiObject;

        } catch (Exception e) {
            logger.error("namedTypes: {},  e: ", namedTypes, e);
        }

        return null;
    }

    public static ABIObject createEventInputObject(AbiDefinition abiDefinition) {
        return creatEventObjectWithOutIndexed(abiDefinition.getInputs());
    }

    public static ABIObject creatEventObjectWithOutIndexed(
            List<AbiDefinition.NamedType> namedTypes) {
        try {
            ABIObject abiObject = new ABIObject(ABIObject.ObjectType.STRUCT);

            for (AbiDefinition.NamedType namedType : namedTypes) {
                if (!namedType.isIndexed()) {
                    abiObject.getStructFields().add(buildTypeObject(namedType));
                }
            }
            return abiObject;
        } catch (Exception e) {
            logger.error("namedTypes: {},  e: ", namedTypes, e);
        }
        return null;
    }

    /**
     * build ABIObject by raw type name
     *
     * @param rawType the rawType of the object
     * @return the built ABIObject
     */
    public static ABIObject buildRawTypeObject(String rawType) {

        ABIObject abiObject = null;

        if (rawType.startsWith("uint")) {
            abiObject = new ABIObject(ABIObject.ValueType.UINT);
        } else if (rawType.startsWith("int")) {
            abiObject = new ABIObject(ABIObject.ValueType.INT);
        } else if (rawType.startsWith("bool")) {
            abiObject = new ABIObject(ABIObject.ValueType.BOOL);
        } else if (rawType.startsWith("string")) {
            abiObject = new ABIObject(ABIObject.ValueType.STRING);
        } else if (rawType.equals("bytes")) {
            abiObject = new ABIObject(ABIObject.ValueType.DBYTES);
        } else if (rawType.startsWith("bytes")) {
            try {
                BigInteger bytesLength =
                        Numeric.decodeQuantity(rawType.substring("bytes".length()));
                abiObject = new ABIObject(ABIObject.ValueType.BYTES, bytesLength.intValue());
            } catch (Exception e) {
                abiObject = new ABIObject(ABIObject.ValueType.BYTES);
            }
        } else if (rawType.startsWith("address")) {
            abiObject = new ABIObject(ABIObject.ValueType.ADDRESS);
        } else if (rawType.startsWith("fixed") || rawType.startsWith("ufixed")) {
            throw new UnsupportedOperationException("Unsupported type:" + rawType);
        } else {
            throw new UnsupportedOperationException("Unrecognized type:" + rawType);
        }

        return abiObject;
    }

    private static ABIObject buildTupleObject(AbiDefinition.NamedType namedType) {
        return createObject(namedType.getName(), namedType.getType(), namedType.getComponents());
    }

    /*private static ABIObject buildListObject(
            AbiDefinition.Type typeObj, AbiDefinition.NamedType namedType) {
        ABIObject abiObject = null;
        if (typeObj.isList()) {
            ABIObject listObject = new ABIObject(ABIObject.ObjectType.LIST);
            listObject.setListType(
                    typeObj.isFixedList() ? ABIObject.ListType.FIXED : ABIObject.ListType.DYNAMIC);
            if (typeObj.isFixedList()) {
                listObject.setListLength(typeObj.getLastDimension());
            }

            listObject.setListValueType(
                    buildListObject(typeObj.reduceDimensionAndGetType(), namedType));
            abiObject = listObject;
        } else if (typeObj.getRawType().startsWith("tuple")) {
            abiObject = buildTupleObject(namedType);
        } else {
            abiObject = buildRawTypeObject(typeObj.getRawType());
        }

        return abiObject;
    }*/

    public static ABIObject buildTypeObject(AbiDefinition.NamedType namedType) {
        try {
            String type = namedType.getType();
            // String name = namedType.getName();
            // boolean indexed = namedType.isIndexed();
            int index = type.indexOf('[');
            String rawType = (-1 == index) ? type.trim() : type.substring(0, index);
            ABIObject abiObject;
            //todo:不支持list
            if (rawType.startsWith("tuple")) {
                abiObject = buildTupleObject(namedType);
            } else {
                abiObject = buildRawTypeObject(rawType);
            }
            abiObject.setName(namedType.getName());
            return abiObject;
        } catch (Exception e) {
            logger.error(" e: ", e);
        }
        return null;
    }
}
