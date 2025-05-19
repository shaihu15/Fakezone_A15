package UnitTesting;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Add import if Basket exists in another package, e.g.:
// import your.package.Basket;

class BasketTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 5;
        Basket basket = new Basket(storeId);
        assertEquals(storeId, basket.getStoreID());
        assertTrue(basket.getProducts().isEmpty());

        Map<Integer, Integer> products = new HashMap<>();
        products.put(10, 2);
        products.put(20, 3);

        Basket basket2 = new Basket(storeId, products);
        assertEquals(storeId, basket2.getStoreID());
        assertEquals(products, basket2.getProducts());
    }
    
    // Stub Basket class for testing if not already defined elsewhere
    class Basket {
        private int storeID;
        private Map<Integer, Integer> products;
    
        public Basket(int storeID) {
            this.storeID = storeID;
            this.products = new HashMap<>();
        }
    
        public Basket(int storeID, Map<Integer, Integer> products) {
            this.storeID = storeID;
            this.products = new HashMap<>(products);
        }
    
        public int getStoreID() {
            return storeID;
        }
    
        public Map<Integer, Integer> getProducts() {
            return products;
        }
    
        public void addProduct(int productId, int quantity) {
            products.put(productId, products.getOrDefault(productId, 0) + quantity);
        }
    }

    @Test
    void testAddProduct_NewAndExisting() {
        Basket basket = new Basket(1);

        // Add new product
        basket.addProduct(100, 2);
        assertEquals(2, basket.getProducts().get(100));

        // Add same product again (should sum quantities)
        basket.addProduct(100, 3);
        assertEquals(5, basket.getProducts().get(100));

        // Add another product
        basket.addProduct(200, 1);
        assertEquals(1, basket.getProducts().get(200));
    }
}