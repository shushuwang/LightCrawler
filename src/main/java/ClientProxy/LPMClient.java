package ClientProxy;

import common.AbstractClient;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.mime.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * proxy
 *
 * @author shushuwang
 * @create 2018-11-21 10:56
 **/
public class LPMClient extends AbstractClient {
    final static Executor httpexector = Executor.newInstance();
    private static Random random = new Random();
    private CloseableHttpClient clientWithProxy;
    private static final int req_timeout = 100*1000;
    private static final int socket_timeout = 100*1000;

    private HttpHost randomProxyPort() {
        int port = random.nextInt(15)+24000;
        new Header();
        return new HttpHost("127.0.0.1", port);
    }

    public String requestWithProxy(String url) throws IOException {
        HttpHost proxy = randomProxyPort();
        String res = httpexector
                .execute(Request.Get(url).viaProxy(proxy))
                .returnContent().asString();
        return res;
    }
    public LPMClient() {
        try {
            switch_proxy();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public void switch_proxy() throws KeyManagementException, NoSuchAlgorithmException {
        close();
        HttpHost proxy = randomProxyPort();
        SSLContext sslcontext = createIgnoreVerifySSL();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();
        PoolingHttpClientConnectionManager conn_mgr =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        conn_mgr.setDefaultMaxPerRoute(Integer.MAX_VALUE);
        conn_mgr.setMaxTotal(Integer.MAX_VALUE);
        conn_mgr.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(socket_timeout).build());
        clientWithProxy = HttpClients.custom()
                .setConnectionManager(conn_mgr)
                .setProxy(proxy)
                .setConnectionTimeToLive(100, TimeUnit.SECONDS)
                .build();
    }
    /*
    public SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }*/

    @Override
    public void switch_ip() {
        try {
            switch_proxy();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public CloseableHttpResponse requestWithProxy(String url,HttpClientContext context, Map<String,String> headers) throws IOException {
        HttpGet request = new HttpGet(url);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(req_timeout)
                .setConnectionRequestTimeout(req_timeout)
                .setSocketTimeout(socket_timeout)
                .build();
        request.setConfig(config);
        if (headers != null) {
            for (String key : headers.keySet()) {
                request.setHeader(key, headers.get(key));
            }
        }
//        int proxy_session_id = random.nextInt(Integer.MAX_VALUE);
//        request.setHeader("x-lpm-session", "dzdp"+proxy_session_id);
        request.setHeader("User-Agent", AbstractClient.user_agents.get(random.nextInt(AbstractClient.user_agents.size())));
        CloseableHttpResponse response = null;
        if (context != null) {
            response = clientWithProxy.execute(request, context);
        } else {
            response = clientWithProxy.execute(request);
        }
        return response;
    }
    public void close() {
        if (clientWithProxy != null)
            try { clientWithProxy.close(); } catch (IOException e) {}
        clientWithProxy = null;
    }

    public static void main(String[] args) throws IOException {
        LPMClient client = new LPMClient();
        CloseableHttpResponse response = client.requestWithProxy("https://www.tripadvisor.cn/TourismChildrenAjax?geo=293943&offset=13&desktop=true", null, null);

        response.close();
    }
}
