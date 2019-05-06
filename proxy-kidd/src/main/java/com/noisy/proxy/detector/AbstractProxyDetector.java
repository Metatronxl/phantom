package com.noisy.proxy.detector;

import com.noisy.proxy.entity.ProxyType;
import com.noisy.proxy.util.IPPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by kevin on 5/24/16.
 */

public abstract class AbstractProxyDetector implements ProxyDetector {


    private static final Logger log = LoggerFactory.getLogger(AbstractProxyDetector.class);
    //TODO update
    private String proxyCheckerURL = "http://www.scumall.com:20000/test/parse";
    private String proxyCheckerHost = "www.scumall.com";
    private int proxyCheckerPort = 20000;
    private String proxyCheckerPath ="/test/parse";
    private final int timeout = 10000;
    // TODO 改为自动获取
    private String localIP="115.28.58.82";
    private String outputDir="output/";



    public String getProxyCheckerURL() {
        return proxyCheckerURL;
    }

    public String getProxyCheckerHost() {
        return proxyCheckerHost;
    }

    public int getProxyCheckerPort() {
        return proxyCheckerPort;
    }

    public String getProxyCheckerPath() {
        return proxyCheckerPath;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getLocalIP() {
        if (StringUtils.isEmpty(localIP)) {
            try {
                localIP = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                localIP = null;
                log.warn("An exception occurred when retrieving the local IP, exception: {}", e);
            }
        }

        return localIP;
    }

    public String getOutputTmpFilePath(String schedulerName) {
        if (StringUtils.isEmpty(schedulerName)) {
            throw new IllegalArgumentException("The scheduler name cannot be null!");
        }

        StringBuilder filePath = new StringBuilder(outputDir);
        filePath.append(schedulerName);
        filePath.append("_");
        filePath.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        filePath.append(".tmp");
        return filePath.toString();
    }

    public String retrieveOutputFilePath(String outputTmpFilePath) {
        String[] tmp = outputTmpFilePath.split("\\.");
        return tmp[0] + ".txt";
    }

    public boolean validateIP(final String ip) {
        return IPPoolUtils.validateIP(ip);
    }

    public ProxyType checkProxyType(String proxyIp, String response) {
        if (response == null || !response.contains("proxy-checker")) {
            return null;
        }

        String currentLocalIP = getLocalIP();
        Set<String> responseIPs = IPPoolUtils.retrieveIPFromText(response);
        if (responseIPs == null || responseIPs.isEmpty()) {
            return null;
        }

        if (responseIPs.contains(currentLocalIP)) {
            return ProxyType.TRANSPARENT;
        } else if (responseIPs.contains(proxyIp)) {
            return ProxyType.ANONYMOUS;
        } else if (responseIPs.contains("0.0.0.0")) {
            return ProxyType.HIGH_ANONYMOUS;
        } else {
            return ProxyType.DISTORTING;
        }
    }
}
