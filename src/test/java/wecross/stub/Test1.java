package wecross.stub;

import org.junit.Test;

/**
 * @author SDKany
 * @ClassName Test
 * @Date 2023/8/16 22:35
 * @Version V1.0
 * @Description
 */
public class Test1 {

    @Test
    public void Test1(){
        String string = "a.b.123";
        System.out.println(string.split("\\.")[2]);
    }
}
