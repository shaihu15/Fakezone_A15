package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import ApplicationLayer.Services.OrderService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Basket;
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
    void testGetOrderUserId_Success() {
        IOrder mockOrder = mock(IOrder.class);
        when(orderRepository.getOrder(1)).thenReturn(mockOrder);
        when(mockOrder.getUserId()).thenReturn(42);

        int userId = orderService.getOrderUserId(1);
        assertEquals(42, userId);
        verify(orderRepository, times(1)).getOrder(1);
    }

    @Test
    void testGetOrderUserId_OrderNotFound() {
        when(orderRepository.getOrder(1)).thenThrow(new IllegalArgumentException("Order not found"));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> orderService.getOrderUserId(1));
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
}
