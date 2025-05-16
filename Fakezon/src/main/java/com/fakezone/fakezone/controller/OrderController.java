package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/order")
public class OrderController {

    public ISystemService systemService;
    private final AuthenticatorAdapter authenticatorAdapter;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    public OrderController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter){
        this.systemService = systemService;
        this.authenticatorAdapter = authenticatorAdapter;
    }


    @GetMapping("/deleteOrder/{orderId}")
    public ResponseEntity<Response<Boolean>> deleteOrder(@PathVariable("orderId") int orderId, @RequestHeader("Authorization") String token){
        try{
            if(!authenticatorAdapter.isValid(token)){
                Response<Boolean> response = new Response<>(null, "User is not logged in", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.deleteOrder(orderId, token);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            logger.error("Error in OrderController: {}", e.getMessage());
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while deleting the order", false, ErrorType.INTERNAL_ERROR, null));
        }
    }

    @GetMapping("/viewOrder/{orderId}")
    public ResponseEntity<Response<OrderDTO>> viewOrder(@PathVariable("orderId") int orderId,  @RequestHeader("Authorization") String token){
        try{
            if(!authenticatorAdapter.isValid(token)){
                return ResponseEntity.status(401).body(new Response<>(null, "Unauthorized", false, ErrorType.UNAUTHORIZED, null));
            }
            Response<OrderDTO> response = systemService.viewOrder(orderId, token);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            logger.error("Error in OrderController: {}", e.getMessage());
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while viewing the order", false, ErrorType.INTERNAL_ERROR, null));
        }
    }

    @GetMapping("/searchOrders/{keyword}")
    public ResponseEntity<Response<List<OrderDTO>>> searchOrders(@PathVariable("keyword") String keyword, @RequestHeader("Authorization") String token){
        try{
            if(!authenticatorAdapter.isValid(token)){
                return ResponseEntity.status(401).body(new Response<>(null, "Unauthorized", false, ErrorType.UNAUTHORIZED, null));
            }
            Response<List<OrderDTO>> response = systemService.searchOrders(keyword, token);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            logger.error("Error in OrderController: {}", e.getMessage());
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while searching for orders", false, ErrorType.INTERNAL_ERROR, null));
        }
    }

    @GetMapping("/getOrdersByUserId/{storeId}")
    public ResponseEntity<Response<List<OrderDTO>>> getOrdersByStoreId(@PathVariable("storeId") int storeId, @RequestHeader("Authorization") String token){
        try{
            if(!authenticatorAdapter.isValid(token)){
                return ResponseEntity.status(401).body(new Response<>(null, "Unauthorized", false, ErrorType.UNAUTHORIZED, null));
            }
            Response<List<OrderDTO>> response = systemService.getOrdersByStoreId(storeId, token);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            logger.error("Error in OrderController: {}", e.getMessage());
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while getting orders by store ID", false, ErrorType.INTERNAL_ERROR, null));
        }
    }

}
