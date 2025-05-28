package com.fakezone.fakezone.controller;

import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.RequestDataTypes.LoginRequest;
import ApplicationLayer.RequestDataTypes.PurchaseRequest;
import ApplicationLayer.RequestDataTypes.RegisterUserRequest;
import ApplicationLayer.Response;
import DomainLayer.Model.Registered;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.User;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Response<String>> registerUser(@RequestBody Request<RegisterUserRequest> userRequest) {
        try {
            String token = userRequest.getToken();
            String email = userRequest.getData().getEmail();
            String password = userRequest.getData().getPassword();
            String dateOfBirth = userRequest.getData().getDateOfBirth();
            String country = userRequest.getData().getCountry();
            logger.info("Received request to register user with email: {}", email);
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            LocalDate dob = LocalDate.parse(dateOfBirth);
            Response<String> response = systemService.guestRegister(email, password, dob.toString(), country);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in registerUser: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred during registration", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addToBasket/{userId}/{storeId}/{productId}/{quantity}")
    public ResponseEntity<Response<Void>> addToBasket(@RequestHeader("Authorization") String token,
                                               @PathVariable int userId,
                                               @PathVariable int storeId,
                                               @PathVariable int productId,
                                               @PathVariable int quantity) {
        try {
            logger.info("Received request to add product to basket for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in addToBasket: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while adding to basket", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/viewCart/{userId}")
    public ResponseEntity<Response<List<CartItemInfoDTO>>> viewCart(@RequestHeader("Authorization") String token,
                                                             @PathVariable int userId) {
        try {
            logger.info("Received request to view cart for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<CartItemInfoDTO>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<CartItemInfoDTO>> response = systemService.viewCart(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in viewCart: {}", e.getMessage());
            Response<List<CartItemInfoDTO>> response = new Response<>(null, "An error occurred while retrieving the cart", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAllMessages/{userId}")
    public ResponseEntity<Response<HashMap<Integer, String>>> getAllMessages(@RequestHeader("Authorization") String token,
                                                                      @PathVariable int userId) {
        try {
            logger.info("Received request to get all messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAllMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving messages", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/purchaseCart")
    public ResponseEntity<Response<String>> purchaseCart(@RequestHeader("Authorization") String token,
                                                  @RequestBody Request<PurchaseRequest> request) {
        try {
            logger.info("Received request to purchase cart for user: {}", request.getData().getUserId());
            if (!authenticatorAdapter.isValid(token)) {
                Response<String> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
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
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in purchaseCart: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred while purchasing cart", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/sendMessageToStore/{userId}/{storeId}")
    public ResponseEntity<Response<Void>> sendMessageToStore(@PathVariable int userId,
                                                      @PathVariable int storeId,
                                                      @RequestBody Request<String> request) {
        try {
            String token = request.getToken();
            String message = request.getData();
            logger.info("Received request to send message to store: {} from user: {}", storeId, userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in sendMessageToStore: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while sending the message", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/getAssignmentMessages/{userId}")
    public ResponseEntity<Response<HashMap<Integer, String>>> getAssignmentMessages(@RequestHeader("Authorization") String token,
                                                                             @PathVariable int userId) {
        try {
            logger.info("Received request to get assignment messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAssignmentMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAssignmentMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving assignment messages", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAuctionEndedMessages/{userId}")
    public ResponseEntity<Response<HashMap<Integer, String>>> getAuctionEndedMessages(@RequestHeader("Authorization") String token,
                                                                                @PathVariable int userId) {
        try {
            logger.info("Received request to get auction ended messages for user: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<HashMap<Integer, String>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<HashMap<Integer, String>> response = systemService.getAuctionEndedMessages(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAuctionEndedtMessages: {}", e.getMessage());
            Response<HashMap<Integer, String>> response = new Response<>(null, "An error occurred while retrieving auction ended messages", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response<UserDTO>> login(@RequestBody Request<LoginRequest> request){
        try {
            String email = request.getData().getUsername();
            String password = request.getData().getPassword();
            logger.info("Received request to login user with email: {}", email);
            Response<AbstractMap.SimpleEntry<UserDTO, String>> response = systemService.login(email, password);
            Response<UserDTO> userResponse;
            if (response.isSuccess()) {
                String token = response.getToken();
                userResponse = new Response<UserDTO>(response.getData().getKey(), response.getMessage(), true, null, token);
                return ResponseEntity.ok(userResponse);
            }
            userResponse = new Response<>(null, response.getMessage(), false, response.getErrorType(), null);
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)  // Explicitly set Content-Type to JSON
                    .body(userResponse);
        } catch (Exception e) {
            logger.error("Error in login: {}", e.getMessage());
            Response<UserDTO> response = new Response<>(null, "An error occurred during login", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(@RequestBody Request<Integer> request) {
        try {
            String token = request.getToken();
            if(!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Integer id = request.getData();
            logger.info("Received request to logout user with id: {}", id);
            Response<Void> response = systemService.userLogout(id);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in logout: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred during logout", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @GetMapping("/generateGuestToken")
    public ResponseEntity<Response<String>> generateGuestToken() {
        try {
            logger.info("Received request to generate guest token");
            String guestToken = authenticatorAdapter.generateGuestToken();
            Response<String> response = new Response<>(guestToken, "Guest token generated successfully", true, null ,null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in generateGuestToken: {}", e.getMessage());
            Response<String> response = new Response<>(null, "An error occurred while generating guest token", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/isGuestToken")
    public ResponseEntity<Response<Boolean>> isGuestToken(@RequestBody String token){
        try {
            logger.info("Received request to check guest token");
            Response<Boolean> response = new Response<>(authenticatorAdapter.isGuestToken(token), "", true, null, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in isGuestToken: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while generating guest token", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostAuthorize("/unsuspendUser/{requesterId}/{userId}")
    public ResponseEntity<Response<Boolean>> unsuspendUser(@PathVariable("requesterId") int requesterId,@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to unsuspend user with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.unsuspendUser(requesterId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in unsuspendUser: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while unsuspending user", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/isUserSuspended/{userId}")
    public ResponseEntity<Response<Boolean>> isUserSuspended(@PathVariable("userId") int userId, @RequestHeader("Authorization") String token ){
        try {
            logger.info("Received request to check if user is suspended with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.isUserSuspended(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in isUserSuspended: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while checking user suspension", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getSuspensionEndDate/{requesterId}/{userId}")
    public ResponseEntity<Response<LocalDate>> getSuspensionEndDate(@PathVariable("requesterId") int requesterId, @PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get suspension end date for user with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<LocalDate> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<LocalDate> response = systemService.getSuspensionEndDate(requesterId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getSuspensionEndDate: {}", e.getMessage());
            Response<LocalDate> response = new Response<>(null, "An error occurred while retrieving suspension end date", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/getAllSuspendedUsers/{requesterId}")
    public ResponseEntity<Response<List<Registered>>> getAllSuspendedUsers(@PathVariable("requesterId") int requesterId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get all suspended users");
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<Registered>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<Registered>> response = systemService.getAllSuspendedUsers(requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllSuspendedUsers: {}", e.getMessage());
            Response<List<Registered>> response = new Response<>(null, "An error occurred while retrieving suspended users", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }

    }
    @PostMapping("/suspendUser/{requesterId}")
    public ResponseEntity<Response<Integer>> cleanupExpiredSuspensions(@PathVariable("requesterId") int requesterId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to cleanup expired suspensions");
            if (!authenticatorAdapter.isValid(token)) {
                Response<Integer> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Integer> response = systemService.cleanupExpiredSuspensions(requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in cleanupExpiredSuspensions: {}", e.getMessage());
            Response<Integer> response = new Response<>(null, "An error occurred while cleaning up expired suspensions", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/addSystemAdmin/{requesterId}/{userId}")
    public ResponseEntity<Response<Void>> addSystemAdmin(@PathVariable("requesterId") int requesterId,@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to add system admin for user with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.addSystemAdmin(requesterId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in addSystemAdmin: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while adding system admin", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/removeSystemAdmin/{requesterId}/{userId}")
    public ResponseEntity<Response<Boolean>> removeSystemAdmin(@PathVariable("requesterId") int requesterId,@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to remove system admin for user with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.removeSystemAdmin(requesterId, userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in removeSystemAdmin: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while removing system admin", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/isSystemAdmin/{userId}")
    public ResponseEntity<Response<Boolean>> isSystemAdmin(@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to check if user is system admin with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.isSystemAdmin(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in isSystemAdmin: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while checking system admin status", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getAllSystemAdmins/{requesterId}")
    public ResponseEntity<Response<List<Registered>>> getAllSystemAdmins(@PathVariable("requesterId") int requesterId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get all system admins");
            if (!authenticatorAdapter.isValid(token)) {
                Response<List<Registered>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<List<Registered>> response = systemService.getAllSystemAdmins(requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllSystemAdmins: {}", e.getMessage());
            Response<List<Registered>> response = new Response<>(null, "An error occurred while retrieving system admins", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getSystemAdminCount/{requesterId}")
    public ResponseEntity<Response<Integer>> getSystemAdminCount(@PathVariable("requesterId") int requesterId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get system admin count");
            if (!authenticatorAdapter.isValid(token)) {
                Response<Integer> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Integer> response = systemService.getSystemAdminCount(requesterId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getSystemAdminCount: {}", e.getMessage());
            Response<Integer> response = new Response<>(null, "An error occurred while retrieving system admin count", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    
    @PostMapping("/createUnsignedUser")
    public ResponseEntity<Response<UserDTO>> createUnsignedUser(@RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to create unsigned user");
            if (!authenticatorAdapter.isValid(token)) {
                Response<UserDTO> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }

            Response<UserDTO> response = systemService.createUnsignedUser();
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in createUnsignedUser: {}", e.getMessage());
            Response<UserDTO> response = new Response<>(null, "An error occurred while creating unsigned user", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/removeUnsignedUser/{userId}")
    public ResponseEntity<Response<Boolean>> removeUnsignedUser(@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to remove unsigned user with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.removeUnsignedUser(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in removeUnsignedUser: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while removing unsigned user", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getUnsignedUserById/{userId}")
    public ResponseEntity<Response<Boolean>> isUnsignedUser(@PathVariable("userId") int userId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to check if user is unsigned with id: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Boolean> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Boolean> response = systemService.isUnsignedUser(userId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in isUnsignedUser: {}", e.getMessage());
            Response<Boolean> response = new Response<>(null, "An error occurred while checking unsigned user status", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/userRoles/{userId}")
    public ResponseEntity<Response<Map<Integer, Map<String, String>>>> getUserRoles(
            @PathVariable("userId") int userId,
            @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get user roles for userId: {}", userId);
            if (!authenticatorAdapter.isValid(token)) {
                Response<Map<Integer, Map<String, String>>> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }

            // Verify token belongs to userId or user has sufficient permissions
            int requesterId = authenticatorAdapter.getUserId(token);
            if (requesterId != userId) {
                logger.error("Controller - Unauthorized access for userId: {} by requester: {}", userId, requesterId);
                Response<Map<Integer, Map<String, String>>> response = new Response<>(null, "Unauthorized access to user roles", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(403).body(response);
            }
            // Get the raw roles from systemService, passing userId
            Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);
            if (!response.isSuccess()) {
                Response<Map<Integer, Map<String, String>>> transformedResponse = new Response<>(null, response.getMessage(), false, response.getErrorType(), null);
                return ResponseEntity.status(400).body(transformedResponse);
            }

            // Transform HashMap<Integer, IRegisteredRole> into Map<Integer, Map<String, String>>
            Map<Integer, Map<String, String>> transformedRoles = new HashMap<>();
            if (response.getData() != null) {
                for (Map.Entry<Integer, IRegisteredRole> entry : response.getData().entrySet()) {
                    Map<String, String> roleData = new HashMap<>();
                    IRegisteredRole role = entry.getValue();
                    String roleType = (role != null && role.getRoleName() != null) ? role.getRoleName().name() : "UNASSIGNED";
                    roleData.put("type", roleType);
                    transformedRoles.put(entry.getKey(), roleData);
                }
            }

            Response<Map<Integer, Map<String, String>>> transformedResponse = new Response<>(transformedRoles, "Success", true, null, null);
            return ResponseEntity.ok(transformedResponse);
        } catch (Exception e) {
            logger.error("Error in getUserRoles for userId {}: {}", userId, e.getMessage());
            Response<Map<Integer, Map<String, String>>> response = new Response<>(null, "An error occurred while retrieving user roles", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/getAllUnsignedUsers/{adminId}")
    public ResponseEntity<Response<Integer>> getUnsignedUserCount(@PathVariable("adminId") int adminId, @RequestHeader("Authorization") String token) {
        try {
            logger.info("Received request to get unsigned user count");
            if (!authenticatorAdapter.isValid(token)) {
                Response<Integer> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Integer> response = systemService.getUnsignedUserCount(adminId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getUnsignedUserCount: {}", e.getMessage());
            Response<Integer> response = new Response<>(null, "An error occurred while retrieving unsigned user count", false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getCartFinalPrice/{userId}")
    public ResponseEntity<Response<Double>> getCartFinalPrice(
            @PathVariable("userId") int userId,
            @RequestParam("dob")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestHeader("Authorization") String token
    ) {
        try {
            logger.info("Received request to getCartFinalPrice");
            if (!authenticatorAdapter.isValid(token)) {
                Response<Double> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Double> response = systemService.getCartFinalPrice(userId, dob);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in getCartFinalPrice: {}", e.getMessage());
            Response<Double> response = new Response<>(null, "An error occurred while retrieving getCartFinalPrice",
                    false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/removeFromBasket/{userId}/{storeId}/{productId}")
    public ResponseEntity<Response<Void>> removeFromBasket(
            @PathVariable("userId") int userId,
            @PathVariable("storeId") int storeId,
            @PathVariable("productId") int productId,
            @RequestHeader("Authorization") String token
    ){
        try{
            logger.info("Received request to remove item from basket" );
            if (!authenticatorAdapter.isValid(token)) {
                Response<Void> response = new Response<>(null, "Invalid token", false, ErrorType.UNAUTHORIZED, null);
                return ResponseEntity.status(401).body(response);
            }
            Response<Void> response = systemService.removeFromBasket(userId, productId, storeId);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            logger.error("Error in removeFromBasket: {}", e.getMessage());
            Response<Void> response = new Response<>(null, "An error occurred while  removeFromBasket",
                    false, ErrorType.INTERNAL_ERROR, null);
            return ResponseEntity.status(500).body(response);
        }
        
    }


}