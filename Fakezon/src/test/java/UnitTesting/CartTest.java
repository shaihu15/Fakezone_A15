package UnitTesting;

import DomainLayer.Model.Cart;
import DomainLayer.Model.Basket;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class CartTest {

    @Test
    void testConstructorAndGetBaskets() {
        Cart cart = new Cart();
        assertNotNull(cart.getBaskets());
        assertTrue(cart.getBaskets().isEmpty());
    }

    @Test
    void testAddProduct_NewBasketAndExistingBasket() {
        Cart cart = new Cart();

        // Add product to a new store (creates new Basket)
        cart.addProduct(1, 100, 2);
        assertTrue(cart.getBaskets().containsKey(1));
        assertEquals(2, cart.getBaskets().get(1).getProducts().get(100));

        // Add another product to the same store (existing Basket)
        cart.addProduct(1, 200, 3);
        assertEquals(3, cart.getBaskets().get(1).getProducts().get(200));

        // Add product to a different store
        cart.addProduct(2, 300, 1);
        assertTrue(cart.getBaskets().containsKey(2));
        assertEquals(1, cart.getBaskets().get(2).getProducts().get(300));
    }

    @Test
    void testGetBasket_ValidAndInvalid() {
        Cart cart = new Cart();
        cart.addProduct(1, 100, 2);

        // Valid basket
        Basket basket = cart.getBasket(1);
        assertNotNull(basket);
        assertEquals(2, basket.getProducts().get(100));

        // Invalid basket (should throw)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> cart.getBasket(99));
        assertEquals("No basket found for store ID: 99", exception.getMessage());
    }

    @Test
    void testGetAllProducts() {
        Cart cart = new Cart();
        cart.addProduct(1, 100, 2);
        cart.addProduct(1, 200, 3);
        cart.addProduct(2, 300, 1);

        Map<Integer, Map<Integer, Integer>> allProducts = cart.getAllProducts();
        assertEquals(2, allProducts.size());
        assertEquals(2, allProducts.get(1).size());
        assertEquals(1, allProducts.get(2).size());
        assertEquals(2, allProducts.get(1).get(100));
        assertEquals(3, allProducts.get(1).get(200));
        assertEquals(1, allProducts.get(2).get(300));
    }

    @Test
    void testClear() {
        Cart cart = new Cart();
        cart.addProduct(1, 100, 2);
        cart.addProduct(2, 200, 3);
        assertFalse(cart.getBaskets().isEmpty());

        cart.clear();
        assertTrue(cart.getBaskets().isEmpty());
    }
}
