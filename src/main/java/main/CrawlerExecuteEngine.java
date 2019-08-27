package main;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.*;
import common.CrawlerExecute;
import main.localTask.CrawlerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 爬虫执行调度器
 *
 * @author shushuwang
 * @create 2018-11-24 11:56
 **/
public class CrawlerExecuteEngine {
    private static final Logger log = LoggerFactory.getLogger(CrawlerExecuteEngine.class);

    private int n_parallel_nodes = 20;
    private List<CrawlerExecute> crawlerTaskList = new ArrayList<>();
    private String seedFile = null;
    private String saveDirectory = null;
    private ListeningExecutorService executor = null;
    private final String saveResultFileName = File.separator + "result.txt";
    private final String failSeedstFileName = File.separator + "failSeeds.txt";
    private final String successSeedstFileName = File.separator + "successSeeds.txt";
    private final String tmpSeedstFileName = File.separator + "tmpseeds.txt";
    private int sleepTimeSeconds = 1;
    private int currentStep = 1;
    private int taskRetry = 1;
    private String resultFile = null;
    private String failureFile = null;
    private String successFile = null;
    private ArrayBlockingQueue<String> resultblockingQueue = new ArrayBlockingQueue<String>(5000);
    private ArrayBlockingQueue<String> failureblockingQueue = new ArrayBlockingQueue<String>(2000);
    private ArrayBlockingQueue<String> successblockingQueue = new ArrayBlockingQueue<String>(2000);

    public static CrawlerExecuteEngine create() { return new CrawlerExecuteEngine();}

    /**
     * 一个线程中的任务执行间隔
     * @param sleepTimeSeconds
     * @return
     */
    public CrawlerExecuteEngine setSleepTimeSeconds(int sleepTimeSeconds) {
        this.sleepTimeSeconds = sleepTimeSeconds;
        return this;
    }

    /**
     * 任务失败重试次数
     * @param n
     * @return
     */
    public CrawlerExecuteEngine setTaskRetry(int n) {
        this.taskRetry = n;
        return this;
    }

    /**
     * 设置爬虫结果保存路径
     * @param saveDirectory
     * @return
     */
    public CrawlerExecuteEngine setSaveDirectory(String saveDirectory){
        this.saveDirectory = saveDirectory;
        return this;
    }
    /**
     * 添加爬虫任务类
     * @param crawlerTask
     * @return
     */
    public CrawlerExecuteEngine addNextCrawlerTask(CrawlerExecute crawlerTask) {
        this.crawlerTaskList.add(crawlerTask);
        return this;
    }
    /**
     * 设置线程数
     * @param size
     * @return
     */
    public CrawlerExecuteEngine setThreadSize(int size) {
        this.n_parallel_nodes = size;
        return this;
    }
    /**
     * 设置种子文件路径
     * @param filePath
     * @return
     */
    public CrawlerExecuteEngine setSeedFile(String filePath) {
        this.seedFile = filePath;
        return this;
    }

    /**
     * 运行爬虫程序
     * @throws IOException
     * @throws InterruptedException
     */
    public void run() throws IOException, InterruptedException {
        for (int i = 0; i < taskRetry; i++) {
            readConfig();
        }
    }

    private void readConfig() throws IOException, InterruptedException {
        if (crawlerTaskList.size() == 0) {
            log.error("请添加爬取任务,调用addNextCrawlerTask！");
            return;
        }

        if (Strings.isNullOrEmpty(saveDirectory)) {
            log.error("请设置数据保存目录！");
            return;
        }
        if (!saveDirectory.endsWith(File.separator)) {
            saveDirectory = saveDirectory+File.separator;
        }
        executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(n_parallel_nodes));
        String tmpseeds = null;
        boolean needUpdateNextTmpseeds = true;
        for (int i = 1; i <= crawlerTaskList.size(); i++) {
            currentStep = i;
            resultFile = saveDirectory + i + saveResultFileName;
            failureFile = saveDirectory + i + failSeedstFileName;
            successFile = saveDirectory + i + successSeedstFileName;
            tmpseeds = saveDirectory + i + tmpSeedstFileName;
            boolean resultExit = FileUtils.isExist(resultFile);
            boolean failSeedsExist = FileUtils.isExist(failureFile);
            if (i > 1 && needUpdateNextTmpseeds) {
                String lastResult = saveDirectory + (i-1) + saveResultFileName;
                if (FileUtils.isExist(lastResult)) {
                    FileUtils.copyTo(lastResult, tmpseeds);
                }
            }
            boolean tmpSeedsExist = FileUtils.isExist(tmpseeds);
            if (failSeedsExist && !tmpSeedsExist) {
                FileUtils.moveTo(failureFile, tmpseeds);
                needUpdateNextTmpseeds = true;
            }
            tmpSeedsExist = FileUtils.isExist(tmpseeds);
            if (resultExit && !tmpSeedsExist) {
                needUpdateNextTmpseeds = false;
                continue;
            }else if (tmpSeedsExist) {
                needUpdateNextTmpseeds = true;
            } else {
                if (i == 1) {
                    FileUtils.copyTo(seedFile, tmpseeds);
                    needUpdateNextTmpseeds = true;
                } else if (i > 1) {
                    String lastResult = saveDirectory + (i-1) + saveResultFileName;
                    FileUtils.copyTo(lastResult, tmpseeds);
                    needUpdateNextTmpseeds = true;
                }
            }
            failSeedsExist = FileUtils.isExist(failureFile);
            if (failSeedsExist) {
                FileUtils.deleteFile(failureFile);
            }
            log.info(">>>种子： " + tmpseeds);
            log.info(">>>结果： " + resultFile);
            log.info(">>>失败： " + failureFile);
            log.info(">>>成功： " + successFile);
            Thread.sleep(3000);
            run(tmpseeds, successFile, crawlerTaskList.get(i-1));
            FileUtils.deleteFile(tmpseeds);
        }
        currentStep = 100;//make daemon write thread run finishing
        executor.shutdown();
        Thread.sleep(5000);
    }

    private void run(String seedFile, String successFile, CrawlerExecute crawlerTask) throws IOException, InterruptedException {
        writeData("crawlerExecuteEngine-writedata-thread-step-"+currentStep);
        /* 种子文件*/
        List<String> seeds = FileUtils.readFile(seedFile);
        List<String> successseedsList = FileUtils.readFile(successFile);
        Set<String> successSeedsSet =  new HashSet<>(successseedsList);
        successseedsList.clear();
        successseedsList = null;
        final CountDownLatch latch = new CountDownLatch(seeds.size());
        AtomicInteger successNumInPeriod = new AtomicInteger(0);
        log.info(">>>>>>>>>>>>start>>>>>>>>>>>>");
        for (String s : seeds) {
            if (successSeedsSet.contains(s)) {
                latch.countDown();
                continue;
            }
            final String seed = s;
            successSeedsSet.add(s);
            CrawlerThread task = new CrawlerThread(crawlerTask, seed, this.sleepTimeSeconds);
            ListenableFuture<List<String>> future = executor.submit(task);
            Futures.addCallback(future, new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    try {
                        if (result == null) {
//                            failureblockingQueue.put(seed);
                            successblockingQueue.put(seed);
                            successNumInPeriod.incrementAndGet();
                            return;
                        }
                        List<String> resultlist = (List<String>) result;
                        if (resultlist.isEmpty()) {
                            failureblockingQueue.put(seed);
                            return;
                        }
                        for (String r : resultlist) {
                            resultblockingQueue.put(r);
                        }
                        successblockingQueue.put(seed);
                        successNumInPeriod.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    try {
                        failureblockingQueue.put(seed);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
//        latch.await();
        int countFailure = 0;// System exit if no seed success in 100 seconds
        while (latch.getCount() > 0) {
            Thread.sleep(10000);
            if (successNumInPeriod.get() < 1) {
                countFailure++;
            } else {
                countFailure = 0;
            }
            if (countFailure > 20) {
                log.error(">>>>>>>>>>>>system exit>>>>>>>>>>>>" + new Date().toString());
                System.exit(1);
            }
            successNumInPeriod.lazySet(0);
        }
        while (resultblockingQueue.peek() != null || failureblockingQueue.peek() != null || successblockingQueue.peek() != null) {
            Thread.sleep(2000);
        }
        Thread.sleep(5000);
        log.info(">>>>>>>>>>>>end>>>>>>>>>>>>" + new Date().toString());
    }


    private void writeData(final String threadName) {
        Thread writefailurethread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final int step = currentStep;
                    List<String> datalist = new ArrayList<>();
                    String poll = null;
                    while (step == currentStep) {
                        log.info(threadName + " writeResultDir:" + resultFile + " ResultQueue-size:" + resultblockingQueue.size());
                        while ((poll = resultblockingQueue.poll()) != null) {
                            datalist.add(poll);
                        }
                        if (datalist.size() > 0) {
                            FileUtils.writeFile(datalist,resultFile);
                            datalist.clear();
                        }
                        log.info(threadName + " writeFailDir:" + failureFile + " FailureQueue-size:"+ failureblockingQueue.size());
                        while ((poll = failureblockingQueue.poll()) != null) {
                            datalist.add(poll);
                        }
                        if (datalist.size() > 0) {
                            FileUtils.writeFile(datalist, failureFile);
                            datalist.clear();
                        }
                        log.info(threadName + " writeSuccessDir:" + successFile + " SucessQueue-size:" + successblockingQueue.size());
                        while ((poll = successblockingQueue.poll()) != null) {
                            datalist.add(poll);
                        }
                        if (datalist.size() > 0) {
                            FileUtils.writeFile(datalist,successFile);
                            datalist.clear();
                        }

                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },threadName);
        writefailurethread.setDaemon(true);
        writefailurethread.start();
    }
}
