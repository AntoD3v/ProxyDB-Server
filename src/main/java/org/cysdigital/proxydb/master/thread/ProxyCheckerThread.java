package org.cysdigital.proxydb.master.thread;

import org.cysdigital.proxydb.master.Proxy;
import org.cysdigital.proxydb.master.ProxyChecker;
import org.cysdigital.proxydb.master.Storage;
import org.cysdigital.proxydb.master.queue.BlockingSystemLinkedBlockingQueue;

public class ProxyCheckerThread extends Thread{

    private boolean alreadyNull = false;
    private int threadMax = 20;
    private BlockingSystemLinkedBlockingQueue<Proxy> queue = new BlockingSystemLinkedBlockingQueue<Proxy>();
    private final Storage storage;

    public ProxyCheckerThread(Storage storage) {
        super(ProxyCheckerThread.class.getName());
        this.storage = storage;
    }

    @Override
    public void run() {

        for (int i = 0; i < threadMax; i++) {
            new ThreadKeepAlive(){

                private Proxy proxy = null;

                @Override
                public void run() {
                    while (true){
                        if(getQueue().size() == 0 || getQueue().isBlocked()) {
                            aliveAgain();
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        try {
                            proxy = getQueue().take();
                            getChecker().checkHttp(proxy);
                            getChecker().checkSock(proxy);
                            storage.getAfterQueue().add(proxy);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        aliveAgain();
                    }
                }
            }.start();
        }
        int size;
        System.out.println("[   OK  ] All thread executed (thread_count="+Thread.getAllStackTraces().size()+")");
        while(true){

            if(queue.size() == 0){
                storage.pushQueue(storage.getAfterQueue());
                storage.refillQueue(getQueue());
                size = getQueue().size();
                if((size == 0 && !alreadyNull) || (size != 0)) {
                    alreadyNull = (size == 0);
                    System.out.println("[   OK  ] Refill queue (proxy_count="+getQueue().size()+")");
                }
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized BlockingSystemLinkedBlockingQueue<Proxy> getQueue() {
        return queue;
    }

    @Override
    public synchronized void start() {
        System.out.println("[   OK  ] Thread "+this.getName()+" started");
        super.start();
    }

    public int getThreadMax() {
        return threadMax;
    }
}
