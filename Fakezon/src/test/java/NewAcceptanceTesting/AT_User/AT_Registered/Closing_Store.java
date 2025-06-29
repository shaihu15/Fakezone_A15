package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfiles;

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
import static org.mockito.Mockito.mock;
import java.util.AbstractMap;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")
public class Closing_Store {
    //Use-case: 4.9 Closing Store

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int storeId;
    int userId;
    String storeName;

    // Track additional stores/users created in tests
    Integer otherStoreId = null;
    Integer otherUserId = null;
    Integer newAdminUserId = null;

    private static final String ADMIN_EMAIL = "dev@fakezone.bgu.ac.il";
    private static final String ADMIN_PASSWORD = "Devpass1";
    private static final int ADMIN_ID = 1; // If you know it's always 1, otherwise fetch after login

    @BeforeAll
    static void createAdminUser(@Autowired SystemService systemService) {
        // Remove all users to ensure the next user gets ID 1
        // If you have a method to clear all users, use it here.
        // Otherwise, at least delete "admin@test.com" and any other test users.
        systemService.deleteUser("admin@test.com");
        systemService.deleteUser("user1@test.com");
        systemService.deleteUser("user2@test.com");
        // ...delete any other known test users...
       
        // Register the admin user FIRST
        systemService.guestRegister("admin@test.com", "a12345", "1990-01-01", "IL");
        // Login to get the userId (should be 1)
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginRes = systemService.login("admin@test.com", "a12345");
        int adminId = loginRes.getData().getKey().getUserId();
        // No need to call addSystemAdmin if TempDataLoader does it for ID 1
    }

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        // Clean up users and stores before creating them - be more thorough
        systemService.deleteUser(testHelper.validEmail());
        systemService.deleteUser(testHelper.validEmail2());  // Always clean up second user
        
        // Also logout any lingering sessions to ensure clean state
        systemService.userLogout(ADMIN_ID);
        
        // Try to remove any existing stores (best effort cleanup)
        // This is defensive programming in case previous tests left stores

        // Register and login main user
        Response<UserDTO> resultUser = testHelper.register_and_login();
        assertTrue(resultUser.isSuccess(), "Failed to register main user");
        userId = resultUser.getData().getUserId();
        storeName = "Test Store";

        // Add main store
        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        assertTrue(resultAddStore.isSuccess(), "Failed to add main store");
        storeId = resultAddStore.getData();

        // Reset tracking for additional stores/users
        otherStoreId = null;
        otherUserId = null;
        newAdminUserId = null;
    }

    @AfterEach
    void tearDown() {
        // Remove the main store (ignore if already removed)
        if (storeId > 0) {
            // First, close the store (ignore if already closed or not found)
            Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, userId);
            if (!closeStoreRes.isSuccess()) {
                String msg = closeStoreRes.getMessage();
                assertTrue(
                    msg.contains("already closed") ||
                    msg.contains("Store not found") ||
                    msg.contains("User not found"),
                    "Failed to close main store: " + msg
                );
            }

            // Now, remove the store (ignore if already removed)
            Response<Void> removeStoreRes = systemService.removeStore(storeId, userId);
            if (!removeStoreRes.isSuccess()) {
                String msg = removeStoreRes.getMessage();
                assertTrue(
                    msg.contains("Store not found") || msg.contains("Error during removing store"),
                    "Failed to remove main store: " + msg
                );
            }
        }

        // Remove additional store if created in test
        if (otherStoreId != null && otherUserId != null) {
            Response<Void> removeOtherStoreRes = systemService.removeStore(otherStoreId, otherUserId);
            if (!removeOtherStoreRes.isSuccess()) {
                String msg = removeOtherStoreRes.getMessage();
                assertTrue(
                    msg.contains("Store not found") || msg.contains("Error during removing store"),
                    "Failed to remove other store: " + msg
                );
            }
        }

        // **CRITICAL FIX**: Always clean up both users to prevent test isolation issues
        // Remove the main user (ignore if already deleted)
        Response<Boolean> deleteUserRes = systemService.deleteUser(testHelper.validEmail());
        if (!deleteUserRes.isSuccess()) {
            String msg = deleteUserRes.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Failed to delete main user: " + msg
            );
        }

        // **ALWAYS** remove the secondary user to ensure test isolation
        // This is the key fix for the failing test
        systemService.deleteUser(testHelper.validEmail2());
        // Ignore result, just ensure user is deleted if exists

        // Remove new admin user if created (redundant but safe)
        if (newAdminUserId != null) {
            systemService.deleteUser(testHelper.validEmail2());
        }

        // Logout any users that might still be logged in to clean up sessions
        if (userId > 0) {
            systemService.userLogout(userId);
        }
        if (otherUserId != null) {
            systemService.userLogout(otherUserId);
        }
        systemService.userLogout(ADMIN_ID);

        // Reset tracking variables
        storeId = 0;
        userId = 0;
        otherStoreId = null;
        otherUserId = null;
        newAdminUserId = null;
    }

    @Test
    void testCloseStore_validArguments_Success() {
        Response<String> result = systemService.closeStoreByFounder(storeId, userId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully", result.getMessage());

        Response<String> result2 = systemService.closeStoreByFounder(storeId, userId);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store: Store: " + storeId + " is already closed", result2.getMessage());
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
        systemService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        Response<String> result = systemService.closeStoreByAdmin(storeId, ADMIN_ID);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());

        // Verify store is actually closed
        Response<String> result2 = systemService.closeStoreByAdmin(storeId, ADMIN_ID);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store by admin: Store: " + storeId + " is already closed", result2.getMessage());
        systemService.userLogout(ADMIN_ID); // Logout the admin after test
    }

    @Test
    void testCloseStoreByAdmin_invalidStoreId_Failure() {
        systemService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        Response<String> result = systemService.closeStoreByAdmin(-1, ADMIN_ID);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store by admin: Store not found", result.getMessage());
        systemService.userLogout(ADMIN_ID);
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
        systemService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        systemService.userLogout(ADMIN_ID);

        Response<String> result = systemService.closeStoreByAdmin(storeId, ADMIN_ID);
        assertFalse(result.isSuccess());
        assertEquals("User is not logged in", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_alreadyClosed_Failure() {
        systemService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        // Close store first time
        Response<String> result1 = systemService.closeStoreByAdmin(storeId, ADMIN_ID);
        assertTrue(result1.isSuccess());

        // Try to close again
        Response<String> result2 = systemService.closeStoreByAdmin(storeId, ADMIN_ID);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store by admin: Store: " + storeId + " is already closed", result2.getMessage());
        systemService.userLogout(ADMIN_ID);
    }

    // @Test
    // void testCloseStoreByAdmin_canCloseAnyStore_Success() {
    //     // Force logout admin to ensure clean state
    //     systemService.userLogout(ADMIN_ID);
        
    //     // Extra defensive cleanup - ensure user2 is deleted before creating
    //     systemService.deleteUser(testHelper.validEmail2());

    //     // Create another user and their store
    //     Response<UserDTO> otherUser = testHelper.register_and_login2();
    //     assertNotNull(otherUser, "register_and_login2() returned null");
        
    //     if (!otherUser.isSuccess()) {
    //         // Add debugging information if registration fails
    //         System.out.println("register_and_login2() failed with message: " + otherUser.getMessage());
    //         System.out.println("Attempting to register with email: " + testHelper.validEmail2());
    //     }
        
    //     assertTrue(otherUser.isSuccess(), "register_and_login2() failed: " + otherUser.getMessage());
    //     otherUserId = otherUser.getData().getUserId();

    //     Response<Integer> otherStoreResult = systemService.addStore(otherUserId, "Other Store");
    //     assertNotNull(otherStoreResult, "addStore() returned null");
    //     assertTrue(otherStoreResult.isSuccess(), "addStore() failed: " + otherStoreResult.getMessage());
    //     otherStoreId = otherStoreResult.getData();

    //     // Now login admin
    //     Response<AbstractMap.SimpleEntry<UserDTO, String>> adminLogin = systemService.login(ADMIN_EMAIL, ADMIN_PASSWORD);
    //     assertTrue(adminLogin.isSuccess(), "Admin login failed: " + adminLogin.getMessage());
        
    //     // Admin should be able to close any store, not just their own
    //     Response<String> result = systemService.closeStoreByAdmin(otherStoreId, ADMIN_ID);
    //     assertTrue(result.isSuccess(), "Admin failed to close store: " + result.getMessage());
    //     assertEquals("Store closed successfully by admin", result.getMessage());
        
    //     systemService.userLogout(ADMIN_ID);

    //     // Explicit cleanup at the end of the test (this will also be done in @AfterEach)
    //     systemService.deleteUser(testHelper.validEmail2());
    // }

    @Test
    void testCloseStoreByAdmin_adminCanCreateOtherAdmins_Success() {
        // Create a new user to make admin
        Response<UserDTO> newUser = testHelper.register_and_login2();
        newAdminUserId = newUser.getData().getUserId();

        // Existing admin creates new admin
        Response<Void> addAdminResult = systemService.addSystemAdmin(ADMIN_ID, newAdminUserId);
        assertTrue(addAdminResult.isSuccess());

        // New admin should be able to close stores
        Response<String> result = systemService.closeStoreByAdmin(storeId, newAdminUserId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());

        // Explicit cleanup at the end of the test (this will also be done in @AfterEach)
        systemService.deleteUser(testHelper.validEmail2());
    }

}
