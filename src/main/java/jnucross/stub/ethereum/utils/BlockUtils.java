package jnucross.stub.ethereum.utils;

import com.webank.wecross.stub.Block;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * @author SDKany
 * @ClassName BlockUtils
 * @Date 2023/7/19 21:09
 * @Version V1.0
 * @Description Block Utils
 */
public class BlockUtils {
    public static Block covertToBlock(EthBlock.Block block){
        Block stubBlock = new Block();
        // TODO: covert EthBlock.Block to WeCross Block
        stubBlock.setBlockHeader(null);

        return stubBlock;
    }
}
