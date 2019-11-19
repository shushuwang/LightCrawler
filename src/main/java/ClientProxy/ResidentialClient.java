package ClientProxy;

import common.AbstractClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class ResidentialClient extends AbstractClient {
public static final String username = "lux-customer-zone-xx_residential-route_err-pass_dyn";
    public static final String password = "03pfxxxxxxx";
    public static final int port = 22225;
    public String session_id;
    public CloseableHttpClient clientNoProxy;
    public String country = null;
    private String loginName = null;

    public String getLoginName() {
        session_id = Integer.toString(rng.nextInt(Integer.MAX_VALUE));
        loginName = username+(country!=null ? "-country-"+country : "")
                +"-dns-remote-session-" + session_id;
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public ResidentialClient() {
        super();
        getLoginName();
        switch_ip();
    }

    public void switch_ip() {
        int proxy_session_id = rng.nextInt(Integer.MAX_VALUE);
        InetAddress address = null;
        try {
            address = InetAddress.getByName("session-"+proxy_session_id+".zproxy.lum-superproxy.io");
            String host = address.getHostAddress();
            this.host = host;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        n_req_for_exit_node = 0;
        super_proxy = new HttpHost(host, port);
        update_client();
    }

    public void update_client() {
        close();
        CredentialsProvider cred_provider = new BasicCredentialsProvider();
        cred_provider.setCredentials(new AuthScope(super_proxy),
                new UsernamePasswordCredentials(loginName, password));
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
        conn_mgr.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(80000).build());
        clientWithProxy = HttpClients.custom()
                .setConnectionManager(conn_mgr)
                .setProxy(super_proxy)
                .setDefaultCredentialsProvider(cred_provider)
                .setConnectionTimeToLive(100, TimeUnit.SECONDS)
                .setDefaultRequestConfig(config)
                .build();
        clientNoProxy = HttpClients.custom()
                .setConnectionManager(conn_mgr)
                .setDefaultRequestConfig(config)
                .build();
    }

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

    public void close() {
        if (clientWithProxy != null)
            try { clientWithProxy.close(); } catch (IOException e) {}
        clientWithProxy = null;
    }
}