package jnucross.stub.ethereum.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SDKany
 * @ClassName BlockUtils
 * @Date 2023/7/19 21:09
 * @Version V1.0
 * @Description Block Utils
 */
public class BlockUtils {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static Block covertToBlock(EthBlock.Block block){
        Block stubBlock = new Block();
        stubBlock.setBlockHeader(extractBlockHeader(block));
        List<EthBlock.TransactionResult> transactions = block.getTransactions();
        List<String> transactionsHashes = new ArrayList<>();
        System.out.println("transactions.size()" + transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            EthBlock.TransactionResult transactionHash = transactions.get(i);
            System.out.println(i + ":" + ((EthBlock.TransactionObject)(transactionHash.get())).get().getValueRaw());
            transactionsHashes.add(((EthBlock.TransactionObject)(transactionHash.get())).get().getHash());
        }
        stubBlock.setTransactionsHashes(transactionsHashes);
        //TODO: byte[] rawBytes = objectMapper.writeValueAsBytes(block);
        stubBlock.setRawBytes(null);
        return stubBlock;
    }

    public static BlockHeader extractBlockHeader(EthBlock.Block block){
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(block.getHash());
        blockHeader.setNumber(block.getNumber().longValue());
        blockHeader.setPrevHash(block.getParentHash());
        blockHeader.setReceiptRoot(block.getReceiptsRoot());
        blockHeader.setStateRoot(block.getStateRoot());
        blockHeader.setTransactionRoot(block.getTransactionsRoot());
        return  blockHeader;
    }
}
