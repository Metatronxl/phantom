package com.noisy.proxy.util;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kevin on 5/25/16.
 */
public class IPSegment implements Comparable<IPSegment> {
    private final static Logger log = LoggerFactory.getLogger(IPSegment.class);

    private Integer randomIndex;
    private String ipWithMaskBits;
    private String startIP;
    private String endIP;
    private String netMask;
    private long nextIp;
    private long startLongIP;
    private long endLongIP;

    public IPSegment(String startIP, String endIP, String netMask) {
        this.startIP = startIP;
        this.startLongIP = ipToLong(startIP);
        this.endIP = endIP;
        this.netMask = netMask;
        this.nextIp = ipToLong(startIP);
        this.endLongIP = ipToLong(endIP);
    }

    public static long ipToLong(String strIp) {
        if (StringUtils.isEmpty(strIp)) {
            log.error("The given string IP is empty.");
            return -1;
        }

        long[] ip = new long[4];
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    public static String longToIP(long longIp) {
        if (longIp < 0) {
            log.error("The given long IP is less than 0.");
            return "0.0.0.0";
        }

        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    public String getNextStringIP() {
        String nextStringIP = longToIP(nextIp);
        nextIp += 1;
        return nextStringIP;
    }

    public boolean hasNextIP() {
        return nextIp <= endLongIP;
    }

    /**
     * Reset next IP to the start IP.
     */
    public void reset() {
        this.nextIp = ipToLong(startIP);
    }

    public Integer getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(Integer randomIndex) {
        this.randomIndex = randomIndex;
    }

    public String getIpWithMaskBits() {
        return ipWithMaskBits;
    }

    public void setIpWithMaskBits(String ipWithMaskBits) {
        this.ipWithMaskBits = ipWithMaskBits;
    }

    public String getStartIP() {
        return startIP;
    }

    public void setStartIP(String startIP) {
        this.startIP = startIP;
        nextIp = ipToLong(startIP);
    }

    public String getEndIP() {
        return endIP;
    }

    public void setEndIP(String endIP) {
        this.endIP = endIP;
        this.endLongIP = ipToLong(endIP);
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    public long getStartLongIP() {
        return startLongIP;
    }

    public void setStartLongIP(long startLongIP) {
        this.startLongIP = startLongIP;
    }

    public long getEndLongIP() {
        return endLongIP;
    }

    public void setEndLongIP(long endLongIP) {
        this.endLongIP = endLongIP;
    }

    @Override
    public int compareTo(IPSegment o) {
        return this.getRandomIndex().compareTo(o.getRandomIndex());
    }
}
