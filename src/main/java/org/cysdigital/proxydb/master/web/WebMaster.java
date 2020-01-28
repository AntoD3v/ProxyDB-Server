package org.cysdigital.proxydb.master.web;

import org.cysdigital.proxydb.master.Storage;

import static spark.Spark.get;
import static spark.Spark.port;

public class WebMaster {

    private final Storage storage;
    private final int PORT = 8003;

    public WebMaster(Storage storage) {
        this.storage = storage;
        registerEntries();
    }

    private void registerEntries() {
        port(PORT);
        System.out.println("[   OK  ] Spark web server is open on localhost:8003");
        get("/proxies/get", new ProxiesGetMethod(storage));
        get("/proxies/add", new ProxiesGetAddMethod(storage));



    }
}
