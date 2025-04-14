package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import DomainLayer.Interfaces.IOrder;
import InfrastructureLayer.Repositories.OrderRepository;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Order;

import java.util.Arrays;
import java.util.Collection;

public class OrderRepositoryTest {
    private OrderRepository repository;
    private IOrder order1;
    private IOrder order2;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository(new HashMap<>());
        Collection<Integer> productIds1 = Arrays.asList(1, 2, 3);
        Collection<Integer> productIds2 = Arrays.asList(4, 5, 6);
        order1 = new Order(1, 101, OrderState.PENDING, productIds1, 201, "123 Main St", PaymentMethod.CREDIT_CARD);
        order2 = new Order(2, 102, OrderState.SHIPPED, productIds2, 202, "456 Elm St", PaymentMethod.CASH_ON_DELIVERY);
    }

    @Test
    void givenValidOrder_WhenAddOrder_ThenOrderIsAdded() {
        repository.addOrder(order1);
        assertEquals(order1, repository.getOrder(1));
    }

    @Test
    void givenDuplicateOrder_WhenAddOrder_ThenThrowsException() {
        repository.addOrder(order1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addOrder(order1);
        });
        assertEquals("Order with ID 1 already exists.", exception.getMessage());
    }

    @Test
    void givenExistingOrder_WhenUpdateOrder_ThenOrderIsUpdated() {
        repository.addOrder(order1);
        IOrder updatedOrder = new Order(1, 101, OrderState.SHIPPED, Arrays.asList(7, 8, 9), 201, "789 Pine St", PaymentMethod.CREDIT_CARD);
        repository.updateOrder(1, updatedOrder);
        assertEquals(updatedOrder, repository.getOrder(1));
    }

    @Test
    void givenNonExistingOrder_WhenUpdateOrder_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateOrder(1, order1);
        });
        assertEquals("Order with ID 1 does not exist.", exception.getMessage());
    }

    @Test
    void givenExistingOrder_WhenDeleteOrder_ThenOrderIsDeleted() {
        repository.addOrder(order1);
        repository.deleteOrder(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getOrder(1);
        });
        assertEquals("Order with ID 1 does not exist.", exception.getMessage());
    }

    @Test
    void givenNonExistingOrder_WhenDeleteOrder_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteOrder(1);
        });
        assertEquals("Order with ID 1 does not exist.", exception.getMessage());
    }

    @Test
    void givenExistingOrder_WhenGetOrder_ThenReturnsOrder() {
        repository.addOrder(order1);
        IOrder retrievedOrder = repository.getOrder(1);
        assertEquals(order1, retrievedOrder);
    }

    @Test
    void givenNonExistingOrder_WhenGetOrder_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getOrder(1);
        });
        assertEquals("Order with ID 1 does not exist.", exception.getMessage());
    }
}