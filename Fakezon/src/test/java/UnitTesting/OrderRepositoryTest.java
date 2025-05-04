package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Model.Basket;
import DomainLayer.Model.StoreProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import DomainLayer.Interfaces.IOrder;
import InfrastructureLayer.Repositories.OrderRepository;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Collection;


public class OrderRepositoryTest {
    private OrderRepository repository;
    private IOrder order1;
    private IOrder order2;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository(new HashMap<>());

        // Create mock StoreProductDTO objects with all required fields
        StoreProductDTO product1 = new StoreProductDTO(1, "Product1", 10.0, 5, 4.5, 1);
        StoreProductDTO product2 = new StoreProductDTO(2, "Product2", 15.0, 3, 4.0, 1);
        StoreProductDTO product3 = new StoreProductDTO(3, "Product3", 20.0, 2, 3.5, 1);

        List<StoreProductDTO> products1 = Arrays.asList(product1, product2, product3);
        List<StoreProductDTO> products2 = Arrays.asList(product1, product2);

        Basket basket1 = new Basket(1, products1);
        Basket basket2 = new Basket(2, products2);

        order1 = new Order(1, 101, OrderState.PENDING, basket1, "123 Main St", PaymentMethod.CREDIT_CARD);
        order2 = new Order(2, 102, OrderState.SHIPPED, basket2, "456 Elm St", PaymentMethod.CASH_ON_DELIVERY);
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
        Basket updatedBasket = new Basket(1, Arrays.asList(
                new StoreProductDTO(7, "Product7", 30.0, 0, 0.0, 1),
                new StoreProductDTO(8, "Product8", 40.0, 0, 0.0, 1),
                new StoreProductDTO(9, "Product9", 50.0, 0, 0.0, 1)
        ));
        IOrder updatedOrder = new Order(1, 101, OrderState.SHIPPED, updatedBasket, "789 Pine St", PaymentMethod.CREDIT_CARD);
        repository.updateOrder(1, updatedOrder);        repository.updateOrder(1, updatedOrder);
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