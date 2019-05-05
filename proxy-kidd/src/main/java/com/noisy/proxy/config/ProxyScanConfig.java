package com.noisy.proxy.config;

import lombok.Data;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author lei.X
 * @date 2019/4/30
 */
@Component
@PropertySource("classpath:config.properties")

@Data
public class ProxyScanConfig {

    private String proxyCheckerUrl;

    private String outputDir;

    private String timeout;

    private String localhostIp;


}
