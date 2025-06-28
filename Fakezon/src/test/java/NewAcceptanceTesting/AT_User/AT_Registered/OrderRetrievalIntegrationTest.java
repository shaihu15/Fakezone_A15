package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.OrderedProductDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;

@ExtendWith(MockitoExtension.class)
public class OrderRetrievalIntegrationTest {

    @Mock
    private SystemService systemService;

    private int userId = 1;
    private int storeId = 101;
    private int productId1 = 1001;
    private int productId2 = 1002;

    @Test
    void testGetOrdersByUser_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(systemService.getOrdersByUserId(userId)).thenReturn(new Response<List<OrderDTO>>(new ArrayList<>(), "No orders found.", true, null, null));

        // Act
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGetOrdersByUser_WithSingleOrder() {
        // Arrange
        Set<OrderedProductDTO> products = new HashSet<>();
        products.add(new OrderedProductDTO(productId1, "Test Product 1", 10.0, 1));
        
        OrderDTO order = new OrderDTO(1, userId, storeId, products, OrderState.SHIPPED.toString(), "Test Address", PaymentMethod.CREDIT_CARD.toString(), 10.0);
        
        List<OrderDTO> orders = new ArrayList<>();
        orders.add(order);

        when(systemService.getOrdersByUserId(userId)).thenReturn(new Response<List<OrderDTO>>(orders, "", true, null, null));

        // Act
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        OrderDTO retrievedOrder = response.getData().get(0);
        assertEquals(userId, retrievedOrder.getUserId());
        assertEquals(1, retrievedOrder.getProducts().size());
        assertEquals(productId1, retrievedOrder.getProducts().iterator().next().getProductId());
    }

    @Test
    void testGetOrdersByUser_WithMultipleOrders() {
        // Arrange
        Set<OrderedProductDTO> products1 = new HashSet<>();
        products1.add(new OrderedProductDTO(productId1, "Test Product 1", 10.0, 1));
        OrderDTO order1 = new OrderDTO(1, userId, storeId, products1, OrderState.SHIPPED.toString(), "Test Address", PaymentMethod.CREDIT_CARD.toString(), 10.0);

        Set<OrderedProductDTO> products2 = new HashSet<>();
        products2.add(new OrderedProductDTO(productId2, "Test Product 2", 20.0, 2));
        OrderDTO order2 = new OrderDTO(2, userId, storeId, products2, OrderState.SHIPPED.toString(), "Test Address", PaymentMethod.CREDIT_CARD.toString(), 40.0);
        
        List<OrderDTO> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);

        when(systemService.getOrdersByUserId(userId)).thenReturn(new Response<List<OrderDTO>>(orders, "", true, null, null));

        // Act
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
    }

    @Test
    void testGetOrdersByUser_InvalidUserId() {
        // Arrange
        int invalidUserId = -1;
        when(systemService.getOrdersByUserId(invalidUserId)).thenReturn(new Response<List<OrderDTO>>(null, "Invalid user ID.", false, ErrorType.INTERNAL_ERROR, null));

        // Act
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(invalidUserId);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetOrdersByUser_NotLoggedIn() {
        // Arrange
        when(systemService.getOrdersByUserId(userId)).thenReturn(new Response<List<OrderDTO>>(null, "User not logged in.", false, ErrorType.INVALID_INPUT, null));

        // Act
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
}