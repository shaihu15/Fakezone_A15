package UnitTesting;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Response;
import ApplicationLayer.Interfaces.ISystemService;
import com.fakezone.fakezone.controller.OrderController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private ISystemService systemService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteOrder_Success() {
        int orderId = 1;
        String token = "valid-token";

        when(systemService.deleteOrder(orderId, token))
                .thenReturn(new Response<>(true, "Order deleted successfully", true, null, null));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).deleteOrder(orderId, token);
    }

    @Test
    void testDeleteOrder_Failure() {
        int orderId = 1;
        String token = "valid-token";

        when(systemService.deleteOrder(orderId, token)).thenReturn(new Response<>(false, "Order not found", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertFalse(response.getBody().getData());
        verify(systemService, times(1)).deleteOrder(orderId, token);
    }

    @Test
    void testDeleteOrder_Exception() {
        int orderId = 1;
        String token = "valid-token";

        when(systemService.deleteOrder(orderId, token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, times(1)).deleteOrder(orderId, token);
    }

    @Test
    void testViewOrder_Success() {
        int orderId = 1;
        String token = "valid-token";
        OrderDTO orderDTO = new OrderDTO(1, 1, 1, List.of(), "2023-01-01", "2023-01-05", "Pending");

        when(systemService.viewOrder(orderId, token))
                .thenReturn(new Response<>(orderDTO, "Order retrieved successfully", true, null, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orderDTO, response.getBody().getData());
        verify(systemService, times(1)).viewOrder(orderId, token);
    }

    @Test
    void testViewOrder_Failure() {
        int orderId = 1;
        String token = "valid-token";

        when(systemService.viewOrder(orderId, token))
                .thenReturn(new Response<>(null, "Order not found", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).viewOrder(orderId, token);
    }

    @Test
    void testViewOrder_Exception() {
        int orderId = 1;
        String token = "valid-token";

        when(systemService.viewOrder(orderId, token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, times(1)).viewOrder(orderId, token);
    }

    @Test
    void testSearchOrders_Success() {
        String keyword = "test";
        String token = "valid-token";
        List<OrderDTO> orders = List.of(new OrderDTO(1, 1, 1, List.of(), "2023-01-01", "2023-01-05", "Pending"));

        when(systemService.searchOrders(keyword, token))
                .thenReturn(new Response<>(orders, "Orders retrieved successfully", true, null, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orders, response.getBody().getData());
        verify(systemService, times(1)).searchOrders(keyword, token);
    }

    @Test
    void testSearchOrders_Failure() {
        String keyword = "test";
        String token = "valid-token";

        when(systemService.searchOrders(keyword, token))
                .thenReturn(new Response<>(null, "No orders found", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).searchOrders(keyword, token);
    }

    @Test
    void testSearchOrders_Exception() {
        String keyword = "test";
        String token = "valid-token";

        when(systemService.searchOrders(keyword, token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, times(1)).searchOrders(keyword, token);
    }

    @Test
    void testGetOrdersByStoreId_Success() {
        int storeId = 1;
        String token = "valid-token";
        List<OrderDTO> orders = List.of(new OrderDTO(1, 1, 1, List.of(), "2023-01-01", "2023-01-05", "Pending"));

        when(systemService.getOrdersByStoreId(storeId, token))
                .thenReturn(new Response<>(orders, "Orders retrieved successfully", true, null, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orders, response.getBody().getData());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, token);
    }

    @Test
    void testGetOrdersByStoreId_Failure() {
        int storeId = 1;
        String token = "valid-token";

        when(systemService.getOrdersByStoreId(storeId, token))
                .thenReturn(new Response<List<OrderDTO>>(null, "No orders found", false, ErrorType.INTERNAL_ERROR, null));
        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, token);
    }

    @Test
    void testGetOrdersByStoreId_Exception() {
        int storeId = 1;
        String token = "valid-token";

        when(systemService.getOrdersByStoreId(storeId, token)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, token);
    }

    @Test
    void testDeleteOrder_InvalidToken() {
        int orderId = 1;
        String token = "invalid-token";

        when(systemService.deleteOrder(orderId, token))
                .thenReturn(new Response<>(null, "Unauthorized", false, ErrorType.UNAUTHORIZED, null));

        ResponseEntity<Response<Boolean>> response = orderController.deleteOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).deleteOrder(orderId, token);
    }

    @Test
    void testGetOrdersByStoreId_EmptyList() {
        int storeId = 1;
        String token = "valid-token";

        when(systemService.getOrdersByStoreId(storeId, token))
                .thenReturn(new Response<>(List.of(), "No orders found", true, null, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.getOrdersByStoreId(storeId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isEmpty());
        verify(systemService, times(1)).getOrdersByStoreId(storeId, token);
    }

    @Test
    void testViewOrder_NullInput() {
        int orderId = 0;
        String token = null;

        when(systemService.viewOrder(orderId, token))
                .thenReturn(new Response<>(null, "Invalid input", false, ErrorType.INVALID_INPUT, null));

        ResponseEntity<Response<OrderDTO>> response = orderController.viewOrder(orderId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).viewOrder(orderId, token);
    }

    @Test
    void testSearchOrders_Unauthorized() {
        String keyword = "test";
        String token = "unauthorized-token";

        when(systemService.searchOrders(keyword, token))
                .thenReturn(new Response<>(null, "Unauthorized access", false, ErrorType.UNAUTHORIZED, null));

        ResponseEntity<Response<List<OrderDTO>>> response = orderController.searchOrders(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        verify(systemService, times(1)).searchOrders(keyword, token);
    }


}