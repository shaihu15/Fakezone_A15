package UnitTesting;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.RequestDataTypes.RegisterUserRequest;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import com.fakezone.fakezone.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .thenReturn(new Response<>("Success", "User registered successfully", true));

        ResponseEntity<Response<UserDTO>> response = userController.registerUser(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody().getMessage());
        verify(systemService, times(1)).guestRegister("test@example.com", "password", "2000-01-01", "USA");
    }

    @Test
    void testRegisterUser_InvalidToken() {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest("test@example.com", "password", "2000-01-01", "USA");
        Request<RegisterUserRequest> request = new Request<>("invalidToken", registerUserRequest);
        when(authenticatorAdapter.isValid("invalidToken")).thenReturn(false);

        ResponseEntity<Response<UserDTO>> response = userController.registerUser(request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).guestRegister(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testViewCart_Success() {
        int userId = 1;
        String token = "validToken";
        List<StoreProductDTO> cart = List.of(new StoreProductDTO(1, "Product", 10.0, 5, 4.5, 1, null));
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.viewCart(userId)).thenReturn(new Response<>(cart, "Cart retrieved successfully", true));

        ResponseEntity<Response<List<StoreProductDTO>>> response = userController.viewCart(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Cart retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).viewCart(userId);
    }

    @Test
    void testViewCart_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<StoreProductDTO>>> response = userController.viewCart(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).viewCart(anyInt());
    }

    @Test
    void testGetOrdersByUser_Success() {
        int userId = 1;
        String token = "validToken";
        List<OrderDTO> orders = List.of(new OrderDTO(1, 1, 1, List.of(), "Pending", "123 Main St", "Credit Card"));        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getOrdersByUser(userId)).thenReturn(new Response<>(orders, "Orders retrieved successfully", true));

        ResponseEntity<Response<List<OrderDTO>>> response = userController.getOrdersByUser(token, userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Orders retrieved successfully", response.getBody().getMessage());
        verify(systemService, times(1)).getOrdersByUser(userId);
    }

    @Test
    void testGetOrdersByUser_InvalidToken() {
        int userId = 1;
        String token = "invalidToken";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<OrderDTO>>> response = userController.getOrdersByUser(token, userId);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getOrdersByUser(anyInt());
    }
}