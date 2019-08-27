package IPPoolProxy;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.NumberUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * IP 代理切换
 *
 * @author shushuwang
 * @create 2018-11-15 15:43
 **/
public class IPFactory {
    private static final Logger log = LoggerFactory.getLogger(IPFactory.class);

    private static BlockingQueue<Long> ipqueue = new LinkedBlockingQueue<Long>(30);
    private static List<String> ips = null;
    private static Set<String> distinctIPs = new HashSet<>();
    private static Set<String> usingIPs = new HashSet<>();
    private static Random random = new Random();
    private static int ipnum = 0;
    static {
        ips = new ArrayList<>();
        try {
            getXiguaDaili();
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        Thread getip = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        getXiguaDaili();
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"get-xiguadaili-thread");
        getip.setDaemon(true);
        getip.start();
    }

    private static void getXiguaDaili() throws IOException {
        if (ips.size() > 300) {
            return;
        }
//        URL url1 = new URL("http://api3.xiguadaili.com/ip/?tid=555221568009460&num=100&&category=2&sortby=time");
//        URL url1 = new URL("http://vip22.xiguadaili.com/ip/?tid=555221568009460&num=1000&category=2&sortby=time");
        // https 专用协议
//        URL url1 = new URL("http://vip22.xiguadaili.com/ip/?tid=555221568009460&num=1000&category=2&sortby=time&protocol=https");
//        URL url1 = new URL("http://vip22.xiguadaili.com/ip/?tid=556910730107815&num=1000&category=2&sortby=time&protocol=https");
        URL url1 = new URL("http://api3.xiguadaili.com/ip/?tid=556910730107815&num=5000&category=2&sortby=time&protocol=https");
        HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(8000);
        urlConnection.setReadTimeout(8000);
        urlConnection.connect();
        InputStream inStrm = urlConnection.getInputStream();
        InputStreamReader isr = new InputStreamReader(inStrm, "UTF-8");
        BufferedReader bufferReader = new BufferedReader(isr);
        String inputLine = "";
        while ((inputLine = bufferReader.readLine()) != null) {
            ips.add(inputLine);
            ipnum++;
        }
        log.info(">>>>> 获取西瓜代理总数量: "+ipnum);
    }

    public synchronized static void removeUsingIp(String proxyIp) {
        usingIPs.remove(proxyIp);
    }

    public synchronized static IP getunUsedIP() {
        String unUsedIP = getRandomIP();
        log.info("Running IP size: ===> " + usingIPs.size());
        int i = 0;
        while (usingIPs.contains(unUsedIP) && i < 30) {
            unUsedIP = getRandomIP();
            i++;
        }
        distinctIPs.add(unUsedIP);
        usingIPs.add(unUsedIP);
        log.info("get one unUsedIP proxy: " + unUsedIP);
        String[] split = unUsedIP.split(":");
        IP ip = new IP();
        ip.setHost(split[0]);
        ip.setPort(NumberUtil.parseInt(split[1]));
        return ip;
    }
    public static String getRandomIP() {
        log.info(">>>>> 获取西瓜代理总数量: "+ipnum + " >>> 使用过(去重后)IP总数量: "+ distinctIPs.size() + " >>> 当前IP池数量: "+ips.size());
        String oneip = null;

        if (ips.size() == 0) {
            try {
                getXiguaDaili();
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        }
        if (ips.size() > 0) {
            oneip = ips.get(0);
            ips.remove(oneip);
        }
        if (Strings.isNullOrEmpty(oneip)) {
            return null;
        }
        return oneip;
    }
    public static void main(String[] args) throws Exception {

    }
}
