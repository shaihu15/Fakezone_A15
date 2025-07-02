package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import NewAcceptanceTesting.TestHelper;
import com.fakezone.fakezone.FakezoneApplication;

@SpringBootTest(classes = FakezoneApplication.class)
public class Closing_StoreTest {
    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int storeId;
    int userId;
    String storeName;
    int adminId;
    Integer otherStoreId = null;
    Integer otherUserId = null;
    Integer newAdminUserId = null;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        systemService.resetUserIdSequence(); // <-- Add this after clearing data
        testHelper = new TestHelper(systemService);

        // Register and login the admin user (same as in TempDataLoader)
        String adminEmail = "dev@fakezone.bgu.ac.il";
        String adminPassword = "Devpass1";
        String adminDob = "2000-01-01";
        String adminCountry = "IL";

        Response<String> registerRes = systemService.guestRegister(adminEmail, adminPassword, adminDob, adminCountry);
        if (!registerRes.isSuccess()) {
            System.out.println("Admin registration failed: " + registerRes.getMessage() + ". Proceeding to login.");
        }

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginRes = systemService.login(adminEmail, adminPassword);
        assertTrue(loginRes.isSuccess(), "Admin login failed: " + loginRes.getMessage());
        UserDTO adminUser = loginRes.getData().getKey();
        adminId = adminUser.getUserId();

        // Promote to system admin (if not already)
        Response<Void> promoteRes = systemService.addSystemAdmin(adminId, adminId);
        assertTrue(promoteRes.isSuccess(), "Failed to promote admin user: " + promoteRes.getMessage());

        // Register and login a regular user
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
        // Log out the admin
        systemService.userLogout(adminId);

        Response<String> result = systemService.closeStoreByAdmin(storeId, adminId);
        assertFalse(result.isSuccess());
        assertEquals("User is not logged in", result.getMessage());

        // Log admin back in for cleanup
        systemService.login("dev@fakezone.bgu.ac.il", "Devpass1");
    }

    @Test
    void testCloseStoreByAdmin_alreadyClosed_Failure() {
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
        otherUserId = otherUser.getData().getUserId();

        Response<Integer> otherStoreResult = systemService.addStore(otherUserId, "Other Store");
        otherStoreId = otherStoreResult.getData();

        // Admin should be able to close any store, not just their own
        Response<String> result = systemService.closeStoreByAdmin(otherStoreId, adminId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());
    }

    @Test
    void testCloseStoreByAdmin_adminCanCreateOtherAdmins_Success() {
        // Create a new user to make admin
        Response<UserDTO> newUser = testHelper.register_and_login2();
        newAdminUserId = newUser.getData().getUserId();

        // Existing admin creates new admin
        Response<Void> addAdminResult = systemService.addSystemAdmin(adminId, newAdminUserId);
        assertTrue(addAdminResult.isSuccess());

        // New admin should be able to close stores
        Response<String> result = systemService.closeStoreByAdmin(storeId, newAdminUserId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully by admin", result.getMessage());
    }
}
