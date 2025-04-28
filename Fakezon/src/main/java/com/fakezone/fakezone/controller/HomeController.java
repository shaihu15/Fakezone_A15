package com.fakezone.fakezone.controller;

import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);


    @GetMapping("/")
    public ResponseEntity<Response<String>> home() {
        try{
            Response<String> test = new Response<String>("home", "Welcome to the home page!", true);
            return ResponseEntity.ok(test);
        }
        catch (Exception e){
            logger.error("Error in home controller: {}", e.getMessage());
            Response<String> test = new Response<String>(null, "An error occurred at the controller level ", false, ErrorType.INTERNAL_ERROR);
            return ResponseEntity.status(500).body(test);
        }
    }

}
