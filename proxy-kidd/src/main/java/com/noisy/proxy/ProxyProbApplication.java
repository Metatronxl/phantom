package com.noisy.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author lei.X
 * @date 2019/4/29
 */
@SpringBootApplication
@EnableJpaAuditing
public class ProxyProbApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProxyProbApplication.class, args);
        ProxyScanner bean = context.getBean(ProxyScanner.class);

        bean.start();

    }
}
