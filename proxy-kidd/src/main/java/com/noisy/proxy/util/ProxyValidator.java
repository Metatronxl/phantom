package com.noisy.proxy.util;

import com.noisy.proxy.detector.ProtocolType;
import com.noisy.proxy.detector.ProxyDetector;
import com.noisy.proxy.task.TaskScheduler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kevin on 6/21/16.
 */
public class ProxyValidator {

    private ProxyDetector proxyDetector;

    public ProxyValidator() {
        TaskScheduler scheduler = new TaskScheduler("InlandHTTPProxyValidation", "HTTP代理验证",
                ProtocolType.HTTP, new HashMap<Integer,String>());
        proxyDetector = scheduler.getProxyDetector();
    }

    public void proxyValidate(String ipListFilePath, int port) throws IOException {
        File proxyIPFile = new File(ipListFilePath);
        List<String> proxyIPs = FileUtils.readLines(proxyIPFile);

        if (proxyIPs != null) {
            for (String proxyIP : proxyIPs) {
                proxyDetector.detect(proxyIP, port);
            }
        }
    }

    public void proxyValidate(String proxyListFilePath) throws IOException {
        File proxyIPFile = new File(proxyListFilePath);
        List<String> proxys = FileUtils.readLines(proxyIPFile);

        if (proxys != null) {
            for (String proxy : proxys) {
                String[] tmpStr = proxy.split("\\,");
                proxyDetector.detect(tmpStr[0], Integer.parseInt(tmpStr[1]));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ProxyValidator proxyValidator = new ProxyValidator();
        proxyValidator.proxyValidate("data/test.txt");
    }
}
