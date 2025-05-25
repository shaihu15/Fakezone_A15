package UnitTesting;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Interfaces.IProduct;
import InfrastructureLayer.Repositories.ProductRepository;

public class ProductRepositoryTest {
    private ProductRepository repository;
    private IProduct product1;
    private IProduct product2;

    @BeforeEach
    void setUp() {
        repository = new ProductRepository(new HashMap<>());

        // Use Mockito to create mock objects for IProduct
        product1 = mock(IProduct.class);
        product2 = mock(IProduct.class);

        // Define behavior for product1
        when(product1.getId()).thenReturn(1);
        when(product1.getName()).thenReturn("Product1");
        when(product1.getDescription()).thenReturn("good product");
        when(product1.getCategory()).thenReturn(PCategory.ELECTRONICS);
        when(product1.getStoresIds()).thenReturn(new ArrayList<>());
        doNothing().when(product1).setName(anyString());
        doNothing().when(product1).setDescription(anyString());
        doNothing().when(product1).addStore(anyInt());
        doNothing().when(product1).removeStore(anyInt());

        // Define behavior for product2
        when(product2.getId()).thenReturn(2);
        when(product2.getName()).thenReturn("Product2");
        when(product2.getDescription()).thenReturn("another good product");
        when(product2.getCategory()).thenReturn(PCategory.BEAUTY);
        when(product2.getStoresIds()).thenReturn(new ArrayList<>());
        doNothing().when(product2).setName(anyString());
        doNothing().when(product2).setDescription(anyString());
        doNothing().when(product2).addStore(anyInt());
        doNothing().when(product2).removeStore(anyInt());
    }

    @Test
    void givenValidProduct_WhenAddProduct_ThenProductIsAdded() {
        repository.addProduct(product1);
        assertEquals(product1, repository.getProductById(1));
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_ThenProductIsUpdated() {
        repository.addProduct(product1);
        repository.updateProduct(product1.getId(), "UpdatedProduct1", "UpdatedDesc", new HashSet<>(Arrays.asList(5, 6)));
        verify(product1, times(1)).setName("UpdatedProduct1");
        verify(product1, times(1)).setDescription("UpdatedDesc");
        verify(product1, atLeast(1)).addStore(anyInt());
        verify(product1, atLeast(1)).removeStore(anyInt());
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_OnlyName() {
        repository.addProduct(product1);
        repository.updateProduct(product1.getId(), "UpdatedProduct1", null, null);
        verify(product1, times(1)).setName("UpdatedProduct1");
        verify(product1, never()).setDescription(anyString());
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_OnlyDescription() {
        repository.addProduct(product1);
        repository.updateProduct(product1.getId(), null, "UpdatedDesc", null);
        verify(product1, never()).setName(anyString());
        verify(product1, times(1)).setDescription("UpdatedDesc");
    }

    @Test
    void givenNonExistingProduct_WhenUpdateProduct_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateProduct(product1.getId(), "UpdatedProduct1", null, null);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenExistingProduct_WhenDeleteProduct_ThenProductIsDeleted() {
        repository.addProduct(product1);
        repository.deleteProduct(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getProductById(1);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenNonExistingProduct_WhenDeleteProduct_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteProduct(1);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenExistingProduct_WhenGetProductById_ThenReturnsProduct() {
        repository.addProduct(product1);
        IProduct retrievedProduct = repository.getProductById(1);
        assertEquals(product1, retrievedProduct);
    }

    @Test
    void givenNonExistingProduct_WhenGetProductById_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getProductById(1);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenMultipleProducts_WhenGetAllProducts_ThenReturnsAllProducts() {
        repository.addProduct(product1);
        repository.addProduct(product2);
        Collection<IProduct> products = repository.getAllProducts();
        assertTrue(products.contains(product1));
        assertTrue(products.contains(product2));
        assertEquals(2, products.size());
    }

    @Test
    void givenExistingCategory_WhenGetProductsByCategory_ThenReturnsProductsInCategory() {
        repository.addProduct(product1);
        repository.addProduct(product2);
        Collection<IProduct> products = repository.getProductsByCategory(PCategory.ELECTRONICS);
        assertTrue(products.contains(product1));
        assertEquals(1, products.size());
    }

    @Test
    void givenNonExistingCategory_WhenGetProductsByCategory_ThenReturnsEmptyCollection() {
        repository.addProduct(product1);
        Collection<IProduct> products = repository.getProductsByCategory(PCategory.BEAUTY);
        assertTrue(products.isEmpty());
    }

    @Test
    void testSearchProducts_FindsByName() {
        repository.addProduct(product1);
        repository.addProduct(product2);
        Collection<IProduct> found = repository.searchProducts("Product1");
        assertTrue(found.contains(product1));
        assertFalse(found.contains(product2));
    }

    @Test
    void testSearchProducts_FindsByDescription() {
        repository.addProduct(product1);
        repository.addProduct(product2);
        Collection<IProduct> found = repository.searchProducts("good product");
        assertTrue(found.contains(product1));
        assertFalse(found.contains(product2));
    }

    @Test
    void testSearchProductsByName_FindsCorrectProduct() {
        repository.addProduct(product1);
        repository.addProduct(product2);
        Collection<IProduct> found = repository.searchProductsByName("Product2");
        assertTrue(found.contains(product2));
        assertFalse(found.contains(product1));
    }

    @Test
    void testSearchProductsByName_CaseInsensitive() {
        repository.addProduct(product1);
        Collection<IProduct> found = repository.searchProductsByName("product1");
        assertTrue(found.contains(product1));
    }

    @Test
    void testClearAllData() {
        repository.addProduct(product1);
        repository.clearAllData();
        assertEquals(0, repository.getAllProducts().size());
    }
}