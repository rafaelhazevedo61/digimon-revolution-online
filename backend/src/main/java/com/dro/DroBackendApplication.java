package com.dro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DroBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DroBackendApplication.class, args);
	}

}
