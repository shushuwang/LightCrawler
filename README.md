# LightCrawler
轻量级爬虫工具，快速部署、断点续爬、操作简单，用于垂直爬取需求多百万量级的场景，达到事半功倍的效果。

# 启动爬虫：    
```java
String crawlerpath = "E:\\test\\";
CrawlerExecuteEngine.create()
        .setThreadSize(100)
        .setSleepTimeSeconds(1)
        .setTaskRetry(3)
        .addNextCrawlerTask(new Stepone())
        .addNextCrawlerTask(new Steptwo())
        .addNextCrawlerTask(new Stepthree())
        .setSeedFile(crawlerpath + "seeds.txt")
        .setSaveDirectory(crawlerpath)
        .run();
```
