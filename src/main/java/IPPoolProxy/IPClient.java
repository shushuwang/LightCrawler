package IPPoolProxy;

import com.google.common.base.Strings;
import common.AbstractClient;
import org.apache.http.HttpHost;
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
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * shushuwang
 */
public class IPClient extends AbstractClient {

    public IPClient() {
        super();
        switch_ip();
    }

    public void switch_ip() {
        n_req_for_exit_node = 0;
        IP randomIP = IPFactory.getunUsedIP();
        if (randomIP != null) {
            if (!Strings.isNullOrEmpty(host)) {
                IPFactory.removeUsingIp(host+":"+port);
            }
            host = randomIP.getHost();
            port = randomIP.getPort();
        }
        if (!Strings.isNullOrEmpty(host)) {
            super_proxy = new HttpHost(host, port);
        }
        update_client();
    }

    public void update_client() {
        if (clientWithProxy != null)
            try {
                clientWithProxy.close();
            } catch (IOException e) {}
        clientWithProxy = null;
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
        if (!Strings.isNullOrEmpty(host)) {
            clientWithProxy = HttpClients.custom()
                    .setConnectionManager(conn_mgr)
                    .setProxy(super_proxy)
                    .setDefaultRequestConfig(config)
                    .setConnectionTimeToLive(8, TimeUnit.SECONDS)
                    .build();
        } else {
            clientWithProxy = HttpClients.custom()
                    .setConnectionManager(conn_mgr)
                    .setDefaultRequestConfig(config)
                    .setConnectionTimeToLive(8, TimeUnit.SECONDS)
                    .build();
        }
    }
}