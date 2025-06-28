package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

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

import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_RemoveOwnership {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int OwnerUserId; // User1 (original owner)
    private int storeId;
    private int ManagerUserId; // User2 (appointed by User1 as manager, then owner)
    private int OwnerUserId2; // User3 (appointed by User2 as manager, then owner)
    private int otherRegisteredUserId; // A registered user not involved in ownership hierarchy

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        // User1 creates a store
        Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login OwnerUserId");
        OwnerUserId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(OwnerUserId, "TestStoreForOwnershipRemoval");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        // User1 appoints User2 as manager, then owner
        Response<UserDTO> managerUserRes = testHelper.register_and_login2();
        assertTrue(managerUserRes.isSuccess(), "Failed to register and login ManagerUserId (User2)");
        ManagerUserId = managerUserRes.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        perms.add(StoreManagerPermission.VIEW_ROLES);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, OwnerUserId, ManagerUserId, perms);
        assertTrue(addManagerRes.isSuccess(), "Failed to add ManagerUserId as manager");

        Response<String> acceptManagerRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptManagerRes.isSuccess(), "ManagerUserId failed to accept manager assignment");

        Response<Void> addOwnerRes1 = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(addOwnerRes1.isSuccess(), "Failed to add ManagerUserId as owner by OwnerUserId");

        Response<String> acceptOwnerRes1 = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptOwnerRes1.isSuccess(), "ManagerUserId failed to accept owner assignment");

        // Verify ManagerUserId (User2) is now an owner
        Response<StoreRolesDTO> rolesAfterOwner1 = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesAfterOwner1.isSuccess());
        assertTrue(rolesAfterOwner1.getData().getStoreOwners().contains(ManagerUserId), "ManagerUserId should be a store owner after acceptance");


        // User2 appoints User3 as manager, then owner
        Response<UserDTO> ownerUser2Res = testHelper.register_and_login3();
        assertTrue(ownerUser2Res.isSuccess(), "Failed to register and login OwnerUserId2 (User3)");
        OwnerUserId2 = ownerUser2Res.getData().getUserId();

        Response<Void> addManagerRes2 = systemService.addStoreManager(storeId, ManagerUserId, OwnerUserId2, perms);
        assertTrue(addManagerRes2.isSuccess(), "Failed to add OwnerUserId2 as manager by ManagerUserId");

        Response<String> acceptManagerRes2 = systemService.acceptAssignment(storeId, OwnerUserId2);
        assertTrue(acceptManagerRes2.isSuccess(), "OwnerUserId2 failed to accept manager assignment");

        Response<Void> addOwnerRes2 = systemService.addStoreOwner(storeId, ManagerUserId, OwnerUserId2);
        assertTrue(addOwnerRes2.isSuccess(), "Failed to add OwnerUserId2 as owner by ManagerUserId");

        Response<String> acceptOwnerRes2 = systemService.acceptAssignment(storeId, OwnerUserId2);
        assertTrue(acceptOwnerRes2.isSuccess(), "OwnerUserId2 failed to accept owner assignment");

        // Verify OwnerUserId2 (User3) is now an owner
        Response<StoreRolesDTO> rolesAfterOwner2 = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesAfterOwner2.isSuccess());
        assertTrue(rolesAfterOwner2.getData().getStoreOwners().contains(OwnerUserId2), "OwnerUserId2 should be a store owner after acceptance");
        
        // Register another user for negative tests
        Response<UserDTO> otherUserRes = testHelper.register_and_login4();
        assertTrue(otherUserRes.isSuccess());
        otherRegisteredUserId = otherUserRes.getData().getUserId();
    }

    @AfterEach
    void tearDown() {
        // Remove OwnerUserId2 (User3) as owner (ignore if already removed)
        Response<Void> removeOwner2Res = systemService.removeStoreOwner(storeId, ManagerUserId, OwnerUserId2);
        if (!removeOwner2Res.isSuccess()) {
            String msg = removeOwner2Res.getMessage();
            assertTrue(
                msg.contains("not found") ||
                msg.contains("is not a store owner") ||
                msg.contains("is not a valid store owner"),
                "Failed to remove OwnerUserId2: " + msg
            );
        }

        // Remove ManagerUserId (User2) as owner (ignore if already removed)
        Response<Void> removeManagerRes = systemService.removeStoreOwner(storeId, OwnerUserId, ManagerUserId);
        if (!removeManagerRes.isSuccess()) {
            String msg = removeManagerRes.getMessage();
            assertTrue(
                msg.contains("not found") ||
                msg.contains("is not a store owner") ||
                msg.contains("is not a valid store owner"),
                "Failed to remove ManagerUserId: " + msg
            );
        }

        // Remove OwnerUserId (User1) as owner (ignore if already removed or is founder)
        Response<Void> removeOwner1Res = systemService.removeStoreOwner(storeId, OwnerUserId, OwnerUserId);
        if (!removeOwner1Res.isSuccess()) {
            String msg = removeOwner1Res.getMessage();
            assertTrue(
                msg.contains("not found") ||
                msg.contains("is not a store owner") ||
                msg.contains("is not a valid store owner") ||
                msg.contains("Can not remove Store Founder") ||
                msg.contains("User not found"),
                "Failed to remove OwnerUserId: " + msg
            );
        }

        // Close the store (ignore if already closed or not found)
        Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, OwnerUserId);
        if (!closeStoreRes.isSuccess()) {
            String msg = closeStoreRes.getMessage();
            assertTrue(
                msg.contains("already closed") ||
                msg.contains("Store not found") ||
                msg.contains("User not found"),
                "Failed to close store: " + msg
            );
        }

        // Remove test users (ignore if already deleted or error during deleting)
        Response<Boolean> deleteResponse1 = systemService.deleteUser(testHelper.validEmail());
        if (!deleteResponse1.isSuccess()) {
            String msg = deleteResponse1.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Failed to delete OwnerUserId: " + msg
            );
        }

        Response<Boolean> deleteResponse2 = systemService.deleteUser(testHelper.validEmail2());
        if (!deleteResponse2.isSuccess()) {
            String msg = deleteResponse2.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Failed to delete ManagerUserId: " + msg
            );
        }

        Response<Boolean> deleteResponse3 = systemService.deleteUser(testHelper.validEmail3());
        if (!deleteResponse3.isSuccess()) {
            String msg = deleteResponse3.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Failed to delete OwnerUserId2: " + msg
            );
        }

        Response<Boolean> deleteOtherResponse = systemService.deleteUser(testHelper.validEmail4());
        if (!deleteOtherResponse.isSuccess()) {
            String msg = deleteOtherResponse.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Failed to delete other registered user: " + msg
            );
        }
        //remove store from system
        Response<Void> removeStoreRes = systemService.removeStore(storeId, OwnerUserId);
        if (!removeStoreRes.isSuccess()) {
            String msg = removeStoreRes.getMessage();
            assertTrue(
                msg.equals("Store not found") || msg.equals("Error during removing store"),
                "Failed to remove store: " + msg
            );
        }
    }

    @Test
    void testRemoveStoreOwner_Success_AppointingOwnerRemovesOwnership() {
        // ManagerUserId (User2) removes ownership from OwnerUserId2 (User3), whom User2 appointed
        Response<Void> response = systemService.removeStoreOwner(storeId, ManagerUserId, OwnerUserId2);

        // Assert
        assertTrue(response.isSuccess(), "Expected ownership removal to succeed by appointing owner");
        assertEquals("Store owner removed successfully", response.getMessage());

        // Verify OwnerUserId2 is no longer an owner
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertFalse(storeRolesData.getStoreOwners().contains(OwnerUserId2), "OwnerUserId2 should no longer be a store owner");

        // Verify that OwnerUserId (User1) and ManagerUserId (User2) are still owners
        assertTrue(storeRolesData.getStoreOwners().contains(OwnerUserId), "OwnerUserId (User1) should still be an owner");
        assertTrue(storeRolesData.getStoreOwners().contains(ManagerUserId), "ManagerUserId (User2) should still be an owner");
    }

    @Test
    void testRemoveStoreOwner_Failure_NonAppointingOwnerRemovesOwnership() {
        // OwnerUserId (User1) tries to remove ownership from OwnerUserId2 (User3), who was appointed by ManagerUserId (User2)
        Response<Void> response = systemService.removeStoreOwner(storeId, OwnerUserId, OwnerUserId2);

        // Assert
        assertFalse(response.isSuccess(), "Expected ownership removal to fail by non-appointing owner");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType()); // Assuming INTERNAL_ERROR for this type of authorization failure
        assertTrue(response.getMessage().contains("Error during removing store owner"),
                "Expected error message for non-appointing owner attempting to remove ownership");

        // Verify OwnerUserId2 is still an owner
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(OwnerUserId2), "OwnerUserId2 should still be a store owner");
    }

    @Test
    void testRemoveStoreOwner_Failure_RemoverIsNotOwner() {
        // otherRegisteredUserId (not an owner) tries to remove ownership from OwnerUserId2 (User3)
        Response<Void> response = systemService.removeStoreOwner(storeId, otherRegisteredUserId, OwnerUserId2);

        // Assert
        assertFalse(response.isSuccess(), "Expected ownership removal to fail by non-owner");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType()); // Assuming INTERNAL_ERROR if requester is not an owner
        assertTrue(response.getMessage().contains("Error during removing store owner"),
                "Expected error message for non-owner attempting to remove ownership");

        // Verify OwnerUserId2 is still an owner
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(OwnerUserId2), "OwnerUserId2 should still be a store owner");
    }

    @Test
    void testRemoveStoreOwner_Failure_RemovingLastOwner() {
        // Try to remove OwnerUserId (User1), who is the original owner.
        // Assuming there's a rule that at least one owner must remain.
        // This test would be relevant if the system prevents reducing owners to zero,
        // or if it prevents removing the initial owner when other owners exist.
        // Here, we have 3 owners initially: OwnerUserId, ManagerUserId, OwnerUserId2.
        
        // First, remove OwnerUserId2 (User3) by ManagerUserId (User2)
        Response<Void> removeOwner2Res = systemService.removeStoreOwner(storeId, ManagerUserId, OwnerUserId2);
        assertTrue(removeOwner2Res.isSuccess(), "Expected OwnerUserId2 removal to succeed first");

        // Now, try to remove ManagerUserId (User2) by OwnerUserId (User1)
        Response<Void> removeOwner1Res = systemService.removeStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(removeOwner1Res.isSuccess(), "Expected ManagerUserId removal to succeed second");

        // Now, only OwnerUserId (User1) remains. Try to remove OwnerUserId.
        // This should fail, as OwnerUserId is the last remaining owner.
        Response<Void> removeLastOwnerRes = systemService.removeStoreOwner(storeId, OwnerUserId, OwnerUserId);

        // Assert
        assertFalse(removeLastOwnerRes.isSuccess(), "Expected removing the last owner to fail");
        assertEquals(ErrorType.INTERNAL_ERROR, removeLastOwnerRes.getErrorType());
        assertTrue(removeLastOwnerRes.getMessage().contains("Error during removing store owner"),
                "Expected error message when attempting to remove the last store owner");

        // Verify OwnerUserId is still an owner
        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(OwnerUserId), "OwnerUserId should still be an owner (last owner)");
    }

    @Test
    void testRemoveStoreOwner_Failure_InvalidStoreId() {
        int invalidStoreId = -1;
        Response<Void> response = systemService.removeStoreOwner(invalidStoreId, ManagerUserId, OwnerUserId2);

        assertFalse(response.isSuccess(), "Expected removal to fail for invalid store ID");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store owner"),
                "Expected error message for invalid store ID");
    }

    @Test
    void testRemoveStoreOwner_Failure_InvalidOwnerIdToRemove() {
        int invalidOwnerId = -1;
        Response<Void> response = systemService.removeStoreOwner(storeId, ManagerUserId, invalidOwnerId);

        assertFalse(response.isSuccess(), "Expected removal to fail for invalid owner ID to remove");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store owner"),
                "Expected error message for invalid owner ID to remove");
    }
}