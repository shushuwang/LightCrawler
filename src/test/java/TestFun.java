import business.Stepone;
import business.Stepthree;
import business.Steptwo;
import main.CrawlerExecuteEngine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * test have fun
 *
 * @author shushuwang
 * @create 2019-03-06 17:05
 **/
public class TestFun {
    private static final Logger log = LoggerFactory.getLogger(TestFun.class);

    @Test
    public void test() throws IOException, InterruptedException {
        String crawlerpath = "E:\\test\\";
        CrawlerExecuteEngine.create()
                .setThreadSize(10)
                .setSleepTimeSeconds(1)
                .setTaskRetry(3)
                .addNextCrawlerTask(new Stepone())
                .addNextCrawlerTask(new Steptwo())
                .addNextCrawlerTask(new Stepthree())
                .setSeedFile(crawlerpath + "seeds.txt")
                .setSaveDirectory(crawlerpath)
                .run();
    }
}
