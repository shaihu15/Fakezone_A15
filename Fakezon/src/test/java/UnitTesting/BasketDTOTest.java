package UnitTesting;

import ApplicationLayer.DTO.BasketDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BasketDTOTest {

    private BasketDTO basketDTO;
    private Map<StoreProductDTO, Boolean> products;

    @BeforeEach
    void setUp() {
        // Initialize test data
        products = new HashMap<>();
        products.put(new StoreProductDTO(1, "Product 1", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS), true);
        products.put(new StoreProductDTO(2, "Product 2", 20.0, 1, 4.0, 1, PCategory.BEAUTY), false);

        basketDTO = new BasketDTO(1001, products);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(basketDTO, "BasketDTO object should be created");
        assertEquals(1001, basketDTO.getStoreId(), "Store ID should match");
        assertEquals(2, basketDTO.getProducts().size(), "Basket should contain 2 products");
    }

    @Test
    void getStoreId_ShouldReturnCorrectId() {
        assertEquals(1001, basketDTO.getStoreId(), "Store ID should match");
    }

    @Test
    void getProducts_ShouldReturnCorrectProducts() {
        Map<StoreProductDTO, Boolean> retrievedProducts = basketDTO.getProducts();
        assertEquals(2, retrievedProducts.size(), "Products size should match");

        StoreProductDTO product1 = retrievedProducts.keySet().stream()
                .filter(p -> p.getProductId() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(product1, "Product 1 should exist in the basket");
        assertTrue(retrievedProducts.get(product1), "Product 1 should be marked as true");

        StoreProductDTO product2 = retrievedProducts.keySet().stream()
                .filter(p -> p.getProductId() == 2)
                .findFirst()
                .orElse(null);
        assertNotNull(product2, "Product 2 should exist in the basket");
        assertFalse(retrievedProducts.get(product2), "Product 2 should be marked as false");
    }
}