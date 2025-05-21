package com.fakezone.fakezone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication(scanBasePackages = {"com.fakezone.fakezone", "ApplicationLayer.Services", "DomainLayer","com.fakezone.fakezone.ui.view"})
public class FakezoneApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(FakezoneApplication.class, args);
	}

}
