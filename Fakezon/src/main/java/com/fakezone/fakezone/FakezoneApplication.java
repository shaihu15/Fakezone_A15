package com.fakezone.fakezone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.fakezone.fakezone", "ApplicationLayer.Services"})
public class FakezoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(FakezoneApplication.class, args);
	}

}
