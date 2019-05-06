package com.noisy.phantom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;


//@SpringBootApplication
@MapperScan("com.noisy")
public class PhantomApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhantomApplication.class, args);
	}

}
