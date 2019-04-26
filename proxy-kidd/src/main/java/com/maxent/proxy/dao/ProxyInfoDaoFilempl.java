package com.maxent.proxy.dao;

import com.maxent.proxy.detector.ProxyInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by kevin on 5/31/16.
 */
public class ProxyInfoDaoFilempl extends AbstractProxyInfoDaoImpl {
    private static final Logger log = LoggerFactory.getLogger(ProxyInfoDaoFilempl.class);

    @Override
    public void append(ProxyInfo proxyInfo, File distFile) {
        try {
            FileUtils.write(distFile, proxyInfo.toString(), "UTF-8", true);
        } catch (IOException e) {
            log.error("An exception occurred when appending a new proxy info to the file: {}",
                    distFile.getAbsolutePath());
        }
    }
}
