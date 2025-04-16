package UnitTesting;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DomainLayer.Interfaces.IProduct;
import InfrastructureLayer.Repositories.ProductRepository;

class MockProduct implements IProduct {
    private int id;
    private String name;
    private String prodcutDescription;

    public MockProduct(int id, String name, String prodcutDescription) {
        this.id = id;
        this.name = name;
        this.prodcutDescription = prodcutDescription;
    }

    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return prodcutDescription;    
    }

    @Override
    public void setDescription(String description) {
        this.prodcutDescription = description;
    }
}

public class ProductRepositoryTest {
    private ProductRepository repository;
    private MockProduct product1;
    private MockProduct product2;

    @BeforeEach
    void setUp() {
        repository = new ProductRepository(new HashMap<>());
        product1 = new MockProduct(1, "Product1", "good product");
        product2 = new MockProduct(2, "Product2", "another good product");
    }

    @Test
    void givenValidProduct_WhenAddProduct_ThenProductIsAdded() {
        repository.addProduct(product1);
        assertEquals(product1, repository.getProductById(1));
    }

    @Test
    void givenExistingProduct_WhenUpdateProduct_ThenProductIsUpdated() {
        repository.addProduct(product1);
        product1.setName("UpdatedProduct1");
        repository.updateProduct(product1);
        IProduct updatedProduct = repository.getProductById(1);
        assertEquals("UpdatedProduct1", updatedProduct.getName());
    }

    @Test
    void givenNonExistingProduct_WhenUpdateProduct_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateProduct(product1);
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
}