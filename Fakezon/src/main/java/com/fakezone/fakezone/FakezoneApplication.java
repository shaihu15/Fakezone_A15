package com.fakezone.fakezone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; 

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.fakezone.fakezone",     
    "ApplicationLayer",           
    "DomainLayer",                
    "InfrastructureLayer"         
})
public class FakezoneApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakezoneApplication.class, args);
    }

}