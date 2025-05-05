package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.RequestDataTypes.PurchaseRequest;
import ApplicationLayer.RequestDataTypes.RegisterUserRequest;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final ISystemService systemService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthenticatorAdapter authenticatorAdapter;

    @Autowired
    public UserController(ISystemService systemService, AuthenticatorAdapter authenticatorAdapter) {
        this.systemService = systemService;
        this.authenticatorAdapter = authenticatorAdapter;
    }

    @PostMapping("/register")
    ResponseEntity<Response<UserDTO>> registerUser(@RequestBody Request<RegisterUserRequest> userRequest) {
        try {
            String token = userRequest.getToken();
            String email = userRequest.getData().getEmail();
            String password = userRequest.getData().getPassword();
            String dateOfBirth = userRequest.getData().getDateOfBirth();
            String country = userRequest.getData().getCountry();
            logger.info("Received request to register user with email: {}", email);
            if (!authenticatorAdapter.isValid(token)) {
                Response<UserDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            LocalDate dob = LocalDate.parse(dateOfBirth);
            Response<String> response = systemService.guestRegister(email, password, dob.toString(), country);
            if (response.isSuccess()) {
                return ResponseEntity.ok(new Response<>(null, "User registered successfully", true));
            }
            return ResponseEntity.status(400).body(new Response<>(null, response.getMessage(), false));
        } catch (Exception e) {
            logger.error("Error in registerUser: {}", e.getMessage());
            Response<UserDTO> response = new Response<>(null, "An error occurred during registration", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addToBasket/{userId}/{storeId}")
    ResponseEntity<Response<Void>> addToBasket(@RequestHeader("Authorization") String token,
                                               @PathVariable int userId,
                                               @PathVariable int storeId,
                                               @RequestBody StoreProductDTO product) {
        try {
            logger.info("Received request to add product to basket for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addToBasket(userId, product.getProductId(), storeId, product.getQuantity());
            if (response.isSuccess()) {
                return ResponseEntity.ok(new Response<>(null, "Product added to basket successfully", true));
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in addToBasket: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while adding to basket", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/viewCart/{userId}")
    ResponseEntity<Response<List<StoreProductDTO>>> viewCart(@RequestHeader("Authorization") String token,
                                                             @PathVariable int userId) {
        try {
            logger.info("Received request to view cart for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<StoreProductDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<StoreProductDTO>> response = systemService.viewCart(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in viewCart: {}", e.getMessage());
            Response<List<StoreProductDTO>> response = new Response<>(null, "An error occurred while retrieving the cart", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAllMessages/{userId}")
    ResponseEntity<Response<HashMap<Integer, String>>> getAllMessages(@RequestHeader("Authorization") String token,
                                                                      @PathVariable int userId) {
        try {
            logger.info("Received request to get all messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAllMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving messages", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/purchaseCart")
    ResponseEntity<Response<String>> purchaseCart(@RequestHeader("Authorization") String token,
                                                  @RequestBody Request<PurchaseRequest> request) {
        try {
            logger.info("Received request to purchase cart for user: {}", request.getData().getUserId());
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<String> response = systemService.purchaseCart(request.getData().getUserId(),
                    request.getData().getCountry(),
                    request.getData().getDob(),
                    request.getData().getPaymentMethod(),
                    request.getData().getDeliveryMethod(),
                    request.getData().getCardNumber(),
                    request.getData().getCardHolder(),
                    request.getData().getExpDate(),
                    request.getData().getCvv(),
                    request.getData().getAddress(),
                    request.getData().getRecipient(),
                    request.getData().getPackageDetails());
            if (response.isSuccess()) {
                return ResponseEntity.ok(new Response<>(null, "purchase cart finished successfully", true));
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in purchaseCart: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred while purchasing cart", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/sendMessageToStore/{userId}/{storeId}")
    ResponseEntity<Response<Void>> sendMessageToStore(@PathVariable int userId,
                                                      @PathVariable int storeId,
                                                      @RequestBody Request<String> request) {
        try {
            String token = request.getToken();
            String message = request.getData();
            logger.info("Received request to send message to store: {} from user: {}", storeId, userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);
            if (response.isSuccess()) {
                return ResponseEntity.ok(new Response<>(null, "Message sent successfully", true));
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in sendMessageToStore: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while sending the message", false);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/getAssignmentMessages/{userId}")
    ResponseEntity<Response<HashMap<Integer, String>>> getAssignmentMessages(@RequestHeader("Authorization") String token,
                                                                             @PathVariable int userId) {
        try {
            logger.info("Received request to get assignment messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAssignmentMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAssignmentMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving assignment messages", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAuctionEndedtMessages/{userId}")
    ResponseEntity<Response<HashMap<Integer, String>>> getAuctionEndedtMessages(@RequestHeader("Authorization") String token,
                                                                                @PathVariable int userId) {
        try {
            logger.info("Received request to get auction ended messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAuctionEndedtMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAuctionEndedtMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving auction ended messages", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getOrdersByUser/{userId}")
    ResponseEntity<Response<List<OrderDTO>>> getOrdersByUser(@RequestHeader("Authorization") String token,
                                                             @PathVariable int userId) {
        try {
            logger.info("Received request to get orders for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<OrderDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<OrderDTO>> response = systemService.getOrdersByUser(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getOrdersByUser: {}", e.getMessage());
            Response<List<OrderDTO>> response = new Response<>(null, "An error occurred while retrieving orders", false);
            return ResponseEntity.status(500).body(response);
        }
    }
}