package com.noisy.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author lei.X
 * @date 2019/4/29
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ProxyProbApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProxyProbApplication.class, args);
        ProxyScanner bean = context.getBean(ProxyScanner.class);
        bean.start();

    }
}
