package org.cysdigital.proxydb.master.web;

import org.cysdigital.proxydb.master.Storage;
import spark.Request;
import spark.Response;
import spark.Route;

public class ProxiesGetMethod implements Route {

    private Storage storage;

    public ProxiesGetMethod(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String key, type,q;
        int quantity;
        KeyManagement keyManagement;
        if((key = request.queryParams("key")) != null && (q = request.queryParams("quantity")) != null){

            if((keyManagement = KeyManagement.valueOf(key)) != null){
                try{
                    quantity = Integer.parseInt(q);

                    String result = "";
                    if((type = request.queryParams("type")) != null){
                        if(type.equalsIgnoreCase("socks") || type.equalsIgnoreCase("http")){
                            System.out.println("[   OK  ] "+keyManagement.getName()+" GET "+quantity+" proxies type of "+type);
                            for (String s : storage.getProxiesQuantityWithType(quantity, type))
                                result += s+"\n";
                            return result;
                        }else
                            return "Type require socks or http";
                    }else {
                        System.out.println("[   OK  ] "+keyManagement.getName()+" GET "+quantity+" proxies");
                        for (String s : storage.getProxiesQuantity(quantity))
                            result += s+"\n";
                        return result;
                    }

                }catch (Exception e){
                    return "Quantity require integer";
                }

            }else
                return "Invalid key";
        }else
            return "Key and quantity are required (quantity-max=10000)";

    }
}
