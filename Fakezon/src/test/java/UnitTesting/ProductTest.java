package UnitTesting;

import DomainLayer.Model.Product;
import ApplicationLayer.Enums.PCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void givenNewProduct_WhenConstructing_ThenInitializeFields() {
        product = new Product("Laptop", "Gaming laptop", PCategory.ELECTRONICS);
    }

    @Test
    void givenValidProductDetails_WhenUsingDefaultConstructor_ThenFieldsAreSetCorrectly() {
        assertNotNull(product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals("Gaming laptop", product.getDescription());
        assertEquals(PCategory.ELECTRONICS, product.getCategory());
        assertTrue(product.getStoresIds().isEmpty());
    }

    @Test
    void givenAllFields_WhenUsingFullConstructor_ThenFieldsAreInitialized() {
        Set<Integer> storeIds = new HashSet<>(List.of(1, 2));
        Product fullProduct = new Product(42, "Phone", "Smartphone", PCategory.ELECTRONICS, storeIds);

        assertEquals(42, fullProduct.getId());
        assertEquals("Phone", fullProduct.getName());
        assertEquals("Smartphone", fullProduct.getDescription());
        assertEquals(PCategory.ELECTRONICS, fullProduct.getCategory());
        assertEquals(List.of(1, 2), fullProduct.getStoresIds());
    }

    @Test
    void givenProduct_WhenSetNameIsCalled_ThenNameIsUpdated() {
        product.setName("Tablet");
        assertEquals("Tablet", product.getName());
    }

    @Test
    void givenProduct_WhenSetDescriptionIsCalled_ThenDescriptionIsUpdated() {
        product.setDescription("Lightweight");
        assertEquals("Lightweight", product.getDescription());
    }

    @Test
    void givenProduct_WhenSetIdIsCalled_ThenIdIsUpdated() {
        product.setId(100);
        assertEquals(100, product.getId());
    }

    @Test
    void givenStoreId_WhenAddStoreIsCalled_ThenStoreIsAdded() {
        product.addStore(123);
        assertTrue(product.getStoresIds().contains(123));
    }

    @Test
    void givenExistingStoreId_WhenRemoveStoreIsCalled_ThenStoreIsRemoved() {
        product.addStore(456);
        product.removeStore(456);
        assertFalse(product.getStoresIds().contains(456));
    }

    @Test
    void givenNullStoreSet_WhenAddStoreIsCalled_ThenSetIsInitializedAndStoreIsAdded() throws Exception {
        var field = Product.class.getDeclaredField("storesIds");
        field.setAccessible(true);
        field.set(product, null);

        product.addStore(789);
        assertTrue(product.getStoresIds().contains(789));
    }

    @Test
    void givenNullStoreSet_WhenRemoveStoreIsCalled_ThenNoExceptionThrown() throws Exception {
        var field = Product.class.getDeclaredField("storesIds");
        field.setAccessible(true);
        field.set(product, null);

        assertDoesNotThrow(() -> product.removeStore(999));
    }

    @Test
    void givenProduct_WhenGetCategoryIsCalled_ThenCorrectCategoryIsReturned() {
        assertEquals(PCategory.ELECTRONICS, product.getCategory());
    }
}
