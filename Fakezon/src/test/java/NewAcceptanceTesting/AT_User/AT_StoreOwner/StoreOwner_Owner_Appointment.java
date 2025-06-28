package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;

import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")
public class StoreOwner_Owner_Appointment {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int OwnerUserId;
    private int storeId;
    private int ManagerUserId;

    @BeforeEach
    void setUp() {

        testHelper = new TestHelper(systemService);

        Response<UserDTO> OwnerUser = testHelper.register_and_login();
        assertTrue(OwnerUser.isSuccess());
        OwnerUserId = OwnerUser.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(OwnerUserId, "TestStore");
        assertTrue(storeRes.isSuccess());
        storeId = storeRes.getData();

        Response<UserDTO> ManagerUser = testHelper.register_and_login2();
        assertTrue(ManagerUser.isSuccess());
        ManagerUserId = ManagerUser.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, OwnerUserId, ManagerUserId, perms);
        assertTrue(addManagerRes.isSuccess());

        
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }
        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(ManagerUserId);
        assertTrue(assignmentMessagesRes.isSuccess());
        assertTrue(assignmentMessagesRes.getData().size() > 0);
        StoreMsg assignmentMessage = assignmentMessagesRes.getData().values().iterator().next();
        assertEquals(storeId, assignmentMessage.getStoreId(), "Assignment message should be for the correct store");

        
        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());
    }

    @AfterEach
    void tearDown() {
        // Close the store (ignore if already closed or not found)
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, OwnerUserId);
        if (!closeStoreResponse.isSuccess()) {
            String msg = closeStoreResponse.getMessage();
            assertTrue(
                msg.contains("already closed") || msg.contains("Store not found"),
                "Unexpected close store message: " + msg
            );
        }

        // Remove the store (ignore if already removed)
        Response<Void> removeStoreResponse = systemService.removeStore(storeId, OwnerUserId);
        if (!removeStoreResponse.isSuccess()) {
            String msg = removeStoreResponse.getMessage();
            assertTrue(
                msg.contains("Store not found"),
                "Unexpected remove store message: " + msg
            );
        }

        // Delete the main owner user
        Response<Boolean> deleteOwnerResponse = systemService.deleteUser(testHelper.validEmail());
        if (!deleteOwnerResponse.isSuccess()) {
            String msg = deleteOwnerResponse.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Unexpected delete user message: " + msg
            );
        }

        // Delete the manager user
        Response<Boolean> deleteManagerResponse = systemService.deleteUser(testHelper.validEmail2());
        if (!deleteManagerResponse.isSuccess()) {
            String msg = deleteManagerResponse.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Unexpected delete user message: " + msg
            );
        }

        // Delete any additional users created in tests (e.g., ManagerUser3)
        Response<Boolean> deleteManager3Response = systemService.deleteUser(testHelper.validEmail3());
        if (!deleteManager3Response.isSuccess()) {
            String msg = deleteManager3Response.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Unexpected delete user message: " + msg
            );
        }
    }

    @Test
    void testAddStoreOwner_Success() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        // Wait for the assignment message to be created (same as in @BeforeEach)
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }

        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(ManagerUserId);
        assertTrue(assignmentMessagesRes.isSuccess());
        assertTrue(assignmentMessagesRes.getData().size() > 0);
        StoreMsg assignmentMessage = assignmentMessagesRes.getData().values().iterator().next();
        assertEquals(storeId, assignmentMessage.getStoreId(), "Assignment message should be for the correct store");
    }

    @Test
    void testAddStoreOwner_ManagerAccept_Success() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());
        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(ManagerUserId));
    }

    @Test
    void testAddStoreOwner_ManagerDecline_Failure() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> declineRes = systemService.declineAssignment(storeId, ManagerUserId);
        assertTrue(declineRes.isSuccess());
        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertFalse(storeRolesData.getStoreOwners().contains(ManagerUserId));
    }

    @Test
    void testAddStoreOwner_ManagementCircle_Failure() {
        
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));
        
        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());
        
        //third manager assignee by second manager
        Response<UserDTO> ManagerUser2 = testHelper.register_and_login3();
        assertTrue(ManagerUser2.isSuccess());
        int ManagerUserId2 = ManagerUser2.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, ManagerUserId, ManagerUserId2, perms);
        assertTrue(addManagerRes.isSuccess());


        // The new manager accepts the assignment for manager
        acceptRes = systemService.acceptAssignment(storeId, ManagerUserId2);
        assertTrue(acceptRes.isSuccess());

        response = systemService.addStoreOwner(storeId, ManagerUserId, ManagerUserId2);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        // The new manager accepts the assignment for owner
        acceptRes = systemService.acceptAssignment(storeId, ManagerUserId2);
        assertTrue(acceptRes.isSuccess());

        //third manager tries to make second owner manager
        addManagerRes = systemService.addStoreManager(storeId, ManagerUserId2, ManagerUserId, perms);
        assertFalse(addManagerRes.isSuccess());

        //third manager tries to make first owner manager
        addManagerRes = systemService.addStoreManager(storeId, ManagerUserId2, OwnerUserId, perms);
        assertFalse(addManagerRes.isSuccess());

    }


    @Test
    void testAddStoreOwner_FailureInvalidUser() {
        int invalidUserId = -999;

        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, invalidUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        assertEquals("User is not registered", response.getMessage());
    }

    @Test
    void testAddStoreOwner_FailureInvalidStore() {
        int invalidStoreId = -123;

        Response<Void> response = systemService.addStoreOwner(invalidStoreId, OwnerUserId, ManagerUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store owner"));
    }
}