package UnitTesting;


import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Response;
import ApplicationLayer.Interfaces.ISystemService;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

import com.fakezone.fakezone.controller.OrderController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.fakezone.fakezone.controller.OrderController;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

class OrderControllerTest {

    @Mock
    private ISystemService systemService;

    @Mock
    private AuthenticatorAdapter authenticatorAdapter;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderController = new OrderController(systemService, authenticatorAdapter);
    }

    @Test
    void deleteOrder_Success() {
        int orderId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.deleteOrder(orderId, userId)).thenReturn(new Response<>(true, "Deleted", true, null, null));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).deleteOrder(orderId, userId);
    }

    @Test
    void deleteOrder_Failure() {
        int orderId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.deleteOrder(orderId, userId)).thenReturn(new Response<>(false, "Not deleted", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Not deleted", response.getBody().getMessage());
        verify(systemService, times(1)).deleteOrder(orderId, userId);
    }

    @Test
    void deleteOrder_InvalidToken() {
        int orderId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).deleteOrder(anyInt(), anyInt());
    }

    @Test
    void deleteOrder_Exception() {
        int orderId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;


        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.deleteOrder(orderId, userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while deleting the order", response.getBody().getMessage());
        verify(systemService, times(1)).deleteOrder(orderId, userId);
    }

    @Test
    void viewOrder_Success() {
        int orderId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        int userId = 42;
        OrderDTO orderDTO = new OrderDTO(1, 1, 1, List.of(), "Pending", "address", "payment");

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.viewOrder(orderId, userId)).thenReturn(new Response<>(orderDTO, "Order retrieved", true, null, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orderDTO, response.getBody().getData());
        verify(systemService, times(1)).viewOrder(orderId, userId);
    }

    @Test
    void viewOrder_Failure() {
        int orderId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.viewOrder(orderId, userId)).thenReturn(new Response<>(null, "Order not found", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Order not found", response.getBody().getMessage());
        verify(systemService, times(1)).viewOrder(orderId, userId);
    }

    @Test
    void viewOrder_InvalidToken() {
        int orderId = 1;
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).viewOrder(anyInt(), anyInt());
    }

    @Test
    void viewOrder_Exception() {
        int orderId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.viewOrder(orderId, userId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An error occurred while viewing the order", response.getBody().getMessage());
        verify(systemService, times(1)).viewOrder(orderId, userId);
    }

    @Test
    void searchOrders_Success() {
        String keyword = "test";
        String token = "valid-token";
        List<OrderDTO> orders = List.of(new OrderDTO(1, 1, 1, List.of(), "Pending", "address", "payment"));
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.searchOrders(keyword, userId)).thenReturn(new Response<>(orders, "Orders found", true, null, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orders, response.getBody().getData());
        verify(systemService, times(1)).searchOrders(keyword, userId);
    }

    @Test
    void searchOrders_Failure() {
        String keyword = "test";
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.searchOrders(keyword, userId)).thenReturn(new Response<>(null, "No orders found", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("No orders found", response.getBody().getMessage());
        verify(systemService, times(1)).searchOrders(keyword, userId);
    }


    @Test
    void getOrdersByStoreId_Success() {
        int storeId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;
        List<OrderDTO> orders = List.of(new OrderDTO(1, 1, 1, List.of(), "Pending", "address", "payment"));

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.getOrdersByStoreId(storeId, userId)).thenReturn(new Response<>(orders, "Orders found", true, null, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orders, response.getBody().getData());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, userId);
    }

    @Test
    void getOrdersByStoreId_Failure() {
        int storeId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        int userId = 42;
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(authenticatorAdapter.getUserId(token)).thenReturn(userId);
        when(systemService.getOrdersByStoreId(storeId, userId)).thenReturn(new Response<>(null, "No orders found", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("No orders found", response.getBody().getMessage());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, userId);
    }

    @Test
    void getOrdersByStoreId_InvalidToken() {
        int storeId = 1;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getOrdersByStoreId(anyInt(), anyInt());
    }

    @Test
    void testViewOrder_NullInput() {
        int orderId = 0;
        int userId = 0;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        when(systemService.viewOrder(orderId, userId))
                .thenReturn(new Response<>(null, "Invalid input", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(0)).viewOrder(orderId, userId);
    }

}