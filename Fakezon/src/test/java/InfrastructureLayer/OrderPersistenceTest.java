package InfrastructureLayer;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Model.Order;
import DomainLayer.Model.OrderedProduct;
import InfrastructureLayer.Repositories.OrderJpaRepository;
import InfrastructureLayer.Repositories.OrderRepositoryImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {com.fakezone.fakezone.FakezoneApplication.class})
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class OrderPersistenceTest {

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    private Order testOrder1;
    private Order testOrder2;

    @BeforeEach
    void setUp() {
        // Clear data before each test
        orderRepository.clearAllData();

        // Create test ordered products
        List<OrderedProduct> products1 = Arrays.asList(
                new OrderedProduct(1, "Product 1", 10.0, 2),
                new OrderedProduct(2, "Product 2", 15.0, 1)
        );

        List<OrderedProduct> products2 = Arrays.asList(
                new OrderedProduct(3, "Product 3", 20.0, 1),
                new OrderedProduct(4, "Product 4", 25.0, 2)
        );

        // Create test orders (let JPA assign IDs)
        testOrder1 = new Order(101, 201, OrderState.PENDING, products1, 
                              "123 Main St", PaymentMethod.CREDIT_CARD, 35.0, 1001, 2001);

        testOrder2 = new Order(102, 202, OrderState.SHIPPED, products2, 
                              "456 Oak Ave", PaymentMethod.PAYPAL, 70.0, 1002, 2002);
    }

    @Test
    void testAddOrder() {
        // Test adding a new order
        orderRepository.addOrder(testOrder1);

        // Verify order was saved
        IOrder savedOrder = orderRepository.getOrder(testOrder1.getId());
        assertNotNull(savedOrder);
        assertEquals(testOrder1.getId(), savedOrder.getId());
        assertEquals(testOrder1.getUserId(), savedOrder.getUserId());
        assertEquals(testOrder1.getStoreId(), savedOrder.getStoreId());
        assertEquals(testOrder1.getTotalPrice(), savedOrder.getTotalPrice());
        assertEquals(testOrder1.getState(), savedOrder.getState());
        assertEquals(testOrder1.getAddress(), savedOrder.getAddress());
        assertEquals(testOrder1.getPaymentMethod(), savedOrder.getPaymentMethod());
    }

    @Test
    void testAddDuplicateOrder() {
        // Add order first time
        orderRepository.addOrder(testOrder1);

        // Try to add same order again
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            orderRepository.addOrder(testOrder1);
        });
    }

    @Test
    void testGetOrder() {
        orderRepository.addOrder(testOrder1);

        IOrder retrievedOrder = orderRepository.getOrder(testOrder1.getId());
        assertNotNull(retrievedOrder);
        assertEquals(testOrder1.getId(), retrievedOrder.getId());
        assertEquals(testOrder1.getUserId(), retrievedOrder.getUserId());
    }

    @Test
    void testGetNonExistentOrder() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            orderRepository.getOrder(999);
        });
    }

    @Test
    void testDeleteOrder() {
        orderRepository.addOrder(testOrder1);
        
        // Verify order exists
        assertDoesNotThrow(() -> orderRepository.getOrder(testOrder1.getId()));
        
        // Delete order
        orderRepository.deleteOrder(testOrder1.getId());
        
        // Verify order is deleted
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            orderRepository.getOrder(testOrder1.getId());
        });
    }

    @Test
    void testDeleteNonExistentOrder() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            orderRepository.deleteOrder(999);
        });
    }

    @Test
    void testGetAllOrders() {
        orderRepository.addOrder(testOrder1);
        orderRepository.addOrder(testOrder2);

        Collection<IOrder> allOrders = orderRepository.getAllOrders();
        assertEquals(2, allOrders.size());

        // Verify orders are in collection
        assertTrue(allOrders.stream().anyMatch(o -> o.getId() == testOrder1.getId()));
        assertTrue(allOrders.stream().anyMatch(o -> o.getId() == testOrder2.getId()));
    }

    @Test
    void testGetOrdersByUserId() {
        orderRepository.addOrder(testOrder1);
        orderRepository.addOrder(testOrder2);

        Collection<IOrder> userOrders = orderRepository.getOrdersByUserId(101);
        assertEquals(1, userOrders.size());
        assertEquals(testOrder1.getId(), userOrders.iterator().next().getId());
    }

    @Test
    void testOrderWithOrderedProducts() {
        orderRepository.addOrder(testOrder1);

        IOrder savedOrder = orderRepository.getOrder(testOrder1.getId());
        List<OrderedProduct> products = savedOrder.getProducts();
        
        assertNotNull(products);
        assertEquals(2, products.size());
        
        // Verify ordered products data
        OrderedProduct product1 = products.get(0);
        assertEquals(1, product1.getProductId());
        assertEquals("Product 1", product1.getName());
        assertEquals(10.0, product1.getPrice());
        assertEquals(2, product1.getQuantity());
    }

    @Test
    void testClearAllData() {
        orderRepository.addOrder(testOrder1);
        orderRepository.addOrder(testOrder2);

        assertEquals(2, orderRepository.getAllOrders().size());

        orderRepository.clearAllData();
        assertEquals(0, orderRepository.getAllOrders().size());
    }

    @Test
    void testOrderStateAndPaymentMethod() {
        orderRepository.addOrder(testOrder1);

        IOrder savedOrder = orderRepository.getOrder(testOrder1.getId());
        assertEquals(OrderState.PENDING, savedOrder.getState());
        assertEquals(PaymentMethod.CREDIT_CARD, savedOrder.getPaymentMethod());

        // Test state update
        savedOrder.setState(OrderState.SHIPPED);
        if (orderRepository instanceof OrderRepositoryImpl) {
            ((OrderRepositoryImpl) orderRepository).updateOrder(savedOrder);
        }

        IOrder updatedOrder = orderRepository.getOrder(testOrder1.getId());
        assertEquals(OrderState.SHIPPED, updatedOrder.getState());
    }
} 