package AcceptanceTesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import ApplicationLayer.Services.SystemService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
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
        storeRepository = mock(IStoreRepository.class);
        userRepository = mock(IUserRepository.class);
        productRepository = mock(IProductRepository.class);
        systemService = new SystemService(storeRepository, userRepository, productRepository);

        store1 = new Store("Test Store 1", founder1Id);
        store2 = new Store("Test Store 2", founder2Id);

    }

    @Test
    void testUserAccessStore() {
        // after regestration will be solved we can test this method
        User user1 = new User();
        // assertTrue(systemService.userAccessStore(0, store1Id));
        // assertFalse(systemService.userAccessStore(-1, store1Id));
        // assertFalse(systemService.userAccessStore(user1.getUserID(), -1));
    }

    @Test
    void testGetStoreProductById() {
        /***********************
         * 
         * // This test is not working because the method addUser in user repository is
         * not implemented yet.
         *****************/
        // int productId = 100;
        // String productName = "Test Product";

        // // Mocking a product
        // Product product = Mockito.mock(DomainLayer.Model.Product.class);
        // when(product.getId()).thenReturn(productId);
        // when(product.getName()).thenReturn(productName);

        // // Mocking store behavior
        // store1 = Mockito.mock(Store.class);
        // when(store1.getId()).thenReturn(store1Id);
        // when(storeRepository.findById(store1Id)).thenReturn(store1);

        // doNothing().when(store1).addStoreProduct(productId, productName, store1Id,
        // productId, null);

        // // Act
        // systemService.openStore(founder1Id, "Store 1");
        // //need to replace state with actual state
        // store1.addStoreProduct(productId, productName, store1Id, productId, null);
        // StoreProductDTO result = systemService.getProductFromStore(store1Id,
        // productId);

        // // Assert
        // assertNotNull(result, "The product should not be null");
        // assertEquals(productId, result.getProductId(), "Product ID should match");
        // assertEquals(productName, result.getName(), "Product name should match");

        // // Verify interactions
        // verify(storeRepository, times(1)).findById(store1Id);
    }

}
