package com.runners.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RunnersApplication {

	public static void main(String[] args) {
		SpringApplication.run(RunnersApplication.class, args);
	}

}
