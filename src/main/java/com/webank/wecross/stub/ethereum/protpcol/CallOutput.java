package com.webank.wecross.stub.ethereum.protpcol;

import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Objects;
public class CallOutput {
    private String currentBlockNumber;
    private String status;
    private String output;

    public BigInteger getCurrentBlockNumber() {
        return Numeric.decodeQuantity(currentBlockNumber);
    }

    public void setCurrentBlockNumber(String currentBlockNumber) {
        this.currentBlockNumber = currentBlockNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallOutput that = (CallOutput) o;
        return Objects.equals(
                Numeric.decodeQuantity(currentBlockNumber),
                Numeric.decodeQuantity(that.currentBlockNumber))
                && Objects.equals(status, that.status)
                && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Numeric.decodeQuantity(currentBlockNumber), status, output);
    }

    @Override
    public String toString() {
        return "CallOutput{"
                + "currentBlockNumber='"
                + currentBlockNumber
                + '\''
                + ", status='"
                + status
                + '\''
                + ", output='"
                + output
                + '\''
                + '}';
    }
}