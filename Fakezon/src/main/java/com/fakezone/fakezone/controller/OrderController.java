package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.RequestDataTypes.RequestOrderDataType;
import ApplicationLayer.Response;
import ApplicationLayer.Services.ProductService;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
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
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final AuthenticatorAdapter authenticatorAdapter;

    @Autowired
    public OrderController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter) {
        this.systemService = systemService;
        this.authenticatorAdapter = authenticatorAdapter;
    }


    @GetMapping("/deleteOrder/{orderId}")
    public ResponseEntity<Response<Boolean>> deleteOrder(@PathVariable("orderId") int orderId, @RequestHeader("Authorization") String token){
        try{
            logger.info("Received request to delete order with ID: {} with token: {}", orderId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(false, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            int userId = authenticatorAdapter.getUserId(token);
            Response<Boolean> response = systemService.deleteOrder(orderId, userId);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            logger.error("Error in OrderController: {}", e.getMessage());
            return ResponseEntity.status(500).body(new Response<>(false, "An error occurred while deleting the order", false, ErrorType.INTERNAL_ERROR, null));
        }
    }

    @GetMapping("/viewOrder/{orderId}")
    public ResponseEntity<Response<OrderDTO>> viewOrder(@PathVariable("orderId") int orderId, @RequestHeader("Authorization") String token){
        try{
            logger.info("Received request to view order with ID: {} with token: {}", orderId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<OrderDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            int userId = authenticatorAdapter.getUserId(token);
            Response<OrderDTO> response = systemService.viewOrder(orderId, userId);
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
            logger.info("Received request to search orders with keyword: {} with token: {}", keyword, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<OrderDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            int userId = authenticatorAdapter.getUserId(token);
            Response<List<OrderDTO>> response = systemService.searchOrders(keyword, userId);
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
            logger.info("Received request to get orders for store ID: {} with token: {}", storeId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<OrderDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            int userId = authenticatorAdapter.getUserId(token);
            Response<List<OrderDTO>> response = systemService.getOrdersByStoreId(storeId, userId);
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
