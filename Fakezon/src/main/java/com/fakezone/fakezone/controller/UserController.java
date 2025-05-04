package com.fakezone.fakezone.controller;

import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final ISystemService systemService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    public UserController(ISystemService systemService){
        this.systemService = systemService;
    }



    @GetMapping("/GetTrueStub/{id}")
    public ResponseEntity<Response<Boolean>> getTrueStub(@PathVariable("id") int id, @RequestHeader("Authorization") String token) {
        try{
            Response<Boolean> response = new Response<>(true, "Success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/PostFalseStub")
    public ResponseEntity<Response<Boolean>> getFalseStub(@RequestBody Request<Boolean> request, @RequestHeader("Authorization") String token){
        try{
            Response<Boolean> response = new Response<>(false, "Success", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred at the controller level", false);
            return ResponseEntity.status(500).body(response);
        }
    }
}
