package AcceptanceTesting;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.Store;
import DomainLayer.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import ApplicationLayer.Services.SystemService;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import DomainLayer.Model.User;

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

        store1 = new Store("Test Store 1", store1Id, founder1Id);
        store2 = new Store("Test Store 2", store2Id, founder2Id);

    }

    @Test
    void testUserAccessStore() {
        User user1 = new User();
        assertTrue(systemService.userAccessStore(user1.getUserID(), store1Id));
        assertFalse(systemService.userAccessStore(-1, store1Id));
        assertFalse(systemService.userAccessStore(user1.getUserID(), -1));
    }

}
