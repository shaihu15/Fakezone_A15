package IntegrationTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;

@SpringBootTest(classes = com.fakezone.fakezone.FakezoneApplication.class)
@Transactional
public class ProductPersistenceTest {

    @Autowired
    private IProductRepository productRepository;

    @Test
    public void testSaveAndRetrieveProduct() {
        // Given
        Product product = new Product("Test Product", "Test Description", PCategory.ELECTRONICS);
        
        // When
        productRepository.addProduct(product);
        
        // Then
        IProduct retrievedProduct = productRepository.getProductById(product.getId());
        assertNotNull(retrievedProduct);
        assertEquals("Test Product", retrievedProduct.getName());
        assertEquals("Test Description", retrievedProduct.getDescription());
        assertEquals(PCategory.ELECTRONICS, retrievedProduct.getCategory());
    }

    @Test
    public void testSearchProductsByCategory() {
        // Given
        Product product1 = new Product("Electronics Product", "Description", PCategory.ELECTRONICS);
        Product product2 = new Product("Fashion Product", "Description", PCategory.FASHION);
        
        productRepository.addProduct(product1);
        productRepository.addProduct(product2);
        
        // When
        Collection<IProduct> electronicsProducts = productRepository.getProductsByCategory(PCategory.ELECTRONICS);
        
        // Then
        boolean found = electronicsProducts.stream()
            .anyMatch(p -> p.getName().equals("Electronics Product") && p.getCategory() == PCategory.ELECTRONICS);
        assertTrue(found);
    }

    @Test
    public void testUpdateProduct() {
        // Given
        Product product = new Product("Original Name", "Original Description", PCategory.BOOKS);
        productRepository.addProduct(product);
        
        // When
        productRepository.updateProduct(product.getId(), "Updated Name", "Updated Description", null);
        
        // Then
        IProduct updatedProduct = productRepository.getProductById(product.getId());
        assertEquals("Updated Name", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
    }

    @Test
    public void testDeleteProduct() {
        // Given
        Product product = new Product("To Delete", "Description", PCategory.TOYS);
        productRepository.addProduct(product);
        int productId = product.getId();
        
        // When
        productRepository.deleteProduct(productId);
        
        // Then
        assertThrows(InvalidDataAccessApiUsageException.class, () -> productRepository.getProductById(productId));
    }
} 