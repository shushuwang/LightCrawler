package main.saveHtml;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtils;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 保存原始页面
 *
 * @author shushuwang
 * @create 2018-12-27 20:14
 **/
public class SaveCrawlerHtmlBody {
    private static final Logger log = LoggerFactory.getLogger(SaveCrawlerHtmlBody.class);

    private ArrayBlockingQueue<HtmlEntity> resultblockingQueue = new ArrayBlockingQueue<HtmlEntity>(3000);

    private final String baseDir = "E:"+ File.separator + "POI_Crawler"+ File.separator + "HTML" + File.separator;

    private SaveCrawlerHtmlBody(){
        writeDataThread();
    }

    private static class SingletonHolder {
        private static final SaveCrawlerHtmlBody INSTANCE = new SaveCrawlerHtmlBody();
    }

    public static SaveCrawlerHtmlBody getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void saveDZDP(HtmlEntity entity) throws InterruptedException {
        resultblockingQueue.put(entity);
    }

    private void writeDataThread() {
        String threadName = "thread-write-html-data";
        Thread writehtmlthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HtmlEntity poll = null;
                    log.info(threadName + " ResultQueue-size:" + resultblockingQueue.size());
                    while ((poll = resultblockingQueue.take()) != null) {
                        try {
                            String url = poll.getUrl();
                            if (Strings.isNullOrEmpty(url)) {
                                continue;
                            }
                            String dirpath = null;
                            if (url.contains("//")) {
                                dirpath = baseDir + url.substring(url.indexOf("//")+2).replaceAll("/","\\"+File.separator);
                            } else {
                                dirpath = baseDir + url.replaceAll("/",File.separator);
                            }
                            FileUtils.writeFile(poll.getHtmlBody(), dirpath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, threadName);
        writehtmlthread.setDaemon(true);
        writehtmlthread.start();
    }
    public static void main(String[] args) throws InterruptedException {

        HtmlEntity entity = new HtmlEntity();
        entity.setHtmlBody("tsetsetset");
        entity.setUrl("http://www.dianping.com/shop/5646160");
        SaveCrawlerHtmlBody.getInstance().saveDZDP(entity);
    }
}
