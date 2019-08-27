package business;

import common.CrawlerExecute;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shushuwang
 * @create 2019-03-06 17:14
 **/
public class Stepthree extends CrawlerExecute {
    @Override
    public List<String> execute(String taskDetail) throws Exception {
        List<String> result = new ArrayList<>();
        //处理逻辑
        result.add(" >>stepthree" + taskDetail);
        return result;
    }

    @Override
    public int changeMsgCode() {
        return 0;
    }

    @Override
    public String getResultMark() {
        return null;
    }
}
