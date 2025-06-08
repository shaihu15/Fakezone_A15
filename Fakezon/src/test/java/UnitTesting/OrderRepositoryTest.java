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
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Order;
import DomainLayer.Model.OrderedProduct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collection;


public class OrderRepositoryTest {
    private OrderRepository repository;
    private IOrder order1;
    private IOrder order2;
    private int storeId;
    private int user1Id = 101; // Example user ID for testing
    private int user2Id = 102; // Example user ID for testing

    @BeforeEach
    void setUp() {
        repository = new OrderRepository(new HashMap<>());
        storeId = 1;
        // Create mock StoreProductDTO objects with all required fields
        StoreProductDTO product1 = new StoreProductDTO(1, "Product1", 10.0, 5, 4.5, storeId,PCategory.ELECTRONICS);
        StoreProductDTO product2 = new StoreProductDTO(2, "Product2", 15.0, 3, 4.0, storeId, PCategory.ELECTRONICS);
        StoreProductDTO product3 = new StoreProductDTO(3, "Product3", 20.0, 2, 3.5, storeId, PCategory.ELECTRONICS);

        List<StoreProductDTO> products1 = Arrays.asList(product1, product2, product3);
        List<StoreProductDTO> products2 = Arrays.asList(product1, product2);

        Basket basket1 = new Basket(1, products1.stream().collect(Collectors.toMap(StoreProductDTO::getProductId, StoreProductDTO::getQuantity)));
        Basket basket2 = new Basket(2, products2.stream().collect(Collectors.toMap(StoreProductDTO::getProductId, StoreProductDTO::getQuantity)));
        List<OrderedProduct> orderedProducts1 = products1.stream().map(product -> new OrderedProduct(product, product.getQuantity())).collect(Collectors.toList());
        List<OrderedProduct> orderedProducts2 = products2.stream().map(product -> new OrderedProduct(product, product.getQuantity())).collect(Collectors.toList());
        double totalPrice1 = products1.stream().mapToDouble(product -> product.getBasePrice() * product.getQuantity()).sum();
        double totalPrice2 = products2.stream().mapToDouble(product -> product.getBasePrice() * product.getQuantity()).sum();
        order1 = new Order(1, user1Id, storeId, OrderState.PENDING, orderedProducts1, "123 Main St", PaymentMethod.CREDIT_CARD, totalPrice1, 111, 222);
        order2 = new Order(2, user2Id, storeId, OrderState.SHIPPED, orderedProducts2, "456 Elm St", PaymentMethod.CASH_ON_DELIVERY, totalPrice2, 333, 444);
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

    @Test
    void givenNonExistingOrders_WhenGetOrderByUser_ThenReturnsOrders() {
        repository.addOrder(order1);
        repository.addOrder(order2);
        Collection<IOrder> userOrders = repository.getOrdersByUserId(user1Id);
        assertEquals(1, userOrders.size());
        assertTrue(userOrders.contains(order1));
    }

    @Test
    void givenNoOrders_WhenGetOrderByUser_ThenReturnsEmptyList() {
        Collection<IOrder> userOrders = repository.getOrdersByUserId(user1Id);
        assertTrue(userOrders.isEmpty());
    }

}