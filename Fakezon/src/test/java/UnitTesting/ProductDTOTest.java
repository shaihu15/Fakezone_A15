package UnitTesting;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDTOTest {

    private ProductDTO productDTO;
    private Set<Integer> storeIds;

    @BeforeEach
    void setUp() {
        // Initialize test data
        storeIds = new HashSet<>();
        storeIds.add(1);
        storeIds.add(2);

        productDTO = new ProductDTO("Test Product", "This is a test product", 1001, PCategory.ELECTRONICS, storeIds);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(productDTO, "ProductDTO object should be created");
        assertEquals("Test Product", productDTO.getName(), "Product name should match");
        assertEquals("This is a test product", productDTO.getDescription(), "Product description should match");
        assertEquals(1001, productDTO.getId(), "Product ID should match");
        assertEquals(PCategory.ELECTRONICS, productDTO.getCategory(), "Product category should match");
        assertEquals(2, productDTO.getStoresIds().size(), "Product should be associated with 2 stores");
    }

    @Test
    void constructor_WithoutStoreIds_ShouldCreateObject() {
        ProductDTO productWithoutStores = new ProductDTO("Test Product", "This is a test product", 1002, PCategory.BEAUTY);
        assertNotNull(productWithoutStores, "ProductDTO object should be created");
        assertEquals("Test Product", productWithoutStores.getName(), "Product name should match");
        assertEquals("This is a test product", productWithoutStores.getDescription(), "Product description should match");
        assertEquals(1002, productWithoutStores.getId(), "Product ID should match");
        assertEquals(PCategory.BEAUTY, productWithoutStores.getCategory(), "Product category should match");
        assertNull(productWithoutStores.getStoresIds(), "Store IDs should be null");
    }

    @Test
    void getStoresIds_ShouldReturnCorrectStoreIds() {
        Set<Integer> retrievedStoreIds = productDTO.getStoresIds();
        assertEquals(2, retrievedStoreIds.size(), "Store IDs size should match");
        assertTrue(retrievedStoreIds.contains(1), "Store ID 1 should be present");
        assertTrue(retrievedStoreIds.contains(2), "Store ID 2 should be present");
    }

    @Test
    void getId_ShouldReturnCorrectId() {
        assertEquals(1001, productDTO.getId(), "Product ID should match");
    }

    @Test
    void getName_ShouldReturnCorrectName() {
        assertEquals("Test Product", productDTO.getName(), "Product name should match");
    }

    @Test
    void setName_ValidName_ShouldUpdateName() {
        productDTO.setName("Updated Product");
        assertEquals("Updated Product", productDTO.getName(), "Product name should be updated");
    }

    @Test
    void getDescription_ShouldReturnCorrectDescription() {
        assertEquals("This is a test product", productDTO.getDescription(), "Product description should match");
    }

    @Test
    void setDescription_ValidDescription_ShouldUpdateDescription() {
        productDTO.setDescription("Updated description");
        assertEquals("Updated description", productDTO.getDescription(), "Product description should be updated");
    }

    @Test
    void getCategory_ShouldReturnCorrectCategory() {
        assertEquals(PCategory.ELECTRONICS, productDTO.getCategory(), "Product category should match");
    }
}