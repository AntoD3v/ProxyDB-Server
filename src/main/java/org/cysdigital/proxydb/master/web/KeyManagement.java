package org.cysdigital.proxydb.master.web;

public enum KeyManagement {

    ZF032F584DPMXNS5C554323F62C56D5S55("Antoine");

    private final String name;

    KeyManagement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
