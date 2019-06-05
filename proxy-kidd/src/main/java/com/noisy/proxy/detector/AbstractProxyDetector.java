package com.noisy.proxy.detector;

import com.noisy.proxy.entity.ProxyType;
import com.noisy.proxy.util.IPPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by lei.x on 5/24/19.
 */

public abstract class AbstractProxyDetector implements ProxyDetector {


    private static final Logger log = LoggerFactory.getLogger(AbstractProxyDetector.class);
    //TODO update
    private String proxyCheckerURL = "http://www.scumall.com:20000/test/parse";
    private final int timeout = 10000;
    private String outputDir="output/";


    public String getProxyCheckerURL() {
        return proxyCheckerURL;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getLocalIP() throws UnknownHostException {

        String localIP = null;
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localIP = null;
            log.error("An exception occurred when retrieving the local IP, exception: {}", e);
            throw new UnknownHostException("can't get local IP");
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

    public ProxyType checkProxyType(String proxyIp, String response) throws UnknownHostException {

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
