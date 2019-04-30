package com.noisy;


import com.noisy.crawlers.GrabData5u;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @Auther: liuzhaoce
 * @Date: 2019-04-26 15:46
 * @Description:
 */

@SpringBootApplication
@MapperScan("com.noisy")
public class GrabCenter extends SpringBootServletInitializer {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(GrabCenter.class, args);
        GrabData5u.grab(context);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GrabCenter.class);
    }
}
