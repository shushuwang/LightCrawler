package main.localTask;

import common.CrawlerExecute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * 爬虫任务
 *
 * @author shushuwang
 * @create 2018-11-21 20:26
 **/
public class CrawlerThread implements Callable<List<String>> {
    private static Random random = new Random();
    private String seed = null;
    private CrawlerExecute execute = null;
    private int sleepTimeSecond = 3;

    public CrawlerThread(CrawlerExecute execute, String seed) {
        this.execute = execute;
        this.seed = seed;
    }
    public CrawlerThread(CrawlerExecute execute, String seed, int sleepTimeSecond) {
        this.execute = execute;
        this.seed = seed;
        this.sleepTimeSecond = sleepTimeSecond;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> result = new ArrayList<>();
        try {
            result = this.execute.execute(seed);
            Thread.sleep(random.nextInt(sleepTimeSecond * 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
