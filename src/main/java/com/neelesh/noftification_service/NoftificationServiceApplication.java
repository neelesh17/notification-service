package com.neelesh.noftification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NoftificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoftificationServiceApplication.class, args);
	}

}
