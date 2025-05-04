package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class OrderController {

    public ISystemService systemService;

    @Autowired
    public OrderController(ISystemService systemService) {
        this.systemService = systemService;
    }

    public ResponseEntity<Response<Integer>> makeOrder(int userId, int storeId, Collection<Integer> products, String address, String paymentMethod, @RequestBody Request request){
        try{
            Response<Integer> response = systemService.addOrder(userId, storeId, products, address, paymentMethod, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while making the order", false));
        }
    }

    public ResponseEntity<Response<Integer>> updateOrder(int orderId, int userId, Collection<Integer> products, int storeId, String address, String paymentMethod, @RequestBody Request request){
        try{
            Response<Integer> response = systemService.updateOrder(orderId, products, storeId, userId, address, paymentMethod, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while updating the order", false));
        }
    }

    public ResponseEntity<Response<Boolean>> deleteOrder(int orderId, @RequestBody Request request){
        try{
            Response<Boolean> response = systemService.deleteOrder(orderId, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while deleting the order", false));
        }
    }

    public ResponseEntity<Response<OrderDTO>> viewOrder(int orderId, @RequestBody Request request){
        try{
            Response<OrderDTO> response = systemService.viewOrder(orderId, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while viewing the order", false));
        }
    }

    public ResponseEntity<Response<List<OrderDTO>>> searchOrders(String keyword, @RequestBody Request request){
        try{
            Response<List<OrderDTO>> response = systemService.searchOrders(keyword, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while searching for orders", false));
        }
    }

    public ResponseEntity<Response<List<OrderDTO>>> getOrdersByStoreId(int storeId, @RequestBody Request request){
        try{
            Response<List<OrderDTO>> response = systemService.getOrdersByStoreId(storeId, request.getToken());
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(response);
            }
        }
        catch (Exception e){
            return ResponseEntity.status(500).body(new Response<>(null, "An error occurred while getting orders by store ID", false));
        }
    }


}
