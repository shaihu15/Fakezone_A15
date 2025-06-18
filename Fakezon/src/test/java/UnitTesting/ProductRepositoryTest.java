package UnitTesting;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;
import InfrastructureLayer.Repositories.ProductJpaRepository;
import InfrastructureLayer.Repositories.ProductRepository;

public class ProductRepositoryTest {
    private ProductRepository repository;
    private Product product1;
    private Product product2;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new ProductRepository(productJpaRepository);

        // Create actual Product instances instead of mocks
        product1 = new Product("Product1", "good product", PCategory.ELECTRONICS, 1);
        product2 = new Product("Product2", "another good product", PCategory.BEAUTY, 2);

        // Setup JPA repository mock behavior
        when(productJpaRepository.findById(1)).thenReturn(Optional.of(product1));
        when(productJpaRepository.findById(2)).thenReturn(Optional.of(product2));
        when(productJpaRepository.findAll()).thenReturn(Arrays.asList(product1, product2));
        when(productJpaRepository.findByCategory(PCategory.ELECTRONICS)).thenReturn(Arrays.asList(product1));
        when(productJpaRepository.findByCategory(PCategory.BEAUTY)).thenReturn(Arrays.asList(product2));
        when(productJpaRepository.findByCategory(PCategory.FOOD)).thenReturn(new ArrayList<>());
        when(productJpaRepository.searchByKeyword("Product1")).thenReturn(Arrays.asList(product1));
        when(productJpaRepository.searchByKeyword("good product")).thenReturn(Arrays.asList(product1, product2));
        when(productJpaRepository.findByNameContainingIgnoreCase("Product1")).thenReturn(Arrays.asList(product1));
        when(productJpaRepository.findByNameContainingIgnoreCase("Product2")).thenReturn(Arrays.asList(product2));
        when(productJpaRepository.findByNameContainingIgnoreCase("product1")).thenReturn(Arrays.asList(product1));
        when(productJpaRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void givenValidProduct_WhenAddProduct_ThenProductIsAdded() {
        repository.addProduct(product1);
        verify(productJpaRepository).save(product1);
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_ThenProductIsUpdated() {
        repository.updateProduct(1, "UpdatedProduct1", "UpdatedDesc", new HashSet<>(Arrays.asList(5, 6)));
        verify(productJpaRepository).save(product1);
        assertEquals("UpdatedProduct1", product1.getName());
        assertEquals("UpdatedDesc", product1.getDescription());
        assertTrue(product1.getStoresIds().contains(5));
        assertTrue(product1.getStoresIds().contains(6));
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_OnlyName() {
        repository.updateProduct(1, "UpdatedProduct1", null, null);
        verify(productJpaRepository).save(product1);
        assertEquals("UpdatedProduct1", product1.getName());
        assertEquals("good product", product1.getDescription());
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_OnlyDescription() {
        repository.updateProduct(1, null, "UpdatedDesc", null);
        verify(productJpaRepository).save(product1);
        assertEquals("Product1", product1.getName());
        assertEquals("UpdatedDesc", product1.getDescription());
    }

    @Test
    void givenNonExistingProduct_WhenUpdateProduct_ThenThrowsException() {
        when(productJpaRepository.findById(999)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateProduct(999, "UpdatedProduct1", null, null);
        });
        assertEquals("Product not found with id: 999", exception.getMessage());
    }

    @Test
    void givenExistingProduct_WhenDeleteProduct_ThenProductIsDeleted() {
        when(productJpaRepository.existsById(1)).thenReturn(true);
        repository.deleteProduct(1);
        verify(productJpaRepository).deleteById(1);
    }

    @Test
    void givenNonExistingProduct_WhenDeleteProduct_ThenThrowsException() {
        when(productJpaRepository.existsById(999)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteProduct(999);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenExistingProduct_WhenGetProductById_ThenReturnsProduct() {
        IProduct retrievedProduct = repository.getProductById(1);
        assertEquals(product1, retrievedProduct);
    }

    @Test
    void givenNonExistingProduct_WhenGetProductById_ThenThrowsException() {
        when(productJpaRepository.findById(999)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.getProductById(999);
        });
        assertEquals("Product not found in the repository.", exception.getMessage());
    }

    @Test
    void givenMultipleProducts_WhenGetAllProducts_ThenReturnsAllProducts() {
        Collection<IProduct> products = repository.getAllProducts();
        assertTrue(products.contains(product1));
        assertTrue(products.contains(product2));
        assertEquals(2, products.size());
    }

    @Test
    void givenExistingCategory_WhenGetProductsByCategory_ThenReturnsProductsInCategory() {
        Collection<IProduct> products = repository.getProductsByCategory(PCategory.ELECTRONICS);
        assertTrue(products.contains(product1));
        assertEquals(1, products.size());
    }

    @Test
    void givenNonExistingCategory_WhenGetProductsByCategory_ThenReturnsEmptyCollection() {
        Collection<IProduct> products = repository.getProductsByCategory(PCategory.FOOD);
        assertTrue(products.isEmpty());
    }

    @Test
    void testSearchProducts_FindsByName() {
        Collection<IProduct> found = repository.searchProducts("Product1");
        assertTrue(found.contains(product1));
        assertFalse(found.contains(product2));
    }

    @Test
    void testSearchProducts_FindsByDescription() {
        Collection<IProduct> found = repository.searchProducts("good product");
        assertTrue(found.contains(product1));
        assertTrue(found.contains(product2));
    }

    @Test
    void testSearchProductsByName_FindsCorrectProduct() {
        Collection<IProduct> found = repository.searchProductsByName("Product2");
        assertTrue(found.contains(product2));
        assertFalse(found.contains(product1));
    }

    @Test
    void testSearchProductsByName_CaseInsensitive() {
        Collection<IProduct> found = repository.searchProductsByName("product1");
        assertTrue(found.contains(product1));
    }

    @Test
    void testClearAllData() {
        repository.clearAllData();
        verify(productJpaRepository).deleteAll();
    }
}