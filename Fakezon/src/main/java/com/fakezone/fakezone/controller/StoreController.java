package com.fakezone.fakezone.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.helpers.UserMsg;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ISystemService systemService;
    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
    private final AuthenticatorAdapter authenticatorAdapter;

    @Autowired
    public StoreController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter){
        this.authenticatorAdapter = authenticatorAdapter;
        this.systemService = systemService;
    }

    @PostMapping("/addStore/{userId}/{storeName}")
    public ResponseEntity<Response<Integer>> addStore(@PathVariable("userId") int userId, @PathVariable("storeName") String storeName, @RequestHeader("Authorization") String token) {
        try{
            logger.info("Received request to add store with name: {} from user this request tocken of: {}", storeName, token);
            if(!authenticatorAdapter.isValid(token)){
                Response<Integer> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Integer> response = systemService.addStore(userId, storeName);
            if(response.isSuccess()){
                return ResponseEntity.ok(response);
            }
            if(response.getErrorType() == ErrorType.INTERNAL_ERROR){
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Integer> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null) ;
            return ResponseEntity.status(500).body(response);
        }

    }


    @PostMapping("/addProductToStore/{storeId}/{requesterId}")
    public ResponseEntity<Response<StoreProductDTO>> addProductToStore(@PathVariable("storeId") int storeId,
                                                            @PathVariable("requesterId") int requesterId,
                                                            @RequestParam("productName") String productName,
                                                            @RequestParam("description") String description,
                                                            @RequestParam("basePrice") double basePrice,
                                                            @RequestParam("quantity") int quantity,
                                                            @RequestParam("category") String category,
                                                            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add product '{}' to store {} by user {} with token {}", productName, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<StoreProductDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<StoreProductDTO> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/viewStore/{storeId}")
    public ResponseEntity<Response<StoreDTO>> viewStore(@PathVariable("storeId") int storeId,
                                                 @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to view store {} with token {}", storeId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<StoreDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            
            Response<StoreDTO> response = systemService.userAccessStore(storeId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<StoreDTO> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/updateProductInStore/{storeId}/{requesterId}/{productId}")
    public ResponseEntity<Response<Void>> updateProductInStore(@PathVariable("storeId") int storeId,
                                                        @PathVariable("requesterId") int requesterId,
                                                        @PathVariable("productId") int productId,
                                                        @RequestParam("basePrice") double basePrice,
                                                        @RequestParam("quantity") int quantity,
                                                        @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to update product {} in store {} by user {} with token {}", productId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.updateProductInStore(storeId, requesterId, productId, basePrice, quantity);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/removeProductFromStore/{storeId}/{requesterId}/{productId}")
    public ResponseEntity<Response<Void>> removeProductFromStore(@PathVariable("storeId") int storeId,
                                                          @PathVariable("requesterId") int requesterId,
                                                          @PathVariable("productId") int productId,
                                                          @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to remove product {} from store {} by user {} with token {}", productId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.removeProductFromStore(storeId, requesterId, productId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addAuctionProductToStore/{storeId}/{requesterId}/{productId}")
    public ResponseEntity<Response<Void>> addAuctionProductToStore(@PathVariable("storeId") int storeId,
                                                            @PathVariable("requesterId") int requesterId,
                                                            @PathVariable("productId") int productId,
                                                            @RequestParam("basePrice") double basePrice,
                                                            @RequestParam("MinutesToEnd") int MinutesToEnd,
                                                            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add auction product {} to store {} by user {} with token {}", productId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addAuctionProductToStore(storeId, requesterId, productId, basePrice, MinutesToEnd);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addBidOnAuctionProductInStore/{storeId}/{requesterId}/{productId}")
    public ResponseEntity<Response<Void>> addBidOnAuctionProductInStore(@PathVariable("storeId") int storeId,
                                                                 @PathVariable("requesterId") int requesterId,
                                                                 @PathVariable("productId") int productId,
                                                                 @RequestParam("bid") double bid,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add bid on auction product {} in store {} by user {} with token {}", productId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, requesterId, productId, bid);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/closeStoreByFounder/{storeId}/{userId}")
    public ResponseEntity<Response<String>> closeStoreByFounder(@PathVariable("storeId") int storeId,
                                                         @PathVariable("userId") int userId,
                                                         @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to close store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<String> response = systemService.closeStoreByFounder(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/closeStoreByAdmin/{storeId}/{adminId}")
    public ResponseEntity<Response<String>> closeStoreByAdmin(@PathVariable("storeId") int storeId,
                                                         @PathVariable("adminId") int adminId,
                                                         @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to close store {} by admin {} with token {}", storeId, adminId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<String> response = systemService.closeStoreByAdmin(storeId, adminId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/ratingStore/{storeId}/{userId}")
    public ResponseEntity<Response<Void>> ratingStore(@PathVariable("storeId") int storeId,
                                               @PathVariable("userId") int userId,
                                               @RequestParam("rating") double rating,
                                               @RequestParam("comment") String comment,
                                               @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to rate store {} by user {} with rating {} and comment '{}' using token {}", storeId, userId, rating, comment, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.ratingStore(storeId, userId, rating, comment);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/ratingStoreProduct/{storeId}/{productId}/{userId}")
    public ResponseEntity<Response<Void>> ratingStoreProduct(@PathVariable("storeId") int storeId,
                                                      @PathVariable("productId") int productId,
                                                      @PathVariable("userId") int userId,
                                                      @RequestParam("rating") double rating,
                                                      @RequestParam("comment") String comment,
                                                      @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to rate product {} in store {} by user {} with rating {} and comment '{}' using token {}", productId, storeId, userId, rating, comment, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, rating, comment);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addStoreOwner/{storeId}/{requesterId}/{ownerId}")
    public ResponseEntity<Response<Void>> addStoreOwner(@PathVariable("storeId") int storeId,
                                                 @PathVariable("requesterId") int requesterId,
                                                 @PathVariable("ownerId") int ownerId,
                                                 @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add owner {} to store {} by user {} with token {}", ownerId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addStoreOwner(storeId, requesterId, ownerId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/removeStoreOwner/{storeId}/{requesterId}/{ownerId}")
    public ResponseEntity<Response<Void>> removeStoreOwner(@PathVariable("storeId") int storeId,
                                                    @PathVariable("requesterId") int requesterId,
                                                    @PathVariable("ownerId") int ownerId,
                                                    @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to remove owner {} from store {} by user {} with token {}", ownerId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.removeStoreOwner(storeId, requesterId, ownerId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addStoreManagerPermissions/{storeId}/{managerId}")
    public ResponseEntity<Response<Void>> addStoreManagerPermissions(@PathVariable("storeId") int storeId,
                                                              @PathVariable("managerId") int managerId,
                                                              @RequestParam("permissions") List<String> permissions,
                                                              @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add permissions {} to manager {} in store {} with token {}", permissions, managerId, storeId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            
            int requesterId = authenticatorAdapter.getUserId(token);
            Response<Void> response = systemService.addStoreManagerPermissions(storeId, managerId, requesterId, permissions.stream()
                    .map(StoreManagerPermission::valueOf)
                    .toList());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addStoreManager/{storeId}/{requesterId}/{managerId}")
    public ResponseEntity<Response<Void>> addStoreManager(@PathVariable("storeId") int storeId,
                                                  @PathVariable("requesterId") int requesterId,
                                                  @PathVariable("managerId") int managerId,
                                                  @RequestParam("permissions") List<String> permissions,
                                                  @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add manager {} to store {} by user {} with token {}", managerId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addStoreManager(storeId, requesterId, managerId, permissions.stream()
                    .map(StoreManagerPermission::valueOf)
                    .toList());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/removeStoreManagerPermissions/{storeId}/{managerId}")
public ResponseEntity<Response<Void>> removeStoreManagerPermissions(@PathVariable("storeId") int storeId,
                                                                 @PathVariable("managerId") int managerId,
                                                                 @RequestParam("permissions") List<String> permissions,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            //List<String> permissions = request.getData();
            logger.info("Received request to remove permissions {} from manager {} in store {} with token {}", permissions, managerId, storeId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            
            int requesterId = authenticatorAdapter.getUserId(token);
            Response<Void> response = systemService.removeStoreManagerPermissions(storeId, requesterId, managerId, permissions.stream()
                    .map(StoreManagerPermission::valueOf)
                    .toList());
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/removeStoreManager/{storeId}/{requesterId}/{managerId}")
    public ResponseEntity<Response<Void>> removeStoreManager(@PathVariable("storeId") int storeId,
                                                      @PathVariable("requesterId") int requesterId,
                                                      @PathVariable("managerId") int managerId,
                                                      @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to remove manager {} from store {} by user {} with token {}", managerId, storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.removeStoreManager(storeId, requesterId, managerId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/sendMessageToUser/{managerId}/{storeId}/{userId}")
    public ResponseEntity<Response<Void>> sendMessageToUser(@PathVariable("managerId") int managerId,
                                                     @PathVariable("storeId") int storeId,
                                                     @PathVariable("userId") int userId,
                                                     @RequestBody Request<String> request,
                                                     @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to send message to user {} from manager {} in store {} with token {}", userId, managerId, storeId, token);
            String message = request.getData();
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.sendMessageToUser(managerId, storeId, userId, message);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getStoreRoles/{storeId}/{requesterId}")
    public ResponseEntity<Response<StoreRolesDTO>> getStoreRoles(@PathVariable("storeId") int storeId,
                                                          @PathVariable("requesterId") int requesterId,
                                                          @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get roles for store {} by user {} with token {}", storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<StoreRolesDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<StoreRolesDTO> response = systemService.getStoreRoles(storeId, requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<StoreRolesDTO> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAllStoreOrders/{storeId}/{userId}")
    public ResponseEntity<Response<List<OrderDTO>>> getAllStoreOrders(@PathVariable("storeId") int storeId,
                                                               @PathVariable("userId") int userId,
                                                               @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get all orders for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<OrderDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<OrderDTO>> response = systemService.getAllStoreOrders(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllStoreOrders: {}", e.getMessage());
            Response<List<OrderDTO>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/acceptAssignment/{storeId}/{userId}")
    public ResponseEntity<Response<String>> acceptAssignment(@PathVariable("storeId") int storeId,
                                                      @PathVariable("userId") int userId,
                                                      @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to accept assignment for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<String> response = systemService.acceptAssignment(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in acceptAssignment: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/declineAssignment/{storeId}/{userId}")
    public ResponseEntity<Response<String>> declineAssignment(@PathVariable("storeId") int storeId,
                                                       @PathVariable("userId") int userId,
                                                       @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to decline assignment for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<String> response = systemService.declineAssignment(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in declineAssignment: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getPendingOwners/{storeId}/{requesterId}")
    public ResponseEntity<Response<List<Integer>>> getPendingOwners(@PathVariable("storeId") int storeId,
                                                             @PathVariable("requesterId") int requesterId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get pending owners for store {} by user {} with token {}", storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<Integer>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<Integer>> response = systemService.getPendingOwners(storeId, requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getPendingOwners: {}", e.getMessage());
            Response<List<Integer>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getPendingManagers/{storeId}/{requesterId}")
    public ResponseEntity<Response<List<Integer>>> getPendingManagers(@PathVariable("storeId") int storeId,
                                                               @PathVariable("requesterId") int requesterId,
                                                               @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get pending managers for store {} by user {} with token {}", storeId, requesterId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<Integer>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<Integer>> response = systemService.getPendingManagers(storeId, requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getPendingManagers: {}", e.getMessage());
            Response<List<Integer>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/getTopRatedProducts/{limit}")
    public ResponseEntity<Response<List<StoreProductDTO>>> getTopRatedProducts(@PathVariable("limit") int limit,
                                                                        @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get top rated products with limit {} and token {}", limit, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<StoreProductDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<StoreProductDTO>> response = systemService.getTopRatedProducts(limit);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getTopRatedProducts: {}", e.getMessage());
            Response<List<StoreProductDTO>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/isStoreOwner/{storeId}/{userId}")
    public ResponseEntity<Response<Boolean>> isStoreOwner(@PathVariable("storeId") int storeId,
                                                       @PathVariable("userId") int userId,
                                                       @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to isStoreOwner for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.isStoreOwner(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in isStoreOwner: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/isStoreManager/{storeId}/{userId}")
    public ResponseEntity<Response<List<StoreManagerPermission>>> isStoreManager(@PathVariable("storeId") int storeId,
                                                       @PathVariable("userId") int userId,
                                                       @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to isStoreManager for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<StoreManagerPermission>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<StoreManagerPermission>> response = systemService.isStoreManager(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in isStoreManager: {}", e.getMessage());
            Response<List<StoreManagerPermission>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAuctionProductsFromStore/{storeId}/{userId}")
    public ResponseEntity<Response<List<AuctionProductDTO>>> getAuctionProductsFromStore(@PathVariable("storeId") int storeId,
                                                       @PathVariable("userId") int userId,
                                                       @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to getAuctionProductsFromStore for store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<AuctionProductDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<AuctionProductDTO>> response = systemService.getAuctionProductsFromStore(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAuctionProductsFromStore: {}", e.getMessage());
            Response<List<AuctionProductDTO>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getMessagesFromUsers/{storeId}/{userId}")
    public ResponseEntity<Response<Map<Integer,UserMsg>>> getMessagesFromUsers(@RequestHeader("Authorization") String token,
                                                                      @PathVariable("storeId") int storeId,
                                                                        @PathVariable("userId") int userId) {
        try {
            logger.info("Received request to get all messages for Store: {} by user {}", storeId, userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Map<Integer,UserMsg>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Map<Integer,UserMsg>> response = systemService.getMessagesFromUsers(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getMessagesFromUsers: {}", e.getMessage());
            Response<Map<Integer,UserMsg>> response = new Response<>(null, "An error occurred while retrieving messages", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/openStore/{storeId}/{userId}")
    public ResponseEntity<Response<Void>> openStore(@PathVariable("storeId") int storeId,
                                                        @PathVariable("userId") int userId,
                                                        @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to open store {} by user {} with token {}", storeId, userId, token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.openStore(storeId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/placeOfferOnStoreProduct/{storeId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> placeOfferOnStoreProduct(@PathVariable("storeId") int storeId,
                                                            @PathVariable("userId") int userId,
                                                            @PathVariable("productId") int productId,
                                                            @RequestParam("offerAmount") double offerAmount,
                                                            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to placeOfferOnStoreProduct '{}' to store {} by user {} with amount {}", productId, storeId, userId, offerAmount);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.placeOfferOnStoreProduct(storeId, userId, productId, offerAmount);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/acceptOfferOnStoreProduct/{storeId}/{ownerId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> acceptOfferOnStoreProduct(@PathVariable("storeId") int storeId,
            @PathVariable("userId") int userId,
            @PathVariable("productId") int productId,
            @PathVariable("ownerId") int ownerId,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to acceptOfferOnStoreProduct '{}' to store {} by user {} with owner {}",
                    productId, storeId, userId, ownerId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.acceptOfferOnStoreProduct(storeId, ownerId, userId, productId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/declineOfferOnStoreProduct/{storeId}/{ownerId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> declineOfferOnStoreProduct(@PathVariable("storeId") int storeId,
            @PathVariable("userId") int userId,
            @PathVariable("productId") int productId,
            @PathVariable("ownerId") int ownerId,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to declineOfferOnStoreProduct '{}' to store {} by user {} with owner {}",
                    productId, storeId, userId, ownerId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.declineOfferOnStoreProduct(storeId, ownerId, userId, productId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/counterOffer/{storeId}/{ownerId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> counterOffer(@PathVariable("storeId") int storeId,
            @PathVariable("userId") int userId,
            @PathVariable("ownerId") int ownerId,
            @PathVariable("productId") int productId,
            @RequestParam("offerAmount") double offerAmount,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to counterOffer '{}' to store {} by user {} with amount {}",
                    productId, storeId, userId, offerAmount);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.counterOffer(storeId, ownerId, userId, productId, offerAmount);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/acceptCounterOffer/{storeId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> acceptCounterOffer(@PathVariable("storeId") int storeId,
            @PathVariable("userId") int userId,
            @PathVariable("productId") int productId,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to acceptCounterOffer '{}' to store {} by user {}",
                    productId, storeId, userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.acceptCounterOffer(storeId, userId, productId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/declineCounterOffer/{storeId}/{userId}/{productId}")
    public ResponseEntity<Response<Void>> declineCounterOffer(@PathVariable("storeId") int storeId,
            @PathVariable("userId") int userId,
            @PathVariable("productId") int productId,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to declineCounterOffer '{}' to store {} by user {}",
                    productId, storeId, userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.declineCounterOffer(storeId, userId, productId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in StoreController: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred at the controller level", false,
                    ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAllStores")
    public ResponseEntity<Response<List<StoreDTO>>> getAllStores(@RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get all stores with token {}", token);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<StoreDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<StoreDTO>> response = systemService.getAllStores();
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            if (response.getErrorType() == ErrorType.INTERNAL_ERROR) {
                return ResponseEntity.status(500).body(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllStores: {}", e.getMessage());
            Response<List<StoreDTO>> response = new Response<>(null, "An error occurred at the controller level", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

}