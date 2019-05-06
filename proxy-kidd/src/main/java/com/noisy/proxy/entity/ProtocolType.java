package com.noisy.proxy.entity;

/**
 * Created by kevin on 5/24/16.
 */
public enum ProtocolType {
    HTTP(0),
    SOCKS_V4(1),
    SOCKS_V5(2);

    private int type;

    ProtocolType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
