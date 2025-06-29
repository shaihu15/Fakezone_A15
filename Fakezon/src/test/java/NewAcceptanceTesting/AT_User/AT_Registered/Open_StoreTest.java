package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import com.fakezone.fakezone.FakezoneApplication;
import org.springframework.test.context.ActiveProfiles;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class Open_StoreTest {
    // Use-case: 3.2 Open Store 

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    // Track created user and store for cleanup
    private Integer userId = null;
    private Integer storeId = null;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);
        userId = null;
        storeId = null;
    }

    @Test
    void testOpenStore_validArguments_Success() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();
        String storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        storeId = resultAddStore.getData();

        assertNotNull(storeId);
        assertTrue(systemService.isStoreOpen(storeId));
    }

    @Test
    void testOpenStore_StoreNameAlreadyTaken_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();

        Response<Integer> resultAddStore1 = systemService.addStore(userId, "Test Store");
        storeId = resultAddStore1.getData();

        Response<Integer> resultAddStore2 = systemService.addStore(userId, "Test Store");

        assertNotNull(storeId);
        assertTrue(systemService.isStoreOpen(storeId));
        assertNull(resultAddStore2.getData());
        assertEquals("Error during opening store: Store name already exists", resultAddStore2.getMessage());
    }

    @Test
    void testOpenStore_StoreNameIsEmpty_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();

        String invalidStoreName = ""; 

        Response<Integer> resultAddStore = systemService.addStore(userId, invalidStoreName);

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: Store name is empty", resultAddStore.getMessage());
    }

    @Test
    void testOpenStore_StoreNameIsNull_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();

        Response<Integer> resultAddStore = systemService.addStore(userId, null);

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: Store name is empty", resultAddStore.getMessage());
    }

    @Test
    void testOpenStore_UserNotRegistered_Failure() {
        userId = 9999; // Assuming this user ID does not exist

        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: User not found", resultAddStore.getMessage());
    }

    @AfterEach
    void tearDown() {
        // Try to close the store (ignore if already closed)
        if (storeId != null && userId != null) {
            Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, userId);
            // Ignore if already closed or not found
        }

        // Try to remove the store (ignore if already removed)
        if (storeId != null && userId != null) {
            systemService.removeStore(storeId, userId);
        }

        // Now delete the user (ignore if already deleted)
        if (userId != null) {
            systemService.deleteUser(testHelper.validEmail());
        }
    }
}
