package AcceptanceTesting;

import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.Store;
import DomainLayer.Model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Model.Product;

public class SystemServiceAcceptanceTest {
    private SystemService systemService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private Store store1;
    private int store1Id = 1;
    private int founder1Id = 10;
    private Store store2;
    private int store2Id = 2;
    private int founder2Id = 20;

    @BeforeEach
    void setUp() {
        storeRepository = Mockito.mock(IStoreRepository.class);
        userRepository = Mockito.mock(IUserRepository.class);
        productRepository = Mockito.mock(IProductRepository.class);
        systemService = new SystemService(storeRepository, userRepository, productRepository);
    }

    @Test
    void testGetStoreProductById() {
        int productId = 100;
        String productName = "Test Product";

        // Mocking a product
        Product product = Mockito.mock(DomainLayer.Model.Product.class);
        when(product.getId()).thenReturn(productId);
        when(product.getName()).thenReturn(productName);


        // Mocking store behavior
        store1 = Mockito.mock(Store.class);
        when(store1.getId()).thenReturn(store1Id);
        when(storeRepository.findById(store1Id)).thenReturn(store1);
        
        doNothing().when(store1).addStoreProduct(productId, productName, store1Id, productId, null);

        // Act
        systemService.openStore(founder1Id, "Store 1");
        //need to replace state with actual state
        store1.addStoreProduct(productId, productName, store1Id, productId, null);
        StoreProductDTO result = systemService.getProductFromStore(store1Id, productId);

        // Assert
        assertNotNull(result, "The product should not be null");
        assertEquals(productId, result.getProductId(), "Product ID should match");
        assertEquals(productName, result.getName(), "Product name should match");

        // Verify interactions
        verify(storeRepository, times(1)).findById(store1Id);
    }

}
