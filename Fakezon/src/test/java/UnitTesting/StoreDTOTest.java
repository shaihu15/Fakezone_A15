package UnitTesting;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StoreDTOTest {

    private StoreDTO storeDTO;
    private Collection<StoreProductDTO> storeProducts;
    private Map<Integer, Double> ratings;

    @BeforeEach
    void setUp() {
        // Initialize test data
        storeProducts = new ArrayList<>();
        storeProducts.add(new StoreProductDTO(1, "Product 1", 10.0, 5, 4.5, 1, PCategory.ELECTRONICS));
        storeProducts.add(new StoreProductDTO(2, "Product 2", 20.0, 3, 4.0, 1, PCategory.BEAUTY));

        ratings = new HashMap<>();
        ratings.put(1, 4.5);
        ratings.put(2, 4.0);

        storeDTO = new StoreDTO(1, "Test Store", 10, true, storeProducts, ratings, 4.25);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(storeDTO, "StoreDTO object should be created");
        assertEquals(1, storeDTO.getStoreId(), "Store ID should match");
        assertEquals("Test Store", storeDTO.getName(), "Store name should match");
        assertEquals(10, storeDTO.getFounderId(), "Founder ID should match");
        assertTrue(storeDTO.isOpen(), "Store should be open");
        assertEquals(2, storeDTO.getStoreProducts().size(), "Store should have 2 products");
        assertEquals(2, storeDTO.getRatings().size(), "Store should have 2 ratings");
    }

    @Test
    void getStoreProductById_ExistingProduct_ShouldReturnProduct() {
        StoreProductDTO product = storeDTO.getStoreProductById(1);
        assertNotNull(product, "Product should be found");
        assertEquals(1, product.getProductId(), "Product ID should match");
        assertEquals("Product 1", product.getName(), "Product name should match");
    }

    @Test
    void getStoreProductById_NonExistingProduct_ShouldThrowException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> storeDTO.getStoreProductById(99),
                "Expected getStoreProductById to throw for non-existing product"
        );
        assertTrue(thrown.getMessage().contains("Product with ID: 99 does not exist"), "Exception message should match");
    }

    @Test
    void getRatings_ShouldReturnRatings() {
        Map<Integer, Double> retrievedRatings = storeDTO.getRatings();
        assertEquals(2, retrievedRatings.size(), "Ratings size should match");
        assertEquals(4.5, retrievedRatings.get(1), "Rating for user 1 should match");
        assertEquals(4.0, retrievedRatings.get(2), "Rating for user 2 should match");
    }

    @Test
    void isOpen_ShouldReturnCorrectValue() {
        assertTrue(storeDTO.isOpen(), "Store should be open");
    }

    @Test
    void getStoreId_ShouldReturnCorrectId() {
        assertEquals(1, storeDTO.getStoreId(), "Store ID should match");
    }

    @Test
    void getName_ShouldReturnCorrectName() {
        assertEquals("Test Store", storeDTO.getName(), "Store name should match");
    }

    @Test
    void getFounderId_ShouldReturnCorrectFounderId() {
        assertEquals(10, storeDTO.getFounderId(), "Founder ID should match");
    }

    @Test
    void getStoreProducts_ShouldReturnCorrectProducts() {
        Collection<StoreProductDTO> products = storeDTO.getStoreProducts();
        assertEquals(2, products.size(), "Store should have 2 products");
    }
}