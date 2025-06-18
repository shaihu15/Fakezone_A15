package UnitTesting;

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
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.RoleName;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.helpers.StoreMsg;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import com.fakezone.fakezone.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.RegisteredRole;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private ISystemService systemService;

    @Mock
    private AuthenticatorAdapter authenticatorAdapter;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest("test@example.com", "password", "2000-01-01", "USA");
        Request<RegisterUserRequest> request = new Request<>("validToken", registerUserRequest);
        when(authenticatorAdapter.isValid("validToken")).thenReturn(true);
        when(systemService.guestRegister("test@example.com", "password", "2000-01-01", "USA"))
                .thenReturn(new Response<>("Success", "User registered successfully", true, null, null));

        ResponseEntity<Response<String>> response = userController.registerUser(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody().getMessage());
        verify(systemService, times(1)).guestRegister("test@example.com", "password", "2000-01-01", "USA");
    }

    @Test
    void testRegisterUser_InvalidToken() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest("test@example.com", "password", "2000-01-01", "USA");
        Request<RegisterUserRequest> request = new Request<>("invalidToken", registerUserRequest);
        when(authenticatorAdapter.isValid("invalidToken")).thenReturn(false);

        ResponseEntity<Response<String>> response = userController.registerUser(request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).guestRegister(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testViewCart_Success() {
        int userId = 1;
        String token = "validToken";
        int storeId = 1;
        int productId = 1;
        int quantity = 2;
        StoreProductDTO storeProduct = new StoreProductDTO(productId, "Product", 10.0, quantity, 4.5, storeId, null);
        List<StoreProductDTO> cart = List.of(storeProduct);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        Map<StoreProductDTO, Boolean> storeProductMap = Map.of(storeProduct, true);
        StoreDTO storeDTO = mock(StoreDTO.class);

        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cartMap = Map.of(storeDTO, storeProductMap);
        List<CartItemInfoDTO> cartList = new ArrayList<>(List.of(new CartItemInfoDTO(storeId, productId, "test", "Product", quantity, true, 10.0, false)));

        when(systemService.viewCart(userId)).thenReturn(new Response<>(cartList, "Cart retrieved successfully", true, null, null));

        ResponseEntity<Response<List<CartItemInfoDTO>>> response = userController.viewCart(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cart retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).viewCart(userId);
    }

    @Test
    void testViewCart_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<CartItemInfoDTO>>> response = userController.viewCart(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).viewCart(anyInt());
    }
    @Test
    void testViewCart_Failure() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        // Simulate a failed response from the service
        Response<List<CartItemInfoDTO>> failedResponse =
                new Response<>(null, "Failed to retrieve cart", false, ErrorType.INVALID_INPUT, null);
        when(systemService.viewCart(userId)).thenReturn(failedResponse);

        ResponseEntity<Response<List<CartItemInfoDTO>>> response = userController.viewCart(token, userId);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve cart", response.getBody().getMessage());
        verify(systemService, times(1)).viewCart(userId);
    }

    @Test
    void testLogin_Success() {
        String email = "test@example.com";
        String password = "password";
        String token = "validToken";
        UserDTO userDTO = new UserDTO(1, email, 18);
        AbstractMap.SimpleEntry<UserDTO, String> loginData = new AbstractMap.SimpleEntry<>(userDTO, token);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> serviceResponse = new Response<>(loginData, "Login successful", true, null, null);

        LoginRequest loginRequest = new LoginRequest(email, password);
        Request<LoginRequest> request = new Request<>(null, loginRequest);

        when(systemService.login(email, password)).thenReturn(serviceResponse);

        ResponseEntity<Response<UserDTO>> response = userController.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(userDTO, response.getBody().getData());
        verify(systemService, times(1)).login(email, password);
    }

    @Test
    void testLogin_Failure() {
        String email = "test@example.com";
        String password = "wrongPassword";
        Response<AbstractMap.SimpleEntry<UserDTO, String>> serviceResponse = new Response<>(null, "Invalid credentials", false, ErrorType.INVALID_INPUT, null);

        LoginRequest loginRequest = new LoginRequest(email, password);
        Request<LoginRequest> request = new Request<>(null, loginRequest);

        when(systemService.login(email, password)).thenReturn(serviceResponse);

        ResponseEntity<Response<UserDTO>> response = userController.login(request);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        verify(systemService, times(1)).login(email, password);
    }

    @Test
    void testLogin_InternalError() {
        String email = "test@example.com";
        String password = "password";

        LoginRequest loginRequest = new LoginRequest(email, password);
        Request<LoginRequest> request = new Request<>(null, loginRequest);

        when(systemService.login(email, password)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<UserDTO>> response = userController.login(request);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred during login", response.getBody().getMessage());
        verify(systemService, times(1)).login(email, password);
    }

    @Test
    void testLogout_Success() {
        String token = "validToken";
        int userId = 1;
        Request<Integer> request = new Request<>(token, userId);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.userLogout(userId)).thenReturn(new Response<>(null, "Logout successful", true, null, null));

        ResponseEntity<Response<Void>> response = userController.logout(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logout successful", response.getBody().getMessage());
        verify(authenticatorAdapter, times(1)).isValid(token);
        verify(systemService, times(1)).userLogout(userId);
    }

    @Test
    void testLogout_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;
        Request<Integer> request = new Request<>(token, userId);

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = userController.logout(request);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(authenticatorAdapter, times(1)).isValid(token);
        verify(systemService, never()).userLogout(anyInt());
    }

    @Test
    void testLogout_InternalError() {
        String token = "validToken";
        int userId = 1;
        Request<Integer> request = new Request<>(token, userId);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.userLogout(userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Void>> response = userController.logout(request);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred during logout", response.getBody().getMessage());
        verify(authenticatorAdapter, times(1)).isValid(token);
        verify(systemService, times(1)).userLogout(userId);
    }

    @Test
    void testAddToBasket_Success() {
        String token = "validToken";
        int userId = 1;
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(1, "Product", 10.0, 2, 4.5, storeId, null);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addToBasket(userId, product.getProductId(), storeId, product.getQuantity()))
                .thenReturn(new Response<>(null, "Product added to basket", true, null, null));

        ResponseEntity<Response<Void>> response = userController.addToBasket(token, userId, storeId, product.getProductId(), product.getQuantity());

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Product added to basket", response.getBody().getMessage());
        verify(systemService, times(1)).addToBasket(userId, product.getProductId(), storeId, product.getQuantity());
    }

    @Test
    void testAddToBasket_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(1, "Product", 10.0, 2, 4.5, storeId, null);

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = userController.addToBasket(token, userId, storeId, product.getProductId(), product.getQuantity());

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).addToBasket(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void testPurchaseCart_Success() {
        String token = "validToken";
        PurchaseRequest purchaseRequest = new PurchaseRequest(1, "USA", LocalDate.parse("2000-01-01"), PaymentMethod.CREDIT_CARD, "Delivery", "1234567890123456", "John Doe", "12/25", "123", "123 Main St", "John Doe", "Package Details");
        Request<PurchaseRequest> request = new Request<>(token, purchaseRequest);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new Response<>("Purchase successful", "Cart purchased successfully", true, null, null));

        ResponseEntity<Response<String>> response = userController.purchaseCart(token, request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Cart purchased successfully", response.getBody().getMessage());
        verify(systemService, times(1)).purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testPurchaseCart_InvalidToken() {
        String token = "invalidToken";
        PurchaseRequest purchaseRequest = new PurchaseRequest(1, "USA",  LocalDate.parse("2000-01-01"), PaymentMethod.CREDIT_CARD, "Delivery", "1234567890123456", "John Doe", "12/25", "123", "123 Main St", "John Doe", "Package Details");
        Request<PurchaseRequest> request = new Request<>(token, purchaseRequest);

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<String>> response = userController.purchaseCart(token, request);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());    }

    @Test
    void testGetAllMessages_Success() {
        String token = "validToken";
        int userId = 1;
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Message 1", null));

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllMessages(userId)).thenReturn(new Response<>(messages, "Messages retrieved successfully", true, null, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAllMessages(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Messages retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).getAllMessages(userId);
    }

    @Test
    void testGetAllMessages_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAllMessages(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getAllMessages(anyInt());
    }

    @Test
    void testSendMessageToStore_Success() {
        String token = "validToken";
        int userId = 1;
        int storeId = 1;
        String message = "Hello, Store!";
        Request<String> request = new Request<>(token, message);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendMessageToStore(userId, storeId, message))
                .thenReturn(new Response<>(null, "Message sent successfully", true, null, null));

        ResponseEntity<Response<Void>> response = userController.sendMessageToStore(userId, storeId, request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Message sent successfully", response.getBody().getMessage());
        verify(systemService, times(1)).sendMessageToStore(userId, storeId, message);
    }

    @Test
    void testSendMessageToStore_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;
        int storeId = 1;
        String message = "Hello, Store!";
        Request<String> request = new Request<>(token, message);

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = userController.sendMessageToStore(userId, storeId, request);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).sendMessageToStore(anyInt(), anyInt(), anyString());
    }

    @Test
    void testGetAuctionEndedMessages_Success() {
        String token = "validToken";
        int userId = 1;
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Auction ended message", null));

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUserOfferMessages(userId)).thenReturn(new Response<>(messages, "Messages retrieved successfully", true, null, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAuctionEndedMessages(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Messages retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).getUserOfferMessages(userId);
    }

    @Test
    void testGetAuctionEndedMessages_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAuctionEndedMessages(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getUserOfferMessages(anyInt());
    }

    @Test
    void testGetAssignmentMessages_Success() {
        String token = "validToken";
        int userId = 1;
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Assignment message", null));

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAssignmentMessages(userId)).thenReturn(new Response<>(messages, "Messages retrieved successfully", true, null, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAssignmentMessages(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Messages retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).getAssignmentMessages(userId);
    }

    @Test
    void testGetAssignmentMessages_InvalidToken() {
        String token = "invalidToken";
        int userId = 1;

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAssignmentMessages(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getAssignmentMessages(anyInt());
    }

    @Test
    void testIsGuestToken_Success() {
        String token = "guestToken";

        when(authenticatorAdapter.isGuestToken(token)).thenReturn(true);

        ResponseEntity<Response<Boolean>> response = userController.isGuestToken(token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(authenticatorAdapter, times(1)).isGuestToken(token);
    }

    @Test
    void testIsGuestToken_Failure() {
        String token = "invalidToken";

        when(authenticatorAdapter.isGuestToken(token)).thenReturn(false);

        ResponseEntity<Response<Boolean>> response = userController.isGuestToken(token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertFalse(response.getBody().getData());
        verify(authenticatorAdapter, times(1)).isGuestToken(token);
    }

    @Test
    void testUnsuspendUser_Success() {
        int requesterId = 1;
        int userId = 2;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.unsuspendUser(requesterId, userId))
                .thenReturn(new Response<>(true, "User unsuspended successfully", true, null, null));

        ResponseEntity<Response<Boolean>> response = userController.unsuspendUser(requesterId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).unsuspendUser(requesterId, userId);
    }

    @Test
    void testUnsuspendUser_InvalidToken() {
        int requesterId = 1;
        int userId = 2;
        String token = "invalidToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Boolean>> response = userController.unsuspendUser(requesterId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, never()).unsuspendUser(anyInt(), anyInt());
    }

    @Test
    void testIsUserSuspended_Success() {
        int userId = 2;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isUserSuspended(userId))
                .thenReturn(new Response<>(true, "User is suspended", true, null, null));

        ResponseEntity<Response<Boolean>> response = userController.isUserSuspended(userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).isUserSuspended(userId);
    }

    @Test
    void testGetSuspensionEndDate_Success() {
        int requesterId = 1;
        int userId = 2;
        String token = "validToken";
        LocalDate suspensionEndDate = LocalDate.now().plusDays(5);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getSuspensionEndDate(requesterId, userId))
                .thenReturn(new Response<>(suspensionEndDate, "Suspension end date retrieved", true, null, null));

        ResponseEntity<Response<LocalDate>> response = userController.getSuspensionEndDate(requesterId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(suspensionEndDate, response.getBody().getData());
        verify(systemService, times(1)).getSuspensionEndDate(requesterId, userId);
    }

    @Test
    void testGetAllSuspendedUsers_Success() {
        int requesterId = 1;
        String token = "validToken";
        List<Registered> suspendedUsers = List.of(new Registered("username", "email", LocalDate.of(2000, 1, 1), "USA"));        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllSuspendedUsers(requesterId))
                .thenReturn(new Response<>(suspendedUsers, "Suspended users retrieved", true, null, null));

        ResponseEntity<Response<List<Registered>>> response = userController.getAllSuspendedUsers(requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(suspendedUsers, response.getBody().getData());
        verify(systemService, times(1)).getAllSuspendedUsers(requesterId);
    }

    @Test
    void testCleanupExpiredSuspensions_Success() {
        int requesterId = 1;
        String token = "validToken";
        int cleanedCount = 5;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.cleanupExpiredSuspensions(requesterId))
                .thenReturn(new Response<>(cleanedCount, "Expired suspensions cleaned", true, null, null));

        ResponseEntity<Response<Integer>> response = userController.cleanupExpiredSuspensions(requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(cleanedCount, response.getBody().getData());
        verify(systemService, times(1)).cleanupExpiredSuspensions(requesterId);
    }

    @Test
    void testAddSystemAdmin_Success() {
        int requesterId = 1;
        int userId = 2;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addSystemAdmin(requesterId, userId))
                .thenReturn(new Response<>(null, "System admin added successfully", true, null, null));

        ResponseEntity<Response<Void>> response = userController.addSystemAdmin(requesterId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("System admin added successfully", response.getBody().getMessage());
        verify(systemService, times(1)).addSystemAdmin(requesterId, userId);
    }

    @Test
    void testAddSystemAdmin_InvalidToken() {
        int requesterId = 1;
        int userId = 2;
        String token = "invalidToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = userController.addSystemAdmin(requesterId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).addSystemAdmin(anyInt(), anyInt());
    }

    @Test
    void testRemoveSystemAdmin_Success() {
        int requesterId = 1;
        int userId = 2;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeSystemAdmin(requesterId, userId))
                .thenReturn(new Response<>(true, "System admin removed successfully", true, null, null));

        ResponseEntity<Response<Boolean>> response = userController.removeSystemAdmin(requesterId, userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).removeSystemAdmin(requesterId, userId);
    }

    @Test
    void testRemoveSystemAdmin_InvalidToken() {
        int requesterId = 1;
        int userId = 2;
        String token = "invalidToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Boolean>> response = userController.removeSystemAdmin(requesterId, userId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).removeSystemAdmin(anyInt(), anyInt());
    }

    @Test
    void testGetAllSystemAdmins_Success() {
        int requesterId = 1;
        String token = "validToken";
        List<Registered> admins = List.of(new Registered("username", "email", LocalDate.of(2000, 1, 1), "USA"));        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllSystemAdmins(requesterId))
                .thenReturn(new Response<>(admins, "System admins retrieved successfully", true, null, null));

        ResponseEntity<Response<List<Registered>>> response = userController.getAllSystemAdmins(requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(admins, response.getBody().getData());
        verify(systemService, times(1)).getAllSystemAdmins(requesterId);
    }

    @Test
    void testGetSystemAdminCount_Success() {
        int requesterId = 1;
        String token = "validToken";
        int adminCount = 5;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getSystemAdminCount(requesterId))
                .thenReturn(new Response<>(adminCount, "System admin count retrieved successfully", true, null, null));

        ResponseEntity<Response<Integer>> response = userController.getSystemAdminCount(requesterId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(adminCount, response.getBody().getData());
        verify(systemService, times(1)).getSystemAdminCount(requesterId);
    }
    @Test
    void testRegisterUser_Failure() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest("test@example.com", "password", "2000-01-01", "USA");
        Request<RegisterUserRequest> request = new Request<>("validToken", registerUserRequest);
        when(authenticatorAdapter.isValid("validToken")).thenReturn(true);
        when(systemService.guestRegister(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new Response<>(null, "Registration failed", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<String>> response = userController.registerUser(request);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Registration failed", response.getBody().getMessage());
        verify(systemService, times(1)).guestRegister(anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testAddToBasket_Failure() {
        String token = "validToken";
        int userId = 1, storeId = 1, productId = 1, quantity = 2;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addToBasket(userId, productId, storeId, quantity))
                .thenReturn(new Response<>(null, "Failed to add to basket", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Void>> response = userController.addToBasket(token, userId, storeId, productId, quantity);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to add to basket", response.getBody().getMessage());
        verify(systemService, times(1)).addToBasket(userId, productId, storeId, quantity);
    }
    
    @Test
    void testPurchaseCart_Failure() {
        String token = "validToken";
        PurchaseRequest purchaseRequest = new PurchaseRequest(1, "USA", LocalDate.parse("2000-01-01"), PaymentMethod.CREDIT_CARD, "Delivery", "1234567890123456", "John Doe", "12/25", "123", "123 Main St", "John Doe", "Package Details");
        Request<PurchaseRequest> request = new Request<>(token, purchaseRequest);
    
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new Response<>(null, "Purchase failed", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<String>> response = userController.purchaseCart(token, request);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Purchase failed", response.getBody().getMessage());
        verify(systemService, times(1)).purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void testGetAllMessages_Failure() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllMessages(userId))
                .thenReturn(new Response<>(null, "Failed to retrieve messages", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAllMessages(token, userId);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve messages", response.getBody().getMessage());
        verify(systemService, times(1)).getAllMessages(userId);
    }
    
    @Test
    void testSendMessageToStore_Failure() {
        String token = "validToken";
        int userId = 1, storeId = 1;
        String message = "Hello, Store!";
        Request<String> request = new Request<>(token, message);
    
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendMessageToStore(userId, storeId, message))
                .thenReturn(new Response<>(null, "Failed to send message", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Void>> response = userController.sendMessageToStore(userId, storeId, request);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send message", response.getBody().getMessage());
        verify(systemService, times(1)).sendMessageToStore(userId, storeId, message);
    }
    
    @Test
    void testGetAuctionEndedMessages_Failure() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUserOfferMessages(userId))
                .thenReturn(new Response<>(null, "Failed to retrieve auction ended messages", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAuctionEndedMessages(token, userId);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve auction ended messages", response.getBody().getMessage());
        verify(systemService, times(1)).getUserOfferMessages(userId);
    }
    
    @Test
    void testGetAssignmentMessages_Failure() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAssignmentMessages(userId))
                .thenReturn(new Response<>(null, "Failed to retrieve assignment messages", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAssignmentMessages(token, userId);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to retrieve assignment messages", response.getBody().getMessage());
        verify(systemService, times(1)).getAssignmentMessages(userId);
    }
    
    @Test
    void testGetAllMessages_InternalError() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAllMessages(userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAllMessages(token, userId);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving messages", response.getBody().getMessage());
        verify(systemService, times(1)).getAllMessages(userId);
    }
    
    // --- Example for getAuctionEndedMessages ---
    @Test
    void testGetAuctionEndedMessages_InternalError() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUserOfferMessages(userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAuctionEndedMessages(token, userId);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving auction ended messages", response.getBody().getMessage());
        verify(systemService, times(1)).getUserOfferMessages(userId);
    }
    
    // --- Example for getAssignmentMessages ---
    @Test
    void testGetAssignmentMessages_InternalError() {
        String token = "validToken";
        int userId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getAssignmentMessages(userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Map<Integer, StoreMsg>>> response = userController.getAssignmentMessages(token, userId);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving assignment messages", response.getBody().getMessage());
        verify(systemService, times(1)).getAssignmentMessages(userId);
    }
    
    // --- Example for sendMessageToStore ---
    @Test
    void testSendMessageToStore_InternalError() {
        String token = "validToken";
        int userId = 1, storeId = 1;
        String message = "Hello, Store!";
        Request<String> request = new Request<>(token, message);
    
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.sendMessageToStore(userId, storeId, message)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Void>> response = userController.sendMessageToStore(userId, storeId, request);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while sending the message", response.getBody().getMessage());
        verify(systemService, times(1)).sendMessageToStore(userId, storeId, message);
    }
    
    // --- Example for addToBasket ---
    @Test
    void testAddToBasket_InternalError() {
        String token = "validToken";
        int userId = 1, storeId = 1, productId = 1, quantity = 2;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.addToBasket(userId, productId, storeId, quantity)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Void>> response = userController.addToBasket(token, userId, storeId, productId, quantity);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while adding to basket", response.getBody().getMessage());
        verify(systemService, times(1)).addToBasket(userId, productId, storeId, quantity);
    }
    
    // --- Example for purchaseCart ---
    @Test
    void testPurchaseCart_InternalError() {
        String token = "validToken";
        PurchaseRequest purchaseRequest = new PurchaseRequest(1, "USA", LocalDate.parse("2000-01-01"), PaymentMethod.CREDIT_CARD, "Delivery", "1234567890123456", "John Doe", "12/25", "123", "123 Main St", "John Doe", "Package Details");
        Request<PurchaseRequest> request = new Request<>(token, purchaseRequest);
    
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<String>> response = userController.purchaseCart(token, request);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while purchasing cart", response.getBody().getMessage());
        verify(systemService, times(1)).purchaseCart(anyInt(), anyString(), any(LocalDate.class), any(PaymentMethod.class), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
    @Test
    void testIsSystemAdmin_Success() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isSystemAdmin(userId)).thenReturn(new Response<>(true, "User is system admin", true, null, null));
    
        ResponseEntity<Response<Boolean>> response = userController.isSystemAdmin(userId, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        assertEquals("User is system admin", response.getBody().getMessage());
        verify(systemService, times(1)).isSystemAdmin(userId);
    }
    
    @Test
    void testIsSystemAdmin_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Boolean>> response = userController.isSystemAdmin(userId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).isSystemAdmin(anyInt());
    }
    
    @Test
    void testIsSystemAdmin_Failure() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isSystemAdmin(userId)).thenReturn(new Response<>(false, "User is not system admin", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Boolean>> response = userController.isSystemAdmin(userId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User is not system admin", response.getBody().getMessage());
        verify(systemService, times(1)).isSystemAdmin(userId);
    }
    
    @Test
    void testIsSystemAdmin_InternalError() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isSystemAdmin(userId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Boolean>> response = userController.isSystemAdmin(userId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while checking system admin status", response.getBody().getMessage());
        verify(systemService, times(1)).isSystemAdmin(userId);
    }

    
    // --- removeUnsignedUser ---
    @Test
    void testRemoveUnsignedUser_Success() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeUnsignedUser(userId)).thenReturn(new Response<>(true, "Removed", true, null, null));
    
        ResponseEntity<Response<Boolean>> response = userController.removeUnsignedUser(userId, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        assertEquals("Removed", response.getBody().getMessage());
        verify(systemService, times(1)).removeUnsignedUser(userId);
    }
    
    @Test
    void testRemoveUnsignedUser_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Boolean>> response = userController.removeUnsignedUser(userId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).removeUnsignedUser(anyInt());
    }
    
    @Test
    void testRemoveUnsignedUser_Failure() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeUnsignedUser(userId)).thenReturn(new Response<>(false, "Not removed", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Boolean>> response = userController.removeUnsignedUser(userId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Not removed", response.getBody().getMessage());
        verify(systemService, times(1)).removeUnsignedUser(userId);
    }
    
    @Test
    void testRemoveUnsignedUser_InternalError() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeUnsignedUser(userId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Boolean>> response = userController.removeUnsignedUser(userId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while removing unsigned user", response.getBody().getMessage());
        verify(systemService, times(1)).removeUnsignedUser(userId);
    }
    
    // --- isUnsignedUser ---
    @Test
    void testIsUnsignedUser_Success() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isUnsignedUser(userId)).thenReturn(new Response<>(true, "Is unsigned", true, null, null));
    
        ResponseEntity<Response<Boolean>> response = userController.isUnsignedUser(userId, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        assertEquals("Is unsigned", response.getBody().getMessage());
        verify(systemService, times(1)).isUnsignedUser(userId);
    }
    
    @Test
    void testIsUnsignedUser_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Boolean>> response = userController.isUnsignedUser(userId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).isUnsignedUser(anyInt());
    }
    
    @Test
    void testIsUnsignedUser_Failure() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isUnsignedUser(userId)).thenReturn(new Response<>(false, "Not unsigned", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Boolean>> response = userController.isUnsignedUser(userId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Not unsigned", response.getBody().getMessage());
        verify(systemService, times(1)).isUnsignedUser(userId);
    }
    
    @Test
    void testIsUnsignedUser_InternalError() {
        int userId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.isUnsignedUser(userId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Boolean>> response = userController.isUnsignedUser(userId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while checking unsigned user status", response.getBody().getMessage());
        verify(systemService, times(1)).isUnsignedUser(userId);
    }
    
    // --- getUserRoles ---
    @Test
    void testGetUserRoles_Success() {
        int userId = 1;
        String token = "validToken";
        int requesterId = 1;
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        IRegisteredRole mockRole = mock(IRegisteredRole.class); // <-- FIXED
        when(mockRole.getRoleName()).thenReturn(RoleName.STORE_OWNER);
        roles.put(1, mockRole);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(requesterId);
        when(systemService.getUserRoles(userId)).thenReturn(new Response<>(roles, "Success", true, null, null));

        ResponseEntity<Response<Map<Integer, Map<String, String>>>> response = userController.getUserRoles(userId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals("STORE_OWNER", response.getBody().getData().get(1).get("type"));
        verify(systemService, times(1)).getUserRoles(userId);
    }
    @Test
    void testGetUserRoles_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Map<Integer, Map<String, String>>>> response = userController.getUserRoles(userId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getUserRoles(anyInt());
    }
    
    @Test
    void testGetUserRoles_Forbidden() {
        int userId = 1;
        String token = "validToken";
        int requesterId = 2; // not the same as userId
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(requesterId);
    
        ResponseEntity<Response<Map<Integer, Map<String, String>>>> response = userController.getUserRoles(userId, token);
    
        assertEquals(403, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Unauthorized access to user roles", response.getBody().getMessage());
        verify(systemService, never()).getUserRoles(anyInt());
    }
    
    @Test
    void testGetUserRoles_Failure() {
        int userId = 1;
        String token = "validToken";
        int requesterId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(requesterId);
        when(systemService.getUserRoles(userId)).thenReturn(new Response<>(null, "Failed", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Map<Integer, Map<String, String>>>> response = userController.getUserRoles(userId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed", response.getBody().getMessage());
        verify(systemService, times(1)).getUserRoles(userId);
    }
    
    @Test
    void testGetUserRoles_InternalError() {
        int userId = 1;
        String token = "validToken";
        int requesterId = 1;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(requesterId);
        when(systemService.getUserRoles(userId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Map<Integer, Map<String, String>>>> response = userController.getUserRoles(userId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving user roles", response.getBody().getMessage());
        verify(systemService, times(1)).getUserRoles(userId);
    }
    
    // --- getUnsignedUserCount ---
    @Test
    void testGetUnsignedUserCount_Success() {
        int adminId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUnsignedUserCount(adminId)).thenReturn(new Response<>(5, "Success", true, null, null));
    
        ResponseEntity<Response<Integer>> response = userController.getUnsignedUserCount(adminId, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(5, response.getBody().getData());
        assertEquals("Success", response.getBody().getMessage());
        verify(systemService, times(1)).getUnsignedUserCount(adminId);
    }
    
    @Test
    void testGetUnsignedUserCount_InvalidToken() {
        int adminId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Integer>> response = userController.getUnsignedUserCount(adminId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getUnsignedUserCount(anyInt());
    }
    
    @Test
    void testGetUnsignedUserCount_Failure() {
        int adminId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUnsignedUserCount(adminId)).thenReturn(new Response<>(null, "Failed", false, ErrorType.INVALID_INPUT, null));
    
        ResponseEntity<Response<Integer>> response = userController.getUnsignedUserCount(adminId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed", response.getBody().getMessage());
        verify(systemService, times(1)).getUnsignedUserCount(adminId);
    }
    
    @Test
    void testGetUnsignedUserCount_InternalError() {
        int adminId = 1;
        String token = "validToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getUnsignedUserCount(adminId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<Integer>> response = userController.getUnsignedUserCount(adminId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving unsigned user count", response.getBody().getMessage());
        verify(systemService, times(1)).getUnsignedUserCount(adminId);
    }
    @Test
    void testGetCartFinalPrice_Success() {
        int userId = 1;
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String token = "validToken";
        double finalPrice = 99.99;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getCartFinalPrice(userId, dob))
                .thenReturn(new Response<>(finalPrice, "Final price calculated", true, null, null));

        ResponseEntity<Response<Double>> response = userController.getCartFinalPrice(userId, dob, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(finalPrice, response.getBody().getData());
        assertEquals("Final price calculated", response.getBody().getMessage());
        verify(systemService, times(1)).getCartFinalPrice(userId, dob);
    }

    @Test
    void testGetCartFinalPrice_InvalidToken() {
        int userId = 1;
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String token = "invalidToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Double>> response = userController.getCartFinalPrice(userId, dob, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getCartFinalPrice(anyInt(), any(LocalDate.class));
    }

    @Test
    void testGetCartFinalPrice_Failure() {
        int userId = 1;
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getCartFinalPrice(userId, dob))
                .thenReturn(new Response<>(null, "Calculation failed", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Double>> response = userController.getCartFinalPrice(userId, dob, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Calculation failed", response.getBody().getMessage());
        verify(systemService, times(1)).getCartFinalPrice(userId, dob);
    }

    @Test
    void testGetCartFinalPrice_InternalError() {
        int userId = 1;
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getCartFinalPrice(userId, dob)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Double>> response = userController.getCartFinalPrice(userId, dob, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while retrieving getCartFinalPrice", response.getBody().getMessage());
        verify(systemService, times(1)).getCartFinalPrice(userId, dob);
    }

    @Test
    void testRemoveFromBasket_Success() {
        int userId = 1;
        int storeId = 2;
        int productId = 3;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeFromBasket(userId, productId, storeId))
                .thenReturn(new Response<>(null, "Item removed from basket", true, null, null));

        ResponseEntity<Response<Void>> response = userController.removeFromBasket(userId, storeId, productId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Item removed from basket", response.getBody().getMessage());
        verify(systemService, times(1)).removeFromBasket(userId, productId, storeId);
    }

    @Test
    void testRemoveFromBasket_InvalidToken() {
        int userId = 1;
        int storeId = 2;
        int productId = 3;
        String token = "invalidToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Void>> response = userController.removeFromBasket(userId, storeId, productId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).removeFromBasket(anyInt(), anyInt(), anyInt());
    }

    @Test
    void testRemoveFromBasket_Failure() {
        int userId = 1;
        int storeId = 2;
        int productId = 3;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeFromBasket(userId, productId, storeId))
                .thenReturn(new Response<>(null, "Remove failed", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Void>> response = userController.removeFromBasket(userId, storeId, productId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Remove failed", response.getBody().getMessage());
        verify(systemService, times(1)).removeFromBasket(userId, productId, storeId);
    }

    @Test
    void testRemoveFromBasket_InternalError() {
        int userId = 1;
        int storeId = 2;
        int productId = 3;
        String token = "validToken";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.removeFromBasket(userId, productId, storeId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Void>> response = userController.removeFromBasket(userId, storeId, productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while  removeFromBasket", response.getBody().getMessage());
        verify(systemService, times(1)).removeFromBasket(userId, productId, storeId);
    }

    

}