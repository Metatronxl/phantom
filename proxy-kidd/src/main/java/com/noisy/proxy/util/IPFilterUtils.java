package com.noisy.proxy.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lei.x on 5/6/19.
 */
public class IPFilterUtils {
    private final static long longMulticastIP = IPSegment.ipToLong("224.0.0.0");
    private final static long classAPrivateIPStart = IPSegment.ipToLong("10.0.0.0");
    private final static long classAPrivateIPEnd = IPSegment.ipToLong("10.255.255.255");
    private final static long classBPrivateIPStart = IPSegment.ipToLong("172.16.0.0");
    private final static long classBPrivateIPEnd = IPSegment.ipToLong("172.31.255.255");
    private final static long classCPrivateIPStart = IPSegment.ipToLong("192.168.0.0");
    private final static long classCPrivateIPEnd = IPSegment.ipToLong("192.168.255.255");

    private final static IPFilterUtils _Instance = new IPFilterUtils();

    private Set<String> _FilterIPList = new HashSet<String>(){
        {
            add("139.219.135.92");
            add("185.12.64.0");
            add("185.12.64.1");
            add("185.12.64.2");
            add("185.12.64.3");
            add("104.20.65.24");

        }
    };



    private IPFilterUtils() {

    }

    public static IPFilterUtils getInstance() {
        return _Instance;
    }

    public boolean needFilter(final String ip) {
        if (_FilterIPList.contains(ip)) {
            return true;
        }

        return isPrivateIPOrMulticastIP(ip);
    }

    public boolean isPrivateIPOrMulticastIP(final String ip) {
        long longIp = IPSegment.ipToLong(ip);
        if (longIp >= longMulticastIP) {
            return true;
        }

        if ((longIp >= classAPrivateIPStart && longIp <= classAPrivateIPEnd)
                || (longIp >= classBPrivateIPStart && longIp <= classBPrivateIPEnd)
                || (longIp >= classCPrivateIPStart && longIp <= classCPrivateIPEnd)) {
            return true;
        }

        return false;
    }

}

