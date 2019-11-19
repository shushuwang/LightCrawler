package ClientProxy;

import common.AbstractClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;

/**
 * @author shushuwang
 * @create 2019-03-07 14:35
 **/
public class LocalClient extends AbstractClient {

    public LocalClient() {
        switch_ip();
    }
    @Override
    public void switch_ip() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(req_timeout)
                .setConnectionRequestTimeout(req_timeout)
                .setSocketTimeout(socket_timeout)
                .build();

        SSLContext sslcontext = createIgnoreVerifySSL();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE))
                .build();
        PoolingHttpClientConnectionManager conn_mgr =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        conn_mgr.setDefaultMaxPerRoute(Integer.MAX_VALUE);
        conn_mgr.setMaxTotal(Integer.MAX_VALUE);
        conn_mgr.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(8000).build());
        clientWithProxy = HttpClients.custom()
                .setConnectionManager(conn_mgr)
                .setDefaultRequestConfig(config)
                .setConnectionTimeToLive(8, TimeUnit.SECONDS)
                .build();
    }
}
