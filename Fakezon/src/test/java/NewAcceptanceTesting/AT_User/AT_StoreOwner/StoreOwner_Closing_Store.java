package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class StoreOwner_Closing_Store {
    // Use-case: 4.9 Close a store
    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int OwnerUserId;
    int storeId;

    @BeforeEach
    void setUp() {

        testHelper = new TestHelper(systemService);

        // Setup Owner
        Response<UserDTO> OwnerUser = testHelper.register_and_login();
        assertTrue(OwnerUser.isSuccess());
        OwnerUserId = OwnerUser.getData().getUserId();

        // owner creates a store
        Response<Integer> storeIdResponse = testHelper.openStore(OwnerUserId);
        assertTrue(storeIdResponse.isSuccess());
        storeId = storeIdResponse.getData();
    }
    @AfterEach
    void tearDown() {
        // Close and remove the main store (ignore if already closed/removed)
        if (systemService.isStoreOpen(storeId)) {
            Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, OwnerUserId);
            assertTrue(closeStoreResponse.isSuccess() ||
                       closeStoreResponse.getMessage().contains("already closed") ||
                       closeStoreResponse.getMessage().contains("Store not found"),
                       "Unexpected close store message: " + closeStoreResponse.getMessage());
        }
        Response<Void> removeStoreResponse = systemService.removeStore(storeId, OwnerUserId);
        if (!removeStoreResponse.isSuccess()) {
            assertTrue(removeStoreResponse.getMessage().contains("Store not found"),
                       "Unexpected remove store message: " + removeStoreResponse.getMessage());
        }

        // Delete the main owner user
        Response<Boolean> deleteOwnerResponse = systemService.deleteUser(testHelper.validEmail());
        if (!deleteOwnerResponse.isSuccess()) {
            String msg = deleteOwnerResponse.getMessage();
            assertTrue(msg.equals("User not found") || msg.equals("Error during deleting user"),
                       "Unexpected delete user message: " + msg);
        }

        // Delete any secondary users and stores created in tests
        Response<Boolean> deleteSecondOwnerResponse = systemService.deleteUser(testHelper.validEmail2());
        if (!deleteSecondOwnerResponse.isSuccess()) {
            String msg = deleteSecondOwnerResponse.getMessage();
            assertTrue(msg.equals("User not found") || msg.equals("Error during deleting user"),
                       "Unexpected delete user message: " + msg);
        }
    }

    @Test
    void testStoreOwnerClosingStore_Success() {
        // owner closes the store
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertTrue(closeStoreResponse.isSuccess());
        assertEquals("Store closed successfully",closeStoreResponse.getMessage());

        // verify that the store is closed
        assertFalse(systemService.isStoreOpen(storeId));
    }

    @Test
    void testStoreOwnerClosingStore_OwnerTriesToCloseThStorAgain_Failure() {
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertTrue(closeStoreResponse.isSuccess());
        assertEquals("Store closed successfully",closeStoreResponse.getMessage());
        
        // verify that the store is closed
        assertFalse(systemService.isStoreOpen(storeId));

        Response<String> closeStoreResponse2 = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertFalse(closeStoreResponse2.isSuccess());
        assertEquals("Error during closing store: Store: " + storeId + " is already closed", closeStoreResponse2.getMessage());

        // verify that the store is still closed
        assertFalse(systemService.isStoreOpen(storeId));
    }

    @Test
    void testStoreOwnerClosingStore_OwnerTriesToCloseAnotherStore_Failure() {
        // another store owner
        Response<UserDTO> anotherOwnerUser = testHelper.register_and_login2();
        assertTrue(anotherOwnerUser.isSuccess());
        int anotherOwnerUserId = anotherOwnerUser.getData().getUserId();

        // another owner creates a store
        Response<Integer> anotherStoreIdResponse = testHelper.openStore2(anotherOwnerUserId);
        assertTrue(anotherStoreIdResponse.isSuccess());
        int anotherStoreId = anotherStoreIdResponse.getData();

        // owner tries to close another store
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(anotherStoreId, OwnerUserId);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: Requester ID: " + OwnerUserId + " is not a Store Founder of store: " + anotherStoreId, closeStoreResponse.getMessage());

        // verify that the other store is still open
        assertTrue(systemService.isStoreOpen(anotherStoreId));
    }

    @Test
    void testStoreOwnerClosingStore_StoreIdNotFound_Failure() {
        // owner tries to close a store that does not exist
        int nonExistentStoreId = 9999; // assuming this store ID does not exist
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(nonExistentStoreId, OwnerUserId);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: Store not found", closeStoreResponse.getMessage());
    }

    @Test
    void testStoreOwnerClosingStore_OwnerIdNotFound_Failure() {
        // owner tries to close a store with a non-existent owner ID
        int nonExistentOwnerId = 9999; // assuming this user ID does not exist
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, nonExistentOwnerId);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: User not found", closeStoreResponse.getMessage());
    }

    @Test
    void testStoreOwnerClosingStore_StoreAlreadyClosed_Failure() {
        // owner closes the store
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertTrue(closeStoreResponse.isSuccess());
        assertEquals("Store closed successfully", closeStoreResponse.getMessage());

        // verify that the store is closed
        assertFalse(systemService.isStoreOpen(storeId));

        // owner tries to close the store again
        Response<String> closeStoreResponse2 = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertFalse(closeStoreResponse2.isSuccess());
        assertEquals("Error during closing store: Store: " + storeId + " is already closed", closeStoreResponse2.getMessage());
    }

    @Test
    void testStoreOwnerClosingStore_StoreIdInvalid_Failure() {
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(-1, OwnerUserId);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: Store not found", closeStoreResponse.getMessage());
    }

    @Test
    void testStoreOwnerClosingStore_OwnerIdInvalid_Failure() {
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, -1);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: User not found", closeStoreResponse.getMessage());
    }

    @Test
    void testStoreOwnerClosingStore_InsufficientPermissionsStoreClosure_Failure() {
        // another store owner
        Response<UserDTO> anotherOwnerUser = testHelper.register_and_login2();
        assertTrue(anotherOwnerUser.isSuccess());
        int anotherOwnerUserId = anotherOwnerUser.getData().getUserId();

        // owner tries to close another store without permission
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, anotherOwnerUserId);
        assertFalse(closeStoreResponse.isSuccess());
        assertEquals("Error during closing store: Requester ID: " + anotherOwnerUserId + " is not a Store Founder of store: " + storeId, closeStoreResponse.getMessage());

        // verify that the other store is still open
        assertTrue(systemService.isStoreOpen(storeId));

    }



}