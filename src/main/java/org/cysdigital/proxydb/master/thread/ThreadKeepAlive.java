package org.cysdigital.proxydb.master.thread;

import org.cysdigital.proxydb.master.ProxyChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadKeepAlive extends Thread{

    private static int KEEP_ALIVE_TIMEOUT = 30000;
    private static Map<Thread, Long> keepAliveThreadMap = new ConcurrentHashMap<Thread, Long>();

    private ProxyChecker checker = new ProxyChecker();

    static {

        new Thread("Keep-Alive-Threading"){
            @Override
            public void run() {
                List<Thread> delete;
                while(true){
                    delete = new ArrayList<>();
                    for (Map.Entry<Thread, Long> entry : keepAliveThreadMap.entrySet()) {
                        Thread thread = entry.getKey();
                        Long lastKeepAliveTime = entry.getValue();
                        if (lastKeepAliveTime < System.currentTimeMillis()) {
                            ((ThreadKeepAlive)thread).getChecker().getHttpURLConnection().disconnect();
                            System.out.println("[   WARN    ] Thread " + thread.getName() + " (thread-id=" + thread.getId() + ") blocked since " + (System.currentTimeMillis()-lastKeepAliveTime+KEEP_ALIVE_TIMEOUT) + "ms. (keep-alive-map-count=" + keepAliveThreadMap.size() + ", thread-executed-now=" + Thread.getAllStackTraces().size() + ")");
                            delete.add(thread);
                        }
                    }
                    delete.forEach(thread -> keepAliveThreadMap.remove(thread));
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

    }

    @Override
    public synchronized void start() {
        super.start();
        aliveAgain();
    }

    public void aliveAgain(){
        // System.out.println("[ DEBUG ] Keep alive for thread-id="+Thread.currentThread().getId());
        keepAliveThreadMap.put(this, System.currentTimeMillis()+KEEP_ALIVE_TIMEOUT);
    }

    public ProxyChecker getChecker() {
        return checker;
    }

}
