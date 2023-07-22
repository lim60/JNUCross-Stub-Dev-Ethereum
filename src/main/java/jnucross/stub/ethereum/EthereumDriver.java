package jnucross.stub.ethereum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import jnucross.stub.ethereum.account.EthereumAccount;
import jnucross.stub.ethereum.common.EthereumType;
import jnucross.stub.ethereum.utils.BlockUtils;
import jnucross.stub.ethereum.utils.TransactionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.bouncycastle.util.BigIntegers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.*;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EthereumDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(EthereumDriver.class);
    public static final BigInteger ONE = BigInteger.valueOf(1);
    public static final BigInteger EIGHT = BigInteger.valueOf(8);

    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {
        int requestType = request.getType();
        /** check if transaction request */
        if ((requestType != EthereumType.ConnectionMessage.ETHEREUM_CALL)
                && (requestType != EthereumType.ConnectionMessage.ETHEREUM_SEND_TRANSACTION)) {
            return new ImmutablePair<>(false, null);
        }




        return null;
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        return null;
    }

    @Override
    public void asyncCall(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {

    }

    @Override
    public void asyncSendTransaction(TransactionContext context, TransactionRequest request, boolean byProxy, Connection connection, Callback callback) {

    }

    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_NUMBER, "");
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), -1);
                    } else {
                        BigInteger blockNumber = new BigInteger(response.getData());
                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, blockNumber.longValue());
                    }
                });
    }

    @Override
    public void asyncGetBlock(long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_BLOCK_BY_NUMBER, "");
        HashMap hashMap = new HashMap<>();
        hashMap.put("blockNumber", blockNumber);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setProperties(hashMap);
        request.setResourceInfo(resourceInfo);
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        EthBlock.Block block = null;
                        try {
                            block = objectMapper.readValue(response.getData(), EthBlock.Block.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, BlockUtils.covertToBlock(block));
                    }
                });
    }

    @Override
    public void asyncGetTransaction(
            String transactionHash,
            long blockNumber,
            BlockManager blockManager,
            boolean isVerified,
            Connection connection,
            GetTransactionCallback callback) {
        Request request = Request.newRequest(EthereumType.ConnectionMessage.ETHEREUM_GET_TRANSACTION, "");
        HashMap hashMap = new HashMap<>();
        hashMap.put("blockNumber", blockNumber);
        hashMap.put("transactionHash", transactionHash);
        hashMap.put("isVerified", isVerified);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setProperties(hashMap);
        request.setResourceInfo(resourceInfo);
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        ObjectMapper objectMapper = new ObjectMapper();
                        org.web3j.protocol.core.methods.response.Transaction transaction = null;
                        try {
                            transaction = objectMapper.readValue(response.getData(), org.web3j.protocol.core.methods.response.Transaction.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        com.webank.wecross.stub.Transaction weCrossTransaction = TransactionUtils.covertToTransaction(transaction);
                        logger.debug("get transaction by transactionHash : {}, transaction : {}", transactionHash, weCrossTransaction.toString());
                        callback.onResponse(null, weCrossTransaction);
                    }
                });
    }

    @Override
    public void asyncCustomCommand(String command, Path path, Object[] args, Account account, BlockManager blockManager, Connection connection, CustomCommandCallback callback) {
        //TODO: 定制化请求，应该可以不实现
    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        ECKeyPair ecKeyPair = ((EthereumAccount)account).getEcKeyPair();
        byte[] hashMsg = Hash.sha3(message);
        Sign.SignatureData signatureData = Sign.signMessage(hashMsg, ecKeyPair, false);
        return concatenateSignature(signatureData.getR(), signatureData.getS(), signatureData.getV());
    }

    public static byte[] concatenateSignature(byte[] r, byte[] s, byte[] v) {
        byte[] signature = new byte[65];
        System.out.println("r.length : " + r.length);
        System.out.println("s.length : " + s.length);
        System.out.println("v.length : " + v.length);
        System.arraycopy(r, 0, signature, 0, r.length);
        System.arraycopy(s, 0, signature, 32, s.length);
        signature[64] = v[0];
        return signature;
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        byte[] hashMsg = Hash.sha3(message);
        byte[] r = Arrays.copyOfRange(signBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(signBytes, 32, 64);
        byte v = signBytes[64];
        int recId = v  - 27;
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        BigInteger recoverPubKey = Sign.recoverFromSignature(recId, sig, hashMsg);
        String recoverAddress = Keys.getAddress(recoverPubKey);
        return Numeric.toBigInt(recoverAddress).equals(Numeric.toBigInt(identity));
    }
}
