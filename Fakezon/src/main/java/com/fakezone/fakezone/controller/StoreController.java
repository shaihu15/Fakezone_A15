package com.fakezone.fakezone.controller;


import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import org.atmosphere.config.service.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ISystemService systemService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final AuthenticatorAdapter authenticatorAdapter;

    @Autowired
    public StoreController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter){
        this.authenticatorAdapter = authenticatorAdapter;
        this.systemService = systemService;
    }

    @PostMapping("/addStore/{userId}/{storeName}")
    ResponseEntity<Response<Void>> addStore(@PathVariable("userId") int userId, @PathVariable("storeName") String storeName, @RequestHeader("Authorization") String token) {
        try{
            logger.info("Received request to add store with name: {} from user this request tocken of: {}", storeName, token);
            if(!authenticatorAdapter.isValid(token)){
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addStore(userId, storeName);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }

    }
}
