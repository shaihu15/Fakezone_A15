package UnitTesting;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.OrderedProductDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderDTOTest {

    private OrderDTO orderDTO;
    private Collection<OrderedProductDTO> products;

    @BeforeEach
    void setUp() {
        // Initialize test data
        products = new ArrayList<>();
        Set<Integer> storeId1 = new HashSet<>();
        storeId1.add(1);
        Set<Integer> storeId2 = new HashSet<>();
        storeId2.add(2);
        products.add(new OrderedProductDTO(1, "Product 1", 10, 10));
        products.add(new OrderedProductDTO(2, "Product 2", 20, 20));  

        orderDTO = new OrderDTO(1001, 2001, 3001, products, "Pending", "123 Test Street", "Credit Card", 0);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(orderDTO, "OrderDTO object should be created");
        assertEquals(1001, orderDTO.getOrderId(), "Order ID should match");
        assertEquals(2001, orderDTO.getUserId(), "User ID should match");
        assertEquals(3001, orderDTO.getStoreId(), "Store ID should match");
        assertEquals(2, orderDTO.getProducts().size(), "Order should contain 2 products");
        assertEquals("Pending", orderDTO.getOrderState(), "Order state should match");
        assertEquals("123 Test Street", orderDTO.getAddress(), "Order address should match");
        assertEquals("Credit Card", orderDTO.getPaymentMethod(), "Payment method should match");
    }

    @Test
    void getOrderId_ShouldReturnCorrectId() {
        assertEquals(1001, orderDTO.getOrderId(), "Order ID should match");
    }

    @Test
    void getUserId_ShouldReturnCorrectUserId() {
        assertEquals(2001, orderDTO.getUserId(), "User ID should match");
    }

    @Test
    void getStoreId_ShouldReturnCorrectStoreId() {
        assertEquals(3001, orderDTO.getStoreId(), "Store ID should match");
    }

    @Test
    void getProducts_ShouldReturnCorrectProducts() {
        Collection<OrderedProductDTO> retrievedProducts = orderDTO.getProducts();
        assertEquals(2, retrievedProducts.size(), "Products size should match");
    }

    @Test
    void getOrderState_ShouldReturnCorrectState() {
        assertEquals("Pending", orderDTO.getOrderState(), "Order state should match");
    }

    @Test
    void getAddress_ShouldReturnCorrectAddress() {
        assertEquals("123 Test Street", orderDTO.getAddress(), "Order address should match");
    }

    @Test
    void getPaymentMethod_ShouldReturnCorrectPaymentMethod() {
        assertEquals("Credit Card", orderDTO.getPaymentMethod(), "Payment method should match");
    }
}