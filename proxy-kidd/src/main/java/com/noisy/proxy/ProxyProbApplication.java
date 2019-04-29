package com.noisy.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author lei.X
 * @date 2019/4/29
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ProxyProbApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyProbApplication.class, args);
    }
}
