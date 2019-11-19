package common;

import IPPoolProxy.IPFactory;
import com.google.common.base.Strings;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by shushuwang on 2018/11/21.
 */
public abstract class AbstractClient {
    public static final List<String> user_agents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:61.0) Gecko/20100101 Firefox/61.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:17.0) Gecko/20100101 Firefox/17.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0"
    );
    public static final int max_failures = 2;
    public static final int req_timeout = 100*1000;
    public static final int socket_timeout = 100*1000;
    public HttpHost super_proxy;
    public CloseableHttpClient clientWithProxy;
    public int fail_count;
    public int n_req_for_exit_node;
    public Random rng;
    public String host;
    public int port;

    public AbstractClient() {
        rng = new Random();
    }

    public HttpGet getHttpGet(String url, Map<String, String> headers) {
        HttpGet request = new HttpGet(url);
        setHeader(request, headers);
        return request;
    }

    public HttpPost getHttpPost(String url, Map<String, String> headers, String data) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(url);
        setHeader(request, headers);
        StringEntity jsondata = new StringEntity(data);
//        jsondata.setContentEncoding(Charsets.UTF_8.toString());
        jsondata.setContentType("application/json");
//        HttpEntityWrapper entityWrapper = new HttpEntityWrapper(jsondata);
        request.setEntity(jsondata);
        return request;
    }

    private void setHeader(HttpRequestBase request, Map<String, String> headers) {
        request.setHeader("User-Agent", user_agents.get(rng.nextInt(user_agents.size())));
        request.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        request.setHeader("Accept-Encoding","gzip, deflate");
        request.setHeader("Accept-Language","zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        request.setHeader("Connection","keep-alive");
        request.setHeader("Upgrade-Insecure-Requests","1");
        if (headers != null) {
            for (String key : headers.keySet()) {
                request.setHeader(key, headers.get(key));
            }
        }
    }

    // Form 表单请求
    public CloseableHttpResponse postFormRequest(String url, HttpClientContext context, Map<String,String> headers, Map<String, String> formParams) throws IOException {
        try {
            HttpPost request = new HttpPost(url);
            if(formParams != null){
                List <NameValuePair> nvps = new ArrayList<>();
                for(String key: formParams.keySet()){
                    nvps.add(new BasicNameValuePair(key, formParams.get(key)));
                }
                request.setEntity(new UrlEncodedFormEntity(nvps));
            }
            setHeader(request, headers);
            CloseableHttpResponse response = clientWithProxy.execute(request, context);
            handle_response(response);
            return response;
        } catch (IOException e) {
            handle_response(null);
            throw e;
        }
    }

    public CloseableHttpResponse requestWithProxy(String url, HttpClientContext context, Map<String,String> headers) throws IOException {
        try {
            HttpGet request = getHttpGet(url, headers);
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(req_timeout)
                    .setConnectionRequestTimeout(req_timeout)
                    .setSocketTimeout(socket_timeout)
                    .build();
            request.setConfig(config);
            CloseableHttpResponse response = clientWithProxy.execute(request, context);
            handle_response(response);
            return response;
        } catch (IOException e) {
            handle_response(null);
            throw e;
        }
    }

    public CloseableHttpResponse postWithProxy(String url, HttpClientContext context, Map<String,String> headers, String data) throws IOException {
        try {
            HttpPost request = getHttpPost(url, headers, data);
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(req_timeout)
                    .setConnectionRequestTimeout(req_timeout)
                    .setSocketTimeout(socket_timeout)
                    .build();
            request.setConfig(config);
            CloseableHttpResponse response = clientWithProxy.execute(request, context);
            handle_response(response);
            return response;
        } catch (IOException e) {
            handle_response(null);
            throw e;
        }
    }

    protected SSLContext createIgnoreVerifySSL() {
        try{
            return new SSLContextBuilder().loadTrustMaterial(null, (e, t) -> true).build();
            /*SSLContext sc = SSLContext.getInstance("SSLv3");

            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                               String paramString) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                                               String paramString) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sc.init(null, new TrustManager[] { trustManager }, null);
            return sc;*/
        }catch (Exception e){
            e.printStackTrace();
            return SSLContexts.createSystemDefault();
        }
    }

    public abstract void switch_ip();

    public void handle_response(HttpResponse response) {
        if (response != null && !status_code_requires_exit_node_switch(
                response.getStatusLine().getStatusCode())) {
            // success or other clientWithProxy/website error like 404...
            n_req_for_exit_node++;
            fail_count = 0;
            return;
        }
        switch_ip();
        fail_count++;
    }

    public boolean status_code_requires_exit_node_switch(int code) {
        return code == 403 || code == 429 || code==502 || code == 503;
    }

    public boolean have_good_super_proxy() {
        return super_proxy != null && fail_count < max_failures;
    }

    public void close() {
        if (!Strings.isNullOrEmpty(host)) {
            IPFactory.removeUsingIp(host+":"+port);
        }
        if (clientWithProxy != null)
            try {
                clientWithProxy.close();
            } catch (IOException e) {}
        clientWithProxy = null;
    }
}
