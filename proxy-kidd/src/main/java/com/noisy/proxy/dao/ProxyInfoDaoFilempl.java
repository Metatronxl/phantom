package com.noisy.proxy.dao;

import com.noisy.proxy.entity.ProxyInfo;
import com.noisy.proxy.entity.ProxyIp;
import com.noisy.proxy.repository.ProxyIpRepository;
import com.noisy.proxy.service.ProxyIpService;
import com.noisy.proxy.util.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by kevin on 5/31/16.
 */

@Slf4j
public class ProxyInfoDaoFilempl extends AbstractProxyInfoDaoImpl {


    ProxyIpService proxyIpService = (ProxyIpService) SpringContextUtils.getApplicationContext().getBean("proxyIpService");

    @Override
    public void append(ProxyInfo proxyInfo, File distFile) {
        try {
            FileUtils.write(distFile, proxyInfo.toString(), "UTF-8", true);
            proxyIpService.saveNewProxyIp(proxyInfo);
        } catch (IOException e) {
            log.error("An exception occurred when appending a new proxy info to the file: {}",
                    distFile.getAbsolutePath());
        }
    }
}
