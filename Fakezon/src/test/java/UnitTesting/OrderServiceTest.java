package UnitTesting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Services.OrderService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Order;
import DomainLayer.Model.Store;

public class OrderServiceTest {
    private OrderService orderService;
    private IOrderRepository orderRepository;
    private Basket mockBasket;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        orderRepository = mock(IOrderRepository.class);
        mockBasket = mock(Basket.class);
        mockStore = mock(Store.class);
        
        orderService = new OrderService(orderRepository);
    }

    @Test
    void givenValidOrderId_WhenDeleteOrder_ThenOrderIsDeleted() {
        orderService.deleteOrder(1);

        verify(orderRepository, times(1)).deleteOrder(1);
    }

    @Test
    void givenNonExistingOrder_WhenDeleteOrder_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Order not found")).when(orderRepository).deleteOrder(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.deleteOrder(1);
        });

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void givenValidOrderId_WhenViewOrder_ThenReturnsOrderDTO() {
        IOrder mockOrder = mock(IOrder.class);
        when(mockOrder.getId()).thenReturn(1);
        when(mockOrder.getState()).thenReturn(OrderState.PENDING);
        when(mockOrder.getAddress()).thenReturn("123 Main St");
        when(mockOrder.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(orderRepository.getOrder(1)).thenReturn(mockOrder);

        IOrder orderDTO = orderService.viewOrder(1);

        assertEquals(1, orderDTO.getId());
        assertEquals("PENDING", orderDTO.getState().toString());
        assertEquals("123 Main St", orderDTO.getAddress());
        assertEquals("CREDIT_CARD", orderDTO.getPaymentMethod().toString());
    }

    @Test
    void givenNonExistingOrder_WhenViewOrder_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Order not found")).when(orderRepository).getOrder(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.viewOrder(1);
        });

        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void givenKeyword_WhenSearchOrders_ThenReturnsMatchingOrderIds() {
        IOrder mockOrder1 = mock(IOrder.class);
        IOrder mockOrder2 = mock(IOrder.class);

        when(mockOrder1.getId()).thenReturn(1);
        when(mockOrder1.getAddress()).thenReturn("123 Main St");
        when(mockOrder1.getState()).thenReturn(OrderState.PENDING);
        when(mockOrder1.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(mockOrder1.getProductIds()).thenReturn(Arrays.asList(1, 2, 3));

        when(mockOrder2.getId()).thenReturn(2);
        when(mockOrder2.getAddress()).thenReturn("456 Elm St");
        when(mockOrder2.getState()).thenReturn(OrderState.SHIPPED);
        when(mockOrder2.getPaymentMethod()).thenReturn(PaymentMethod.CASH_ON_DELIVERY);
        when(mockOrder2.getProductIds()).thenReturn(Arrays.asList(4, 5, 6));

        when(orderRepository.getAllOrders()).thenReturn(Arrays.asList(mockOrder1, mockOrder2));

        List<IOrder> orders = orderService.searchOrders("123");
        List<Integer> orderIds = orders.stream().map(order -> order.getId()).toList();

        assertEquals(1, orderIds.size());
        assertTrue(orderIds.contains(1));
    }

    @Test
    void givenStoreId_WhenGetOrdersByStoreId_ThenReturnsMatchingOrders() {
        // Mock orders
        IOrder mockOrder1 = mock(IOrder.class);
        IOrder mockOrder2 = mock(IOrder.class);

        when(mockOrder1.getId()).thenReturn(1);
        when(mockOrder1.getUserId()).thenReturn(101);
        when(mockOrder1.getStoreId()).thenReturn(10);
        when(mockOrder1.getProductIds()).thenReturn(Arrays.asList(1, 2));
        when(mockOrder1.getState()).thenReturn(OrderState.PENDING);
        when(mockOrder1.getAddress()).thenReturn("123 Main St");
        when(mockOrder1.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);

        when(mockOrder2.getId()).thenReturn(2);
        when(mockOrder2.getUserId()).thenReturn(102);
        when(mockOrder2.getStoreId()).thenReturn(20);
        when(mockOrder2.getProductIds()).thenReturn(Collections.singletonList(3));
        when(mockOrder2.getState()).thenReturn(OrderState.SHIPPED);
        when(mockOrder2.getAddress()).thenReturn("456 Elm St");
        when(mockOrder2.getPaymentMethod()).thenReturn(PaymentMethod.CASH_ON_DELIVERY);

        when(orderRepository.getAllOrders()).thenReturn(Arrays.asList(mockOrder1, mockOrder2));

        // Call the method
        List<IOrder> result = orderService.getOrdersByStoreId(10);

        // Verify the result
        assertEquals(1, result.size());
        IOrder orderDTO = result.get(0);
        assertEquals(1, orderDTO.getId());
        assertEquals(101, orderDTO.getUserId());
        assertEquals(10, orderDTO.getStoreId());
        assertEquals(2, orderDTO.getProductIds().size());
        assertEquals("PENDING", orderDTO.getState().toString());
        assertEquals("123 Main St", orderDTO.getAddress());
        assertEquals("CREDIT_CARD", orderDTO.getPaymentMethod().toString());
    }

    @Test
    void testgetOrderOrderId_Success() {
        IOrder mockOrder = mock(IOrder.class);
        when(orderRepository.getOrder(1)).thenReturn(mockOrder);
        when(mockOrder.getUserId()).thenReturn(42);

        int userId = orderService.getOrderOrderId(1);
        assertEquals(42, userId);
        verify(orderRepository, times(1)).getOrder(1);
    }

    @Test
    void testgetOrderOrderId_OrderNotFound() {
        when(orderRepository.getOrder(1)).thenThrow(new IllegalArgumentException("Order not found"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderOrderId(1));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetOrderStoreId_Success() {
        IOrder mockOrder = mock(IOrder.class);
        when(orderRepository.getOrder(1)).thenReturn(mockOrder);
        when(mockOrder.getStoreId()).thenReturn(99);

        int storeId = orderService.getOrderStoreId(1);
        assertEquals(99, storeId);
        verify(orderRepository, times(1)).getOrder(1);
    }

    @Test
    void testGetOrderStoreId_OrderNotFound() {
        when(orderRepository.getOrder(1)).thenThrow(new IllegalArgumentException("Order not found"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderStoreId(1));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void testGetOrderProductIds_Success() {
        IOrder mockOrder = mock(IOrder.class);
        List<Integer> productIds = Arrays.asList(1, 2, 3);
        when(orderRepository.getOrder(1)).thenReturn(mockOrder);
        when(mockOrder.getProductIds()).thenReturn(productIds);

        List<Integer> result = orderService.getOrderProductIds(1);
        assertEquals(productIds, result);
        verify(orderRepository, times(1)).getOrder(1);
    }

    @Test
    void testGetOrderProductIds_OrderNotFound() {
        when(orderRepository.getOrder(1)).thenThrow(new IllegalArgumentException("Order not found"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderProductIds(1));
        assertEquals("Order not found", ex.getMessage());
    }

@Test
void testAddOrderCart_Exception() {
    IOrder mockOrder = mock(IOrder.class);
    doThrow(new RuntimeException("Add failed")).when(orderRepository).addOrder(mockOrder);

    Exception ex = assertThrows(RuntimeException.class, () -> orderRepository.addOrder(mockOrder));
    assertEquals("Add failed", ex.getMessage());
}


    @Test
    void testClearAllData() {
        orderService.clearAllData();
        verify(orderRepository, times(1)).clearAllData();
    }
    @Test
    void testAddOrderCart_Success() {
        // Arrange
        StoreDTO storeDTO = mock(StoreDTO.class);
        when(storeDTO.getStoreId()).thenReturn(1);
        StoreProductDTO productDTO = mock(StoreProductDTO.class);
        when(productDTO.getQuantity()).thenReturn(2);
    
        Map<StoreProductDTO, Boolean> products = new java.util.HashMap<>();
        products.put(productDTO, true);
    
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart = new java.util.HashMap<>();
        cart.put(storeDTO, products);
    
        Map<Integer, Double> prices = new java.util.HashMap<>();
        prices.put(1, 99.99);
    
        int userId = 42;
        String address = "123 Main St";
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
    
        // Act
        orderService.addOrderCart(cart, prices, userId, address, paymentMethod);
    
        // Assert
        verify(orderRepository, times(1)).addOrder(any(Order.class));
    }
    
    @Test
    void testAddOrderCart_MultipleStoresAndProducts() {
        // Arrange
        StoreDTO store1 = mock(StoreDTO.class);
        StoreDTO store2 = mock(StoreDTO.class);
        when(store1.getStoreId()).thenReturn(1);
        when(store2.getStoreId()).thenReturn(2);
    
        StoreProductDTO product1 = mock(StoreProductDTO.class);
        StoreProductDTO product2 = mock(StoreProductDTO.class);
        StoreProductDTO product3 = mock(StoreProductDTO.class);
        when(product1.getQuantity()).thenReturn(1);
        when(product2.getQuantity()).thenReturn(2);
        when(product3.getQuantity()).thenReturn(3);
    
        Map<StoreProductDTO, Boolean> products1 = new java.util.HashMap<>();
        products1.put(product1, true);
        products1.put(product2, false);
    
        Map<StoreProductDTO, Boolean> products2 = new java.util.HashMap<>();
        products2.put(product3, true);
    
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart = new java.util.HashMap<>();
        cart.put(store1, products1);
        cart.put(store2, products2);
    
        Map<Integer, Double> prices = new java.util.HashMap<>();
        prices.put(1, 10.0);
        prices.put(2, 20.0);
    
        int userId = 99;
        String address = "456 Elm St";
        PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
    
        // Act
        orderService.addOrderCart(cart, prices, userId, address, paymentMethod);
    
        // Assert: Should call addOrder twice (once per store)
        verify(orderRepository, times(2)).addOrder(any(Order.class));
    }
    
    @Test
    void testAddOrderCart_ThrowsIllegalArgumentException() {
        // Arrange
        StoreDTO storeDTO = mock(StoreDTO.class);
        when(storeDTO.getStoreId()).thenReturn(1);
        StoreProductDTO productDTO = mock(StoreProductDTO.class);
        when(productDTO.getQuantity()).thenReturn(1);
    
        Map<StoreProductDTO, Boolean> products = new java.util.HashMap<>();
        products.put(productDTO, true);
    
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart = new java.util.HashMap<>();
        cart.put(storeDTO, products);
    
        Map<Integer, Double> prices = new java.util.HashMap<>();
        prices.put(1, 50.0);
    
        int userId = 7;
        String address = "789 Oak St";
        PaymentMethod paymentMethod = PaymentMethod.PAYPAL;
    
        doThrow(new IllegalArgumentException("Order error")).when(orderRepository).addOrder(any(Order.class));
    
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            orderService.addOrderCart(cart, prices, userId, address, paymentMethod)
        );
        assertEquals("Order error", ex.getMessage());
        verify(orderRepository, times(1)).addOrder(any(Order.class));
    }
    
    @Test
    void testAddOrderCart_EmptyCart_NoOrdersAdded() {
        // Arrange
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart = new java.util.HashMap<>();
        Map<Integer, Double> prices = new java.util.HashMap<>();
        int userId = 1;
        String address = "Empty";
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
    
        // Act
        orderService.addOrderCart(cart, prices, userId, address, paymentMethod);
    
        // Assert: No orders should be added
        verify(orderRepository, never()).addOrder(any(Order.class));
    }
    
        @Test
    void testGetOrdersByUserId_ReturnsMatchingOrders() {
        IOrder mockOrder1 = mock(IOrder.class);
        IOrder mockOrder2 = mock(IOrder.class);
        when(mockOrder1.getId()).thenReturn(1);
        when(mockOrder1.getUserId()).thenReturn(42);
        when(mockOrder2.getId()).thenReturn(2);
        when(mockOrder2.getUserId()).thenReturn(42);

        List<IOrder> mockOrders = Arrays.asList(mockOrder1, mockOrder2);
        when(orderRepository.getOrdersByUserId(42)).thenReturn(mockOrders);
        List<IOrder> result = orderService.getOrdersByUserId(42);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(orderRepository, times(1)).getOrdersByUserId(42);
    }

    @Test
    void testGetOrdersByUserId_NoOrdersFound() {
        when(orderRepository.getOrdersByUserId(99)).thenReturn(Collections.emptyList());
        List<IOrder> result = orderService.getOrdersByUserId(99);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).getOrdersByUserId(99);
    }
}
