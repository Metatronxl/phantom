package com.noisy.proxy.detector;

/**
 * Created by kevin on 5/24/16.
 */
public enum ProxyType {
    TRANSPARENT(0),
    ANONYMOUS(1),
    DISTORTING(2),
    HIGH_ANONYMOUS(3);

    private int type;

    ProxyType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
