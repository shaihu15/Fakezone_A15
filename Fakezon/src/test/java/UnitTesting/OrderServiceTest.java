package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Services.OrderService;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IProduct;
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
    void givenValidOrderDetails_WhenAddOrder_ThenOrderIsAdded() {
        StoreProductDTO mockProduct = mock(StoreProductDTO.class);
        when(mockProduct.getProductId()).thenReturn(1);

        when(mockBasket.getProducts()).thenReturn(Collections.singletonList(mockProduct));
        when(mockBasket.getStoreID()).thenReturn(101);

        int orderId = orderService.addOrder(mockBasket, 1, "123 Main St", PaymentMethod.CREDIT_CARD);

        verify(orderRepository, times(1)).addOrder(any(IOrder.class));
        assertEquals(1, orderId); // Assuming the ID is generated as 1
    }

    @Test
    void givenValidOrderDetails_WhenUpdateOrder_ThenOrderIsUpdated() {
        StoreProductDTO mockProductDTO = mock(StoreProductDTO.class);
        when(mockProductDTO.getProductId()).thenReturn(1);
        when(mockBasket.getProducts()).thenReturn(Collections.singletonList(mockProductDTO));

        int orderId = orderService.updateOrder(1, mockBasket, 1, "123 Main St", PaymentMethod.CREDIT_CARD);

        verify(orderRepository, times(1)).updateOrder(eq(1), any(IOrder.class));
        assertEquals(1, orderId);
    }

    @Test
    void givenNonExistingOrder_WhenUpdateOrder_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Order not found")).when(orderRepository).updateOrder(eq(1), any(IOrder.class));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrder(1, mockBasket, 1, "123 Main St", PaymentMethod.CREDIT_CARD);
        });

        assertEquals("Order not found", exception.getMessage());
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

        List<Integer> orderIds = orderService.searchOrders("123");

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
}
