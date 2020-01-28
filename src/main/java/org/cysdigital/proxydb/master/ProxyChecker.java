package org.cysdigital.proxydb.master;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public class ProxyChecker {

    private int TIME_OUT_MAX = 3000;
    private HttpURLConnection httpURLConnection;

    public void checkSock(Proxy proxy) {
        // System.out.println("[   DEBUG   ] Scan "+proxy.getHost()+":"+proxy.getPort()+" on sock (thread-id="+Thread.currentThread().getId()+")");
        boolean response = false;
        proxy.setSocks(response = connect(new java.net.Proxy(java.net.Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHost(), proxy.getPort()))));
        if(response) {
            proxy.setValid(true);
            System.out.println("[   DEBUG   ] Proxy "+proxy.getHost()+":"+proxy.getPort()+" work on socks (thread-id="+Thread.currentThread().getId()+")");
        }

        // System.out.println("[   DEBUG   ] End of scan "+proxy.getHost()+":"+proxy.getPort()+" on sock (isValid="+((response) ? "true" : "false")+", thread-id="+Thread.currentThread().getId()+")");
    }

    public void checkHttp(Proxy proxy) {
        // System.out.println("[   DEBUG   ] Scan "+proxy.getHost()+":"+proxy.getPort()+" on http (thread-id="+Thread.currentThread().getId()+")");
        boolean response = false;
        proxy.setHttp(response = connect(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()))));
        if(response) {
            proxy.setValid(true);
            System.out.println("[   DEBUG   ] Proxy "+proxy.getHost()+":"+proxy.getPort()+" work on http (thread-id="+Thread.currentThread().getId()+")");
        }
        // System.out.println("[   DEBUG   ] End of scan "+proxy.getHost()+":"+proxy.getPort()+" on http (isValid="+((response) ? "true" : "false")+", thread-id="+Thread.currentThread().getId()+")");
    }

    private boolean connect(java.net.Proxy proxy) {

        try {
            URL url = new URL("http://google.com");
            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            httpURLConnection.setConnectTimeout(TIME_OUT_MAX);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            return (httpURLConnection.getResponseCode() == 200);
        } catch (Exception ignored) {
            //ignored.printStackTrace();
        }
        return false;
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }
}
