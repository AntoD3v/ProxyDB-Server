package org.cysdigital.proxydb.master;

public class Proxy {

    private String host;
    private int port;
    private boolean socks;
    private boolean http;
    private boolean direct;
    private boolean valid = false;

    public Proxy(String host, int port, boolean socks, boolean http) {
        this.host = host;
        this.port = port;
        this.socks = socks;
        this.http = http;
    }

    public Proxy(String host, int port) {
        this(host, port, false, false);
    }

    public boolean isDirect() {
        return direct;
    }

    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSocks(boolean socks) {
        this.socks = socks;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSocks() {
        return socks;
    }

    public boolean isHttp() {
        return http;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return getHost()+":"+getPort();
    }
}
