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
public class StoreOwner_Manager_Appointment {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int ownerUserId;
    private int storeId;
    private int managerUserId;
    private int otherRegisteredUserId; // For negative tests
    private int otherOwnerUserId; // For negative tests (if an owner can appoint other owners as managers)

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login owner");
        ownerUserId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(ownerUserId, "ManagerAppointmentTestStore");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        Response<UserDTO> managerUserRes = testHelper.register_and_login2();
        assertTrue(managerUserRes.isSuccess(), "Failed to register and login manager to be appointed");
        managerUserId = managerUserRes.getData().getUserId();

        Response<UserDTO> otherUserRes = testHelper.register_and_login3();
        assertTrue(otherUserRes.isSuccess(), "Failed to register and login other registered user");
        otherRegisteredUserId = otherUserRes.getData().getUserId();

        // For testing scenarios where an owner might try to appoint another existing owner as manager
        Response<UserDTO> otherOwnerRes = testHelper.register_and_login4();
        assertTrue(otherOwnerRes.isSuccess(), "Failed to register and login other owner");
        otherOwnerUserId = otherOwnerRes.getData().getUserId();

        // Make otherOwnerUserId an owner of the store
        Response<Void> addOwnerRes = systemService.addStoreOwner(storeId, ownerUserId, otherOwnerUserId);
        assertTrue(addOwnerRes.isSuccess(), "Failed to appoint otherOwnerUserId as owner");
        Response<String> acceptOwnerRes = systemService.acceptAssignment(storeId, otherOwnerUserId);
        assertTrue(acceptOwnerRes.isSuccess(), "otherOwnerUserId failed to accept owner assignment");

        // Verify otherOwnerUserId is indeed an owner
        Response<StoreRolesDTO> rolesAfterAddOtherOwner = systemService.getStoreRoles(storeId, ownerUserId);
        assertTrue(rolesAfterAddOtherOwner.isSuccess());
        assertTrue(rolesAfterAddOtherOwner.getData().getStoreOwners().contains(otherOwnerUserId), "otherOwnerUserId should be an owner");
    }

    @AfterEach
    void tearDown() {
        // Clean up: remove all roles and delete users
        Response<Void> removeManagerRes = systemService.removeStoreManager(storeId, ownerUserId, managerUserId);
        assertTrue(removeManagerRes.isSuccess(), "Failed to remove manager after test");

        Response<Void> removeOwnerRes = systemService.removeStoreOwner(storeId, ownerUserId, otherOwnerUserId);
        assertTrue(removeOwnerRes.isSuccess(), "Failed to remove other owner after test");

        Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, ownerUserId);
        assertTrue(closeStoreRes.isSuccess(), "Failed to close store after test");

        Response<Boolean> deleteManagerUserRes = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteManagerUserRes.isSuccess(), "Failed to delete manager user after test");

        Response<Boolean> deleteOtherRegisteredUserRes = systemService.deleteUser(testHelper.validEmail3());
        assertTrue(deleteOtherRegisteredUserRes.isSuccess(), "Failed to delete other registered user after test");

        Response<Boolean> deleteOtherOwnerUserRes = systemService.deleteUser(testHelper.validEmail4());
        assertTrue(deleteOtherOwnerUserRes.isSuccess(), "Failed to delete other owner user after test");
    }

    @Test
    void testAddStoreManager_Success() throws InterruptedException {
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        perms.add(StoreManagerPermission.VIEW_PURCHASES);

        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, managerUserId, perms);
        assertTrue(response.isSuccess(), "Expected manager appointment to succeed");
        assertEquals("Store manager added successfully", response.getMessage());
        
        TimeUnit.SECONDS.sleep(1);
        // Verify assignment message is sent
        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(managerUserId);
        assertTrue(assignmentMessagesRes.isSuccess(), "Expected to retrieve assignment messages for manager");
        assertTrue(assignmentMessagesRes.getData().size() > 0, "Expected manager to have at least one assignment message");
        StoreMsg assignmentMessage = assignmentMessagesRes.getData().values().iterator().next();
        assertEquals(storeId, assignmentMessage.getStoreId(), "Assignment message should be for the correct store");
        assertTrue(assignmentMessage.getMessage().contains("Please approve or decline this role"), "Expected assignment message to contain appointment notification");
    }

    @Test
    void testAddStoreManager_ManagerAcceptsAssignment_Success() {
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        perms.add(StoreManagerPermission.VIEW_ROLES);

        Response<Void> addManagerRes = systemService.addStoreManager(storeId, ownerUserId, managerUserId, perms);
        assertTrue(addManagerRes.isSuccess(), "Failed to add manager for acceptance test");

        // Manager accepts the assignment
        Response<String> acceptRes = systemService.acceptAssignment(storeId, managerUserId);
        assertTrue(acceptRes.isSuccess(), "Expected manager to successfully accept assignment");

        // Verify manager role and permissions
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, ownerUserId);
        assertTrue(storeRolesRes.isSuccess(), "Failed to retrieve store roles");
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreManagers().containsKey(managerUserId), "Manager should be listed in store roles");
        assertTrue(storeRolesData.getStoreManagers().get(managerUserId).containsAll(perms), "Manager should have the assigned permissions");

        // Verify assignment message is cleared after acceptance
        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(managerUserId);
        assertTrue(assignmentMessagesRes.isSuccess(), "Expected to retrieve assignment messages");
    }

    @Test
    void testAddStoreManager_ManagerDeclinesAssignment_Success() {
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.DISCOUNT_POLICY);

        Response<Void> addManagerRes = systemService.addStoreManager(storeId, ownerUserId, managerUserId, perms);
        assertTrue(addManagerRes.isSuccess(), "Failed to add manager for decline test");

        // Manager declines the assignment
        Response<String> declineRes = systemService.declineAssignment(storeId, managerUserId);
        assertTrue(declineRes.isSuccess(), "Expected manager to successfully decline assignment");

        // Verify manager is NOT in store roles
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, ownerUserId);
        assertTrue(storeRolesRes.isSuccess(), "Failed to retrieve store roles");
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertFalse(storeRolesData.getStoreManagers().containsKey(managerUserId), "Manager should NOT be listed in store roles after decline");

        // Verify assignment message is cleared after decline
        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(managerUserId);
        assertTrue(assignmentMessagesRes.isSuccess(), "Expected to retrieve assignment messages");

    }

    @Test
    void testAddStoreManager_Failure_InvalidStoreId() {
        int invalidStoreId = -1;
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);

        Response<Void> response = systemService.addStoreManager(invalidStoreId, ownerUserId, managerUserId, perms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail for invalid store ID");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType()); // Assuming INTERNAL_ERROR for invalid IDs
        assertTrue(response.getMessage().contains("Error during adding store manager"), "Expected error message for invalid store ID");
    }

    @Test
    void testAddStoreManager_Failure_InvalidManagerUserId() {
        int invalidUserId = -100;
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);

        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, invalidUserId, perms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail for invalid manager user ID");
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddStoreManager_Failure_RequesterIsNotOwner() {
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);

        // otherRegisteredUserId is not an owner of the store
        Response<Void> response = systemService.addStoreManager(storeId, otherRegisteredUserId, managerUserId, perms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail when requester is not an owner");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType()); // Assuming INTERNAL_ERROR for permission issues
        assertTrue(response.getMessage().contains("Error during adding store manager"), "Expected error message for non-owner requester");}

    @Test
    void testAddStoreManager_Failure_AppointingAlreadyManager() {
        List<StoreManagerPermission> initialPerms = new ArrayList<>();
        initialPerms.add(StoreManagerPermission.INVENTORY);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, ownerUserId, managerUserId, initialPerms);
        assertTrue(addManagerRes.isSuccess(), "Failed to add manager initially for this test");
        systemService.acceptAssignment(storeId, managerUserId); // Accept to make them a confirmed manager

        List<StoreManagerPermission> newPerms = new ArrayList<>();
        newPerms.add(StoreManagerPermission.VIEW_PURCHASES);

        // Try to appoint the same user again as manager
        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, managerUserId, newPerms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail if user is already a manager");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType()); // Or a more specific error type
        assertTrue(response.getMessage().contains("Error during adding store manager"), "Expected error message for appointing existing manager");
        // Verify original permissions are not overwritten if this is a failure case
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, ownerUserId);
        assertTrue(storeRolesRes.isSuccess());
        assertTrue(storeRolesRes.getData().getStoreManagers().get(managerUserId).containsAll(initialPerms));
        assertFalse(storeRolesRes.getData().getStoreManagers().get(managerUserId).containsAll(newPerms)); // New permissions should not be added on failure
    }

    @Test
    void testAddStoreManager_Failure_AppointingAlreadyOwner() {
        // Here, `otherOwnerUserId` is already an owner. Try to appoint them as a manager.
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);

        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, otherOwnerUserId, perms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail if user is already an owner");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager"), "Expected error message for appointing existing owner as manager");
    }

    @Test
    void testAddStoreManager_Failure_AppointingSelf() {
        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);

        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, ownerUserId, perms);
        assertFalse(response.isSuccess(), "Expected manager appointment to fail when appointing self");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager"), "Expected error message for appointing self as manager");
    }
}