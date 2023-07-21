package jnucross.stub.ethereum;

import com.webank.wecross.stub.Block;
import jnucross.stub.ethereum.utils.BlockUtils;
import jnucross.stub.ethereum.utils.TransactionUtils;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author SDKany
 * @ClassName UtilsTest
 * @Date 2023/7/21 16:36
 * @Version V1.0
 * @Description
 */
public class UtilsTest {
    Web3j web3 = Web3j.build(new HttpService("https://nodes.mewapi.io/rpc/eth"));
    Web3j web3j = Web3j.build(new HttpService("http://10.154.24.12:8545"));

    @Test
    public void BlockUtilsCovertBlockTest() throws IOException {
        EthBlock.Block block = web3.ethGetBlockByNumber(
                DefaultBlockParameter.valueOf(BigInteger.valueOf(17740710L)), true).send().getBlock();

        System.out.println("EthBlock");
        System.out.println(block);

        Block block1 = BlockUtils.covertToBlock(block);
        System.out.println("WeCross Block");
        System.out.println(block1);
    }

    @Test
    public void TransactionUtilsCovertTransactionTest() throws IOException {
        Transaction transaction = web3.ethGetTransactionByHash("0xb02b49d813ef83e74ff8aa109ea343193e00801c121a9cc72623ec0a9492d4d3").send().getTransaction().get();

        System.out.println("EthTransaction");
        System.out.println(transaction);

        com.webank.wecross.stub.Transaction transaction1 = TransactionUtils.covertToTransaction(transaction);
        System.out.println("WeCross Transaction");
        System.out.println(transaction1);
    }

}
