package UnitTesting;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Model.Basket;
import DomainLayer.Model.User;

public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testAddToBasket() {
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS);

        int quantity = 2;
        user.addToBasket(storeId, product.getProductId(), quantity);

        Basket basket = user.getBasket(storeId);
        assertNotNull(basket, "Basket should not be null after adding a product");
        assertEquals(1, basket.getProducts().size(), "Basket should contain one product");
    }

    @Test
    void testViewCart() {
        int storeId = 1;
        int storeId2 = 2;
        StoreProductDTO product1 = new StoreProductDTO(101, "Product 1", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS);
        StoreProductDTO product2 = new StoreProductDTO(102, "Product 2", 20.0, 1, 5.0, 2, PCategory.BEAUTY);

        user.addToBasket(storeId, product1.getProductId(), product1.getQuantity());
        user.addToBasket(storeId2, product2.getProductId(), product2.getQuantity());

        Map<Integer, Map<Integer, Integer>> cartProducts = user.viewCart();

        assertNotNull(cartProducts, "Cart products should not be null");
        assertEquals(2, cartProducts.size(), "Cart should contain two products");
    }

    @Test
    void testClearCart() {
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS);
        user.addToBasket(storeId, product.getProductId(), product.getQuantity());

        user.clearCart();

        Map<Integer, Map<Integer, Integer>> cartProducts = user.viewCart();
        assertTrue(cartProducts.isEmpty(), "Cart should be empty after clearing");
    }

    @Test
    void testLogout() {
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS);
        user.addToBasket(storeId, product.getProductId(), product.getQuantity());

        boolean result = user.logout();

        assertTrue(result, "Logout should return true");
        Map<Integer, Map<Integer, Integer>> cartProducts = user.viewCart();
        assertTrue(cartProducts.isEmpty(), "Cart should be empty after logout");
    }

    @Test
    void testIsLoggedIn_DefaultFalse() {
        assertFalse(user.isLoggedIn(), "User should not be logged in by default");
    }

    @Test
    void testIsRegistered_DefaultFalse() {
        assertFalse(user.isRegistered(), "User should not be registered by default");
    }

    @Test
    void testGetUserId() {
        int id = user.getUserId();
        assertTrue(id > 0, "User ID should be positive");
    }

    @Test
    void testToDTO() {
        UserDTO dto = user.toDTO();
        assertEquals(user.getUserId(), dto.getUserId());
    }

    @Test
    void testSetUserId() {
        user.setUserId(42);
        assertEquals(42, user.getUserId());
    }

    @Test
    void testSetCart() {
        int storeId = 1;
        int productId = 101;
        int quantity = 3;
        Map<Integer, Map<Integer, Integer>> validCart = Map.of(storeId, Map.of(productId, quantity));
        user.setCart(validCart);

        Map<Integer, Map<Integer, Integer>> cartProducts = user.viewCart();
        assertEquals(1, cartProducts.size());
        assertEquals(quantity, cartProducts.get(storeId).get(productId));
    }

    @Test
    void testSaveCartOrder() {
        int storeId = 1;
        int productId = 101;
        int quantity = 2;
        user.addToBasket(storeId, productId, quantity);

        user.saveCartOrderAndDeleteIt();
        Map<Integer, Map<Integer, Integer>> cartProducts = user.viewCart();
        assertTrue(cartProducts.isEmpty(), "Cart should be empty after saving order");
    }
}