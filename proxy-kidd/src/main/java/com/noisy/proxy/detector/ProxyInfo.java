package com.noisy.proxy.detector;

/**
 * Created by kevin on 5/24/16.
 */
public class ProxyInfo {
    private String ip;
    private int protocol;
    private int port;
    private int type;
    private String location;
    private long updateTime;

    public ProxyInfo() {
    }

    public ProxyInfo(String ip, int protocol, int port, int type, long updateTime) {
        this.ip = ip;
        this.protocol = protocol;
        this.port = port;
        this.type = type;
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(ip);
        stringBuilder.append(",");
        stringBuilder.append(protocol);
        stringBuilder.append(",");
        stringBuilder.append(port);
        stringBuilder.append(",");
        stringBuilder.append(type);
        stringBuilder.append(",");
        stringBuilder.append(location);
        stringBuilder.append(",");
        stringBuilder.append(updateTime);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
