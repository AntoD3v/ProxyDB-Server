package org.cysdigital.proxydb.master;

import org.cysdigital.proxydb.master.thread.ProxyCheckerThread;
import org.cysdigital.proxydb.master.web.WebMaster;

public class Master {

    private Storage storage = new Storage();
    private ProxyCheckerThread proxyCheckerThread = new ProxyCheckerThread(storage);
    private WebMaster webMaster = new WebMaster(storage);

    public Master() {
        proxyCheckerThread.start();
       /* try {
            storage.getInjectQueue().put(new Proxy("103.12.161.194", 59777));
            storage.getInjectQueue().put(new Proxy("182.52.238.111", 30098));
            storage.getInjectQueue().put(new Proxy("151.253.158.19", 8080));
            storage.getInjectQueue().put(new Proxy("36.37.73.245", 8080));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public static void main(String[] args){
        new Master();
    }
}
