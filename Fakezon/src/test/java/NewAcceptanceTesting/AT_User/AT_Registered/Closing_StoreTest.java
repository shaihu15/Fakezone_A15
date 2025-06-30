package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class Closing_StoreTest {
    //Use-case: 4.9 Closing Store

     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int storeId;
    int userId;
    String storeName;

    @BeforeEach
    void setUp() {

        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);       

        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();
        storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        storeId = resultAddStore.getData();
    }

    @Test
    void testCloseStore_validArguments_Success() {
        Response<String> result = systemService.closeStoreByFounder(storeId, userId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully", result.getMessage());

        Response<String> result2 = systemService.closeStoreByFounder(storeId, userId);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store: Store: 1 is already closed", result2.getMessage());
    }

    @Test
    void testCloseStore_invalidStoreId_Failure() {
        Response<String> result = systemService.closeStoreByFounder(-1, userId);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: Store not found", result.getMessage());
    }

    @Test
    void testCloseStore_invalidUserId_Failure() {
        Response<String> result = systemService.closeStoreByFounder(storeId, -1);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: User not found", result.getMessage());
    }

    @Test
    void testCloseStore_userNotFounder_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login2();
        int notFoundeId = resultUser.getData().getUserId();

        Response<String> result = systemService.closeStoreByFounder(storeId, notFoundeId);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: Requester ID: " + notFoundeId + " is not a Store Founder of store: " + storeId, result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_validArguments_Success() {
        // Use the pre-configured admin user (ID: 1001)
        int adminId = 1001;
        
        Response<String> result = systemService.closeStoreByAdmin(storeId, adminId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());

        // Verify store is actually closed
        Response<String> result2 = systemService.closeStoreByAdmin(storeId, adminId);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store by admin: Store: " + storeId + " is already closed", result2.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_invalidStoreId_Failure() {
        // Use the pre-configured admin user (ID: 1001)
        int adminId = 1001;
        
        Response<String> result = systemService.closeStoreByAdmin(-1, adminId);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store by admin: Store not found", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_userNotAdmin_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login2();
        int notAdminId = resultUser.getData().getUserId();

        Response<String> result = systemService.closeStoreByAdmin(storeId, notAdminId);
        assertFalse(result.isSuccess());
        assertEquals("User is not a system admin", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_invalidUserId_Failure() {
        Response<String> result = systemService.closeStoreByAdmin(storeId, -1);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store by admin: User not found", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_userNotLoggedIn_Failure() {
        // Use the pre-configured admin user (ID: 1001) but log them out
        int adminId = 1001;
        
        // First ensure admin is logged in, then log them out
        systemService.login("testFounder1001@gmail.com", "a12345"); // Login the admin
        systemService.userLogout(adminId); // Then log them out
        
        Response<String> result = systemService.closeStoreByAdmin(storeId, adminId);
        assertFalse(result.isSuccess());
        assertEquals("User is not logged in", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_alreadyClosed_Failure() {
        // Use the pre-configured admin user (ID: 1001)
        int adminId = 1001;
        
        // Close store first time
        Response<String> result1 = systemService.closeStoreByAdmin(storeId, adminId);
        assertTrue(result1.isSuccess());
        
        // Try to close again
        Response<String> result2 = systemService.closeStoreByAdmin(storeId, adminId);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store by admin: Store: " + storeId + " is already closed", result2.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_canCloseAnyStore_Success() {
        // Create another user and their store
        Response<UserDTO> otherUser = testHelper.register_and_login2();
        int otherUserId = otherUser.getData().getUserId();
        
        Response<Integer> otherStoreResult = systemService.addStore(otherUserId, "Other Store");
        int otherStoreId = otherStoreResult.getData();
        
        // Use the pre-configured admin user (ID: 1001)
        int adminId = 1001;
        
        // Admin should be able to close any store, not just their own
        Response<String> result = systemService.closeStoreByAdmin(otherStoreId, adminId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_adminCanCreateOtherAdmins_Success() {
        // Use the pre-configured admin user (ID: 1001) to create another admin
        int existingAdminId = 1001;
        
        // Create a new user to make admin
        Response<UserDTO> newUser = testHelper.register_and_login2();
        int newUserId = newUser.getData().getUserId();
        
        // Existing admin creates new admin
        Response<Void> addAdminResult = systemService.addSystemAdmin(existingAdminId, newUserId);
        assertTrue(addAdminResult.isSuccess());
        
        // New admin should be able to close stores
        Response<String> result = systemService.closeStoreByAdmin(storeId, newUserId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());
    }

}
