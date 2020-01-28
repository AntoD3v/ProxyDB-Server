package org.cysdigital.proxydb.master.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class BlockingSystemLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private boolean blocked;

    public synchronized  boolean isBlocked() {
        return blocked;
    }

    public synchronized void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public synchronized  void blockSystem(){
        this.blocked = true;
    }
}
