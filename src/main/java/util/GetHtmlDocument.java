package util;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import common.AbstractClient;
import ClientProxy.LocalClient;
import ClientProxy.ResidentialClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shushuang on 15-8-25.
 */
public class GetHtmlDocument {
    private static final Logger log = LoggerFactory.getLogger(GetHtmlDocument.class);

    private static HttpHost proxy = new HttpHost("proxy.superproxy.io", 22225);
    private static Executor httpExecutor = Executor.newInstance()
            .auth(proxy, "lum-customer-zone-residential-country-cn", "50e34xxxxx");

    private static Pattern htmlTitlePattern = Pattern.compile("<title>.*?</title>");
    private static Random rng = new Random();

    private static AbstractClient getHttpClient() {
//        IPClient client = new IPClient();
//        LPMClient client = new LPMClient();
        LocalClient client = new LocalClient();
//        ResidentialClient client = new ResidentialClient();
        return client;
    }

    public static String getHtmlByHttpClientWithProxy(String url, HttpClientContext context, Map<String, String> headers) throws IOException, IPBlockException, KeyManagementException, NoSuchAlgorithmException {
        AbstractClient client = getHttpClient();
        return getHtmlByHttpClientWithProxy(client, url, context, headers, 3);
    }

    public static String getHtmlByHttpClientWithProxy(String url, HttpClientContext context, Map<String, String> headers, String data) throws IOException, IPBlockException, KeyManagementException, NoSuchAlgorithmException {
        AbstractClient client = getHttpClient();
        return sendRequest(RequestMethod.POST, client, url, context, headers,data, 3);
    }

    public static String getHtmlByHttpClientWithProxy(AbstractClient client, String url, HttpClientContext context, Map<String, String> headers, int retry) throws IOException, IPBlockException, KeyManagementException, NoSuchAlgorithmException {
        return sendRequest(RequestMethod.GET, client, url, context, headers, "",retry);
    }
    public static String sendRequest(RequestMethod method, AbstractClient client, String url, HttpClientContext context, Map<String, String> headers, String data, int retry) throws IOException, IPBlockException, KeyManagementException, NoSuchAlgorithmException {
        String html = null;
        AtomicInteger at_req = new AtomicInteger(0);
        if (retry <= 0) retry = 1;
        while (at_req.getAndAdd(1) <= retry) {
            CloseableHttpResponse response = null;
            try {
                switch (method) {
                    case GET:
                        response = client.requestWithProxy(url, context, headers);
                        break;
                    case POST:
                        response = client.postWithProxy(url, context, headers, data);
                        break;
                }
                int code = response.getStatusLine().getStatusCode();
                String title = null;
                try {
                    html = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
                    Matcher htmlTitleMatcher = htmlTitlePattern.matcher(html);
                    title = htmlTitleMatcher.find() ? htmlTitleMatcher.group() : null;
                } catch (IOException e) {
                    log.error("1 >>> " + e.getMessage());
                } catch (ParseException e) {
                    log.error("2 >>> " + e.getMessage());
                }
                log.info("status code:" +code +" url:"+url+" title: "+title);
                if (code == 403) {
                    html = "";
                } else if (code == 404) {
                    html = "404";
                    break;
                } else if (code == 200) {
                    if (title != null && (title.isEmpty() || title.contains("验证") || title.contains("页面不存在"))) {
                        html = "";
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("3 >>> " + e.getMessage());
            } finally {
                try {
                    if (response != null)
                        response.close();
                } catch (Exception e) {}
            }
            client.handle_response(null);
        }
        client.close();
        return html;
    }

    public static String getRandomDpid(int length)
    {
        Random random = new Random();
        String[] arr = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9"};
        String s = "";
        for(int i = 0; i < length; i++)
        {
            int j = random.nextInt(62);
            s = s + arr[j];
        }
        return s;
    }
    public static Connection.Response  getDzdpWechatJson(String url) throws IOException {
        // url = "https://m.dianping.com/wxmapi/shop/shopmapinfo?shopId=95883815"
        Connection.Response response = null;
//        IPClient client = new IPClient();
//        ResidentialClient client=new ResidentialClient();
//        LPMClient client=new LPMClient();
        response = Jsoup.connect(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .proxy("127.0.0.1",24000)
//                .proxy(client.host,client.port)
                .timeout(5000)
                .header("charset", "utf-8")
                .header("Accept-Encoding", "gzip")
                .header("platformversion", "4.4.2")
                .header("content-type", "application/json")
                .header("platform", "Android")
                .header("minaname", "dianping-wxapp")
                .header("token", "")
                .header("minaversion", "3.8.3")
                .header("sdkversion", "2.2.2")
                .header("wechatversion", "6.6.7")
//                .header("openid", "bojD6hC_secp4KnThwgfbHettD4vktkorRHnRX1vkAw")
                .header("openid", getRandomDpid(43))
                .header("referer", "https://servicewechat.com/wx734c1ad7b3562129/64/page-frame.html")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; mate8 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36 MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN")
                //.header("Host", "itrip.meituan.com/")
                .header("Connection", "Keep-Alive")
                .execute();

        return response;
    }


    public static Connection.Response getResponse(String url,String host, String reffer) throws IOException {
        return getResponse(url, host, reffer, null);
    }
    public static Connection.Response getResponse(String url,String host) throws IOException {
        return getResponse(url, host, null, null);
    }
    public static Connection.Response getResponse2(String url,String host, String cookie) throws IOException {
        return getResponse(url, host, null, cookie);
    }


    public static Connection.Response getResponse(String url, String host, String reffer, String cookie) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        if (!Strings.isNullOrEmpty(host)){
            headers.put("Host", host);
        }
        if (!Strings.isNullOrEmpty(cookie)) {
            headers.put("Cookie",cookie);
        }
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        headers.put("Connection", "keep-alive");
        headers.put("Upgrade-Insecure-Requests", "1");
        return commRequest(url,headers,reffer,null, Connection.Method.GET);
    }
    public static Connection.Response getResponse(String url, Map<String,String> headers) throws IOException {
        return commRequest(url,headers,null,null, Connection.Method.GET);
    }

    public static Document getHtml2(String url) throws IOException{
        Document response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36")
                .timeout(50000)
                .get();
        return response;
    }
    public static InputStream httpGetStream(String url) throws Exception{
        URL url1 = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection)url1.openConnection();
        if (url.endsWith("svg")) {
            urlConnection.setRequestProperty("Content-type", "image/svg+xml");
        } else {
            urlConnection.setRequestProperty("Content-type", "application/json; charset=utf-8");
        }
        urlConnection.setRequestProperty("Host", "s3plus.meituan.net");
        urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(10000);
        urlConnection.connect();
        return urlConnection.getInputStream();
    }

    public static String httpGet(String url) throws Exception{
        URL url1 = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection)url1.openConnection();
        urlConnection.setRequestProperty("Content-type", "application/json; charset=utf-8");
//            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(10*1000);
        urlConnection.setReadTimeout(15*1000);
        urlConnection.connect();
        InputStream inStrm = urlConnection.getInputStream();

        InputStreamReader isr = new InputStreamReader(inStrm,"UTF-8");
        BufferedReader bufferReader = new BufferedReader(isr);
        String inputLine  = "";
        StringBuilder builder = new StringBuilder();
        while((inputLine = bufferReader.readLine()) != null){
            builder.append(inputLine);
        }
        return builder.toString();
    }

    public static String getImg(String url){
        try{

            URL url1 = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection)url1.openConnection();
//            urlConnection.setRequestProperty("Content-type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5*10000);
            urlConnection.connect();
            InputStream inStrm = urlConnection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] by = new byte[1024];
            int read = 0;
            while((read = inStrm.read(by))>0){
                out.write(by);
            }
//            InputStreamReader isr = new InputStreamReader(inStrm);
//            BufferedReader bufferReader = new BufferedReader(isr);
//            String inputLine  = "";
//            StringBuilder builder = new StringBuilder();
//            while((inputLine = bufferReader.readLine()) != null){
//                builder.append(inputLine);
//            }
            return Base64.encodeBase64String(out.toByteArray());
        }catch (Exception e){
            return null;
        }
    }

    public static String getJson(String url, Map<String,String> headers) throws IOException {
        String json = null;
        Connection.Response response = commRequest(url, headers, null, null, Connection.Method.GET);
        if (response != null) {
            json = response.body();
        }
        return json;
    }

    public static String postJson(String url, String host, String refferurl, Map<String, String> formdata) throws Exception {
        String json = null;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Host", host);
        Connection.Response response = commRequest(url, headers, refferurl, formdata, Connection.Method.POST);
        if (response != null) {
            json = response.body();
        }
        return json;
    }

    public static Connection.Response commRequest(String url, Map<String, String> headers, String refferurl, Map<String, String> formdata, Connection.Method connMethod) throws IOException {
        Connection.Response response = null;
        try {
            List<String> user_agents = ResidentialClient.user_agents;
            Connection conn = Jsoup.connect(url)
                    .userAgent(user_agents.get(rng.nextInt(user_agents.size())))
                    .timeout(5000)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(connMethod);
            if (formdata != null) {
                conn.data(formdata);
            }
            if (!Strings.isNullOrEmpty(refferurl)) {
                conn.referrer(refferurl);
            }
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.header(key, headers.get(key));
                }
            }
            response = conn.execute();

        } catch (Exception e) {
            if (e.toString().contains("404")) {
                return null;
            }
            throw e;
        }
        return response;
    }

    public static Connection.Response httpPost(String url, Map<String, String> data, String cookie, String host, String reffer) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        headers.put("Host", host);
        return commRequest(url, headers, reffer, data, Connection.Method.POST);
    }

    public static Connection.Response getDzdpRecommendDishHtml(String url) throws IOException{
        Connection.Response response = Jsoup.connect(url)
//                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("charset", "utf-8")
                .header("Accept-Encoding", "gzip")
                .header("platform", "Android-8")
                .header("channelversion", "6.6.7")
                .header("minaname", "dianping-wxapp")
                .header("sdkversion", "2.2.4")
                .header("token", "")
                .header("minaversion", "3.14.0")
                .header("content-type", "application/json")
                .header("wechatversion", "6.6.7")
                .header("referer", "https://servicewechat.com/wx734c1ad7b3562129/89/page-frame.html")
                .header("platformversion", "6.0.1")
                .header("channel", "")
                .header("openid", "bojD6hC_secp4KnThwgfbHettD4vktkorRHnRX1vkAw")
                .userAgent("Mozilla/5.0 (Linux; Android 6.0.1; oppo R11s Plus Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN")
                .header("Host", "m.dianping.com")
                .header("Connection", "Keep-Alive")
                .ignoreContentType(true)
                .timeout(50000)
                .execute();
        return response;

    }

    public static String getDzdpRecommendDishHtmlWithProxy(String url) throws ClientProtocolException, IOException {
        String html = null;
        int retry = 5;
        for (int i = 0; i < retry; i++) {
            try {
                html = httpExecutor.execute(Request.Get(url).userAgent(
                        "Mozilla/5.0 (Linux; Android 6.0.1; oppo R9 Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36 MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN MicroMessenger/6.6.7.1321(0x26060736) NetType/WIFI Language/zh_CN")
                        .addHeader("charset", "utf-8")
                        .addHeader("Accept-Encoding", "gzip")
                        .addHeader("platform", "Android-8")
                        .addHeader("channelversion", "6.6.7")
                        .addHeader("minaname", "dianping-wxapp")
                        .addHeader("sdkversion", "2.2.4")
                        .addHeader("token", "")
                        .addHeader("minaversion", "3.14.0")
                        .addHeader("content-type", "application/json")
                        .addHeader("wechatversion", "6.6.7")
                        .addHeader("referer", "https://servicewechat.com/wx734c1ad7b3562129/89/page-frame.html")
                        .addHeader("platformversion", "6.0.1")
                        .addHeader("channel", "")
                        .addHeader("openid", "bojD6hC_secp4KnThwgfbHettD4vktkorRHnRX1vkAw")
                        .addHeader("Host", "m.dianping.com")
                        .addHeader("Connection", "Keep-Alive")
                        .viaProxy(proxy))
                        .returnContent().asString(Charsets.UTF_8);

                if (StringUtils.isBlank(html)) {
                    continue;
                }
                Matcher htmlTitleMatcher = htmlTitlePattern.matcher(html);
                String htmlTitle = htmlTitleMatcher.find() ? htmlTitleMatcher.group() : null;

            } catch (HttpResponseException e) {
                e.printStackTrace();
                continue;
            }
        }

        return null;
    }

    public static void main(String[] args) throws Exception {
//        PhantomJSDriver htmlByPhantomjs = getHtmlByPhantomjs("http://www.dianping.com/shop/19612173");
        //经纬度
//        Connection.Response response = getDzdpWechatJson("https://m.dianping.com/wxmapi/shop/shopmapinfo?shopId=95883815");
//        Connection.Response response = getDzdpWechatJson("https://m.dianping.com/wxmapi/shop/shopinfo?shopId=1795132&os=android&online=1");
//        Connection.Response response = getDzdpWechatJson("https://m.dianping.com/shopping/shop/95883815");
//        log.info(response.body());

        String data = "{\"pageEnName\":\"spudishlist\",\"moduleInfoList\":[{\"moduleName\":\"header\",\"query\":{\"cityid\":1,\"skuid\":10057755,\"regionid\":-1,\"region\":true,\"lat\":\"\",\"lng\":\"\"}}]}";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Origin", "https://m.dianping.com");
        headers.put("Referer", "https://m.dianping.com/dishes/list/c1d10057755");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        String result = getHtmlByHttpClientWithProxy("https://m.dianping.com/dishes/module", null, headers, data);
        log.info(result);

        String url="https://api.foursquare.com/v2/venues/search?ll=30.6757154042,121.9042968750&categoryId=4bf58dd8d48988d163941735&client_id=NOBUO2S0R1OSPLM3GSLDTRV1GYBMJCFY4DGHIZHYNRQFM2Y5&client_secret=T2NPP1OTZVD5SDUDDDO1CLLL1FA4YNJ4IDMMRNX40QI4AM4M&v=20190411&limit=50&radius=100000";

        String r=httpGet(url);
//        log.info(response.body());

    }
}
