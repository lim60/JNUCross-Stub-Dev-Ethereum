package com.webank.wecross.stub.ethereum.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.utils.Numeric;

public class ContractABIDefinition {

    private static final Logger logger = LoggerFactory.getLogger(ContractABIDefinition.class);

    private AbiDefinition constructor = null;
    private Map<String, List<AbiDefinition>> functions = new HashMap<>();
    private Map<String, List<AbiDefinition>> events = new HashMap<>();
    // method id => function
    private Map<String, AbiDefinition> methodIDToFunctions = new HashMap<>();
    private AbiDefinition fallbackFunction;
    private AbiDefinition receiveFunction;
    // event topic => event
    private Map<String, AbiDefinition> eventTopicToEvents = new HashMap<>();

    public ContractABIDefinition() {
    }

    public AbiDefinition getConstructor() {
        return constructor;
    }

    public void setConstructor(AbiDefinition constructor) {
        this.constructor = constructor;
    }

    public Map<String, List<AbiDefinition>> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, List<AbiDefinition>> functions) {
        this.functions = functions;
    }

    public Map<String, List<AbiDefinition>> getEvents() {
        return events;
    }

    public void setEvents(Map<String, List<AbiDefinition>> events) {
        this.events = events;
    }

    public Map<String, AbiDefinition> getMethodIDToFunctions() {
        return methodIDToFunctions;
    }

    public void setMethodIDToFunctions(Map<String, AbiDefinition> methodIDToFunctions) {
        this.methodIDToFunctions = methodIDToFunctions;
    }

    public Map<String, AbiDefinition> getEventTopicToEvents() {
        return eventTopicToEvents;
    }

    public void setEventTopicToEvents(Map<String, AbiDefinition> eventTopicToEvents) {
        this.eventTopicToEvents = eventTopicToEvents;
    }

    public static String getMethodSignatureAsString(AbiDefinition abiDefinition) {
        String name = abiDefinition.getName();
        List<AbiDefinition.NamedType> inputs = abiDefinition.getInputs();
        StringBuilder result = new StringBuilder();
        // Fix: the name field of the fallback is empty
        if (name != null) {
            result.append(name);
        }
        result.append("(");
        if (inputs != null) {
            String params = inputs.stream()
                            .map(ContractABIDefinition::getTypeAsString)
                            .collect(Collectors.joining(","));
            result.append(params);
        }
        result.append(")");
        return result.toString();
    }

    private static String getTupleRawTypeAsString(List<AbiDefinition.NamedType> components) {
        StringBuilder result = new StringBuilder();
        String params =
                components.stream()
                        .map(ContractABIDefinition::getTypeAsString)
                        .collect(Collectors.joining(","));
        result.append(params);
        return result.toString();
    }

    private static String getTypeAsString(AbiDefinition.NamedType abiDefinition) {
        // not tuple, return
        String type = abiDefinition.getType();
        if (!type.startsWith("tuple")) {
            return type;
        }
        String tupleRawString = getTupleRawTypeAsString(abiDefinition.getComponents());
        return type.replaceAll("tuple", "(" + tupleRawString + ")");
    }

    public void addFunction(String name, AbiDefinition abiDefinition) {

        List<AbiDefinition> abiDefinitions = functions.get(name);
        if (abiDefinitions == null) {
            functions.put(name, new ArrayList<>());
            abiDefinitions = functions.get(name);
        } else {
            logger.info(" overload method ??? name: {}, abiDefinition: {}", name, abiDefinition);
        }
        abiDefinitions.add(abiDefinition);

        // calculate method id and add abiDefinition to methodIdToFunctions
        String methodId = getMethodSignatureAsString(abiDefinition);
        methodIDToFunctions.put(methodId, abiDefinition);

        logger.info(
                " name: {}, methodId: {}, methodSignature: {}, abi: {}",
                name,
                methodId,
                getMethodSignatureAsString(abiDefinition),
                abiDefinition);
    }

    private String getEventTopic(AbiDefinition abiDefinition) {
        // from EventEncoder get eventTopic signature hash not substring
        return EventEncoder.buildEventSignature(getMethodSignatureAsString(abiDefinition));
    }

    public void addEvent(String name, AbiDefinition abiDefinition) {
        events.putIfAbsent(name, new ArrayList<>());
        List<AbiDefinition> abiDefinitions = events.get(name);
        abiDefinitions.add(abiDefinition);
        logger.info(" name: {}, abi: {}", name, abiDefinition);
        EventEncoder.buildEventSignature(getMethodSignatureAsString(abiDefinition));
        // calculate event topic and add abiDefinition to eventTopicToEvents
        String eventTopic = getEventTopic(abiDefinition);
        eventTopicToEvents.put(eventTopic, abiDefinition);
    }

    public AbiDefinition getABIDefinitionByMethodId(String methodId) {
        return methodIDToFunctions.get(Numeric.prependHexPrefix(methodId));
    }

    public AbiDefinition getABIDefinitionByEventTopic(String eventTopic) {
        return eventTopicToEvents.get(Numeric.prependHexPrefix(eventTopic));
    }

    public AbiDefinition getFallbackFunction() {
        return fallbackFunction;
    }

    public void setFallbackFunction(AbiDefinition fallbackFunction) {
        this.fallbackFunction = fallbackFunction;
    }

    public boolean hasFallbackFunction() {
        return this.fallbackFunction != null;
    }

    public boolean hasReceiveFunction() {
        return this.receiveFunction != null;
    }

    public AbiDefinition getReceiveFunction() {
        return receiveFunction;
    }

    public void setReceiveFunction(AbiDefinition receiveFunction) {
        this.receiveFunction = receiveFunction;
    }
}

