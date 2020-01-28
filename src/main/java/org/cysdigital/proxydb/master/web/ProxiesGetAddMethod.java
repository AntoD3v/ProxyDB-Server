package org.cysdigital.proxydb.master.web;

import org.cysdigital.proxydb.master.Proxy;
import org.cysdigital.proxydb.master.Storage;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.concurrent.BlockingQueue;

public class ProxiesGetAddMethod implements Route {
    private final Storage storage;

    public ProxiesGetAddMethod(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String key;
        KeyManagement keyManagement;
        if((key = request.queryParams("key")) != null){
            if((keyManagement = KeyManagement.valueOf(key)) != null){
                String content = new String(request.bodyAsBytes());
                String[] line = content.split("\n");
                BlockingQueue<Proxy> injectQueue = storage.getInjectQueue();
                for (String s : line) {
                    String[] proxy = s.split(":");
                    if(proxy.length == 2){
                        try {
                            int port = Integer.parseInt(proxy[1].trim());
                            injectQueue.add(new Proxy(proxy[0], port));
                        }catch (Exception e){
                            System.out.println("nop: "+s+" "+e.getMessage());
                        }

                    }
                }
                System.out.println("[   OK  ] "+keyManagement.getName()+" ADD "+line.length+" proxies (proxy_inject_count="+injectQueue.size()+")");

                return "success";
            }else
                return "Invalid key";
        }else
            return "Key and quantity are required (quantity-max=10000)";
    }
}
