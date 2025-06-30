package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class StoreOwner_change_permissionsTest {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int OwnerUserId;
    private int storeId;
    private int ManagerUserId;
    private int OtherRegisteredUserId; // For testing scenarios where a non-owner/non-manager tries to act
    private int SecondOwnerUserId;
    @BeforeEach
    void setUp() {

        testHelper = new TestHelper(systemService);

        // Setup Owner
        Response<UserDTO> OwnerUser = testHelper.register_and_login();
        assertTrue(OwnerUser.isSuccess());
        OwnerUserId = OwnerUser.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(OwnerUserId, "PermissionsTestStore");
        assertTrue(storeRes.isSuccess());
        storeId = storeRes.getData();

        // Setup Manager
        Response<UserDTO> ManagerUser = testHelper.register_and_login2();
        assertTrue(ManagerUser.isSuccess());
        ManagerUserId = ManagerUser.getData().getUserId();

        // Add Manager with some initial permissions: INVENTORY, VIEW_PURCHASES
        List<StoreManagerPermission> initialPerms = new ArrayList<>();
        initialPerms.add(StoreManagerPermission.INVENTORY);
        initialPerms.add(StoreManagerPermission.VIEW_PURCHASES);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, OwnerUserId, ManagerUserId, initialPerms);
        assertTrue(addManagerRes.isSuccess());

        // Manager accepts the assignment
        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());

        // Setup another registered user (not owner/manager)
        Response<UserDTO> OtherRegisteredUser = testHelper.register_and_login3();
        assertTrue(OtherRegisteredUser.isSuccess());
        OtherRegisteredUserId = OtherRegisteredUser.getData().getUserId();

        Response<UserDTO> SecondOwnerUser = testHelper.register_and_login4(); // Using register_and_login4
        assertTrue(SecondOwnerUser.isSuccess());
        SecondOwnerUserId = SecondOwnerUser.getData().getUserId();

        // Appoint SecondOwnerUserId as an owner
        Response<Void> addSecondOwnerRes = systemService.addStoreOwner(storeId, OwnerUserId, SecondOwnerUserId);
        assertTrue(addSecondOwnerRes.isSuccess());
        // Second owner accepts the assignment
        Response<String> acceptSecondOwnerRes = systemService.acceptAssignment(storeId, SecondOwnerUserId);
        assertTrue(acceptSecondOwnerRes.isSuccess());
    }

    @AfterEach
    void tearDown() {
        // Remove the store and users created for this test
        Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, OwnerUserId);
        assertTrue(closeStoreRes.isSuccess());

        Response<Boolean> deleteManagerRes = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteManagerRes.isSuccess());

        Response<Boolean> deleteOtherUserRes = systemService.deleteUser(testHelper.validEmail3());
        assertTrue(deleteOtherUserRes.isSuccess());

        Response<Boolean> deleteSecondOwnerRes = systemService.deleteUser(testHelper.validEmail4());
        assertTrue(deleteSecondOwnerRes.isSuccess());

        Response<Boolean> deleteOwnerRes = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteOwnerRes.isSuccess());
    }

    @Test
    void testAddStoreManagerPermissions_Success_SinglePermission() {
        // Arrange
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.PURCHASE_POLICY);

        // Act
        // OwnerUserId (requester) adds permissions to ManagerUserId (target)
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, permsToAdd);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Permissions added successfully", response.getMessage());

        // Verify the manager now has the new permission
        Response<StoreRolesDTO> rolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesRes.isSuccess());
        // Corrected: Use getStoreManagers()
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).containsAll(permsToAdd));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES));
        assertEquals(3, rolesRes.getData().getStoreManagers().get(ManagerUserId).size());
    }

    @Test
    void testAddStoreManagerPermissions_Success_MultiplePermissions() {
        // Arrange
        List<StoreManagerPermission> permsToAdd = Arrays.asList(
            StoreManagerPermission.DISCOUNT_POLICY,
            StoreManagerPermission.REQUESTS_REPLY
        );

        // Act
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, permsToAdd);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Permissions added successfully", response.getMessage());

        Response<StoreRolesDTO> rolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesRes.isSuccess());
        // Corrected: Use getStoreManagers()
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).containsAll(permsToAdd));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES));
        assertEquals(4, rolesRes.getData().getStoreManagers().get(ManagerUserId).size());
    }

    @Test
    void testAddStoreManagerPermissions_Failure_NotAnOwner() {
        // Arrange
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.PURCHASE_POLICY);

        // Act
        // OtherRegisteredUserId tries to add permissions (requester is not an owner)
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, ManagerUserId, OtherRegisteredUserId, permsToAdd);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions"));
    }

    @Test
    void testAddStoreManagerPermissions_Failure_RequesterNotOwnerOfThisStore() {
        // Arrange
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Create another store and owner
        Response<UserDTO> anotherOwner = testHelper.register_and_login5();
        assertTrue(anotherOwner.isSuccess());
        int anotherOwnerId = anotherOwner.getData().getUserId();
        Response<Integer> anotherStoreRes = systemService.addStore(anotherOwnerId, "AnotherStoreForPerms");
        assertTrue(anotherStoreRes.isSuccess());
        int anotherStoreId = anotherStoreRes.getData();

        // OwnerUserId (from initial setup) tries to add permissions in `anotherStoreId`
        Response<Void> response = systemService.addStoreManagerPermissions(anotherStoreId, ManagerUserId, OwnerUserId, permsToAdd);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions"));
    }

    @Test
    void testAddStoreManagerPermissions_Failure_TargetNotAManager() {
        // Arrange
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.REQUESTS_REPLY);

        // Act
        // Owner tries to add permissions to OtherRegisteredUserId (who is not a manager in this store)
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, OtherRegisteredUserId, OwnerUserId, permsToAdd);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions"));
    }

    @Test
    void testAddStoreManagerPermissions_Failure_InvalidStoreId() {
        // Arrange
        int invalidStoreId = -1;
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.addStoreManagerPermissions(invalidStoreId, ManagerUserId, OwnerUserId, permsToAdd);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions"));
    }

    @Test
    void testAddStoreManagerPermissions_Failure_InvalidManagerId() {
        // Arrange
        int invalidManagerId = -999;
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, invalidManagerId, OwnerUserId, permsToAdd);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions"));
    }

    @Test
    void testAddStoreManagerPermissions_Success_PermissionsAlreadyExist() {
        // Arrange - Manager already has INVENTORY from setUp
        List<StoreManagerPermission> permsToAdd = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, permsToAdd);

        // Assert
        assertTrue(response.isSuccess()); // Adding an existing permission should typically succeed as a no-op.
        assertEquals("Permissions added successfully", response.getMessage());

        // Verify state remains correct (no duplicate permissions, original count)
        Response<StoreRolesDTO> rolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesRes.isSuccess());
        // Corrected: Use getStoreManagers()
        List<StoreManagerPermission> currentPerms = new ArrayList<>(rolesRes.getData().getStoreManagers().get(ManagerUserId));
        assertEquals(2, currentPerms.size()); // Should still be 2 (INVENTORY, VIEW_PURCHASES)
        assertTrue(currentPerms.contains(StoreManagerPermission.INVENTORY));
        assertTrue(currentPerms.contains(StoreManagerPermission.VIEW_PURCHASES));
    }



    @Test
    void testRemoveStoreManagerPermissions_Success_SinglePermission() {
        // Arrange - Manager has INVENTORY and VIEW_PURCHASES from setUp
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, ManagerUserId, permsToRemove);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Permissions removed successfully", response.getMessage());

        // Verify the manager no longer has the removed permission
        Response<StoreRolesDTO> rolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesRes.isSuccess());
        // Corrected: Use getStoreManagers()
        assertFalse(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES)); // Other initial perm should remain
        assertEquals(1, rolesRes.getData().getStoreManagers().get(ManagerUserId).size());
    }

    @Test
    void testRemoveStoreManagerPermissions_Success_MultiplePermissions() {
        // Arrange - Manager has INVENTORY and VIEW_PURCHASES from setUp
        // Add more permissions to Manager to test removal of multiple
        systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, Arrays.asList(StoreManagerPermission.PURCHASE_POLICY, StoreManagerPermission.DISCOUNT_POLICY));

        List<StoreManagerPermission> permsToRemove = Arrays.asList(
            StoreManagerPermission.INVENTORY,
            StoreManagerPermission.DISCOUNT_POLICY
        );

        // Act
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, ManagerUserId, permsToRemove);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Permissions removed successfully", response.getMessage());

        Response<StoreRolesDTO> rolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(rolesRes.isSuccess());
        // Corrected: Use getStoreManagers()
        assertFalse(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertFalse(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.DISCOUNT_POLICY));
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES)); // Remaining initial perm
        assertTrue(rolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.PURCHASE_POLICY)); // Remaining added perm
        assertEquals(2, rolesRes.getData().getStoreManagers().get(ManagerUserId).size());
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_NotAnOwner() {
        // Arrange
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        // OtherRegisteredUserId tries to remove permissions (requester is not an owner)
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OtherRegisteredUserId, ManagerUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_RequesterNotOwnerOfThisStore() {
        // Arrange
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Create another store and owner
        Response<UserDTO> anotherOwner = testHelper.register_and_login5();
        assertTrue(anotherOwner.isSuccess());
        int anotherOwnerId = anotherOwner.getData().getUserId();
        Response<Integer> anotherStoreRes = systemService.addStore(anotherOwnerId, "AnotherStoreForPermsToRemove");
        assertTrue(anotherStoreRes.isSuccess());
        int anotherStoreId = anotherStoreRes.getData();

        // OwnerUserId (from initial setup) tries to remove permissions in `anotherStoreId`
        Response<Void> response = systemService.removeStoreManagerPermissions(anotherStoreId, OwnerUserId, ManagerUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_TargetNotAManager() {
        // Arrange
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        // Owner tries to remove permissions from OtherRegisteredUserId (who is not a manager in this store)
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, OtherRegisteredUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_InvalidStoreId() {
        // Arrange
        int invalidStoreId = -1;
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.removeStoreManagerPermissions(invalidStoreId, OwnerUserId, ManagerUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_InvalidManagerId() {
        // Arrange
        int invalidManagerId = -999;
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, invalidManagerId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_PermissionDoesNotExist() {
        // Arrange - Manager has INVENTORY and VIEW_PURCHASES from setUp
        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.REQUESTS_REPLY); // Manager does not have this permission

        // Act
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, ManagerUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
    }

    @Test
    void testRemoveStoreManagerPermissions_Failure_RemovingPermissionsFromSelf() {

        List<StoreManagerPermission> ownerAsManagerPerms = Arrays.asList(StoreManagerPermission.INVENTORY, StoreManagerPermission.VIEW_ROLES);
        systemService.addStoreManager(storeId, OwnerUserId, OwnerUserId, ownerAsManagerPerms);

        List<StoreManagerPermission> permsToRemove = Arrays.asList(StoreManagerPermission.INVENTORY);

        // Act (OwnerUserId tries to remove permissions from itself assuming it's also a manager)
        Response<Void> response = systemService.removeStoreManagerPermissions(storeId, OwnerUserId, OwnerUserId, permsToRemove);

        // Assert
        assertFalse(response.isSuccess()); // Expecting failure because an owner cannot remove their own inherent permissions.
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during removing store manager permissions"));
        // The specific error message would depend on your domain logic, e.g.,
        // "Cannot remove permissions from a store owner", "Operation not applicable to owner", etc.
    }

    @Test
    void testAddStoreManagerPermissions_Concurrency_OnlyOneSucceedsForNewPermission() throws InterruptedException, ExecutionException {
        // Arrange
        // Ensure ManagerUserId does NOT initially have the specific permission we are trying to add
        List<StoreManagerPermission> permsToAddConcurrently = Arrays.asList(StoreManagerPermission.DISCOUNT_POLICY);
        
        // Sanity check: Manager should not have this permission yet
        Response<StoreRolesDTO> initialRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(initialRolesRes.isSuccess());
        assertFalse(initialRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.DISCOUNT_POLICY));

        // Create a thread pool for concurrent execution
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Define tasks for each owner to add the same permission
        Callable<Response<Void>> task1 = () -> systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, permsToAddConcurrently);
        Callable<Response<Void>> task2 = () -> systemService.addStoreManagerPermissions(storeId, ManagerUserId, SecondOwnerUserId, permsToAddConcurrently);

        // Submit tasks
        Future<Response<Void>> future1 = executor.submit(task1);
        Future<Response<Void>> future2 = executor.submit(task2);

        // Get results
        Response<Void> result1 = future1.get();
        Response<Void> result2 = future2.get();

        // Shutdown the executor
        executor.shutdown();

        // Assert that exactly one of the operations succeeded
        assertTrue(result1.isSuccess() ^ result2.isSuccess());

        // Verify the manager now has the permission
        Response<StoreRolesDTO> finalRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(finalRolesRes.isSuccess());
        assertTrue(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.DISCOUNT_POLICY));

        // Ensure no unexpected duplicates or issues, current count should be 3
        assertEquals(3, finalRolesRes.getData().getStoreManagers().get(ManagerUserId).size());
        assertTrue(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES));
    }

    @Test
    void testRemoveStoreManagerPermissions_Concurrency_OnlyOneSucceedsIfPermissionExists() throws InterruptedException, ExecutionException {
        // Arrange
        // Add a permission that both owners will try to remove
        List<StoreManagerPermission> permsToRemoveConcurrently = Arrays.asList(StoreManagerPermission.REQUESTS_REPLY);
        systemService.addStoreManagerPermissions(storeId, ManagerUserId, OwnerUserId, permsToRemoveConcurrently);
        systemService.acceptAssignment(storeId, ManagerUserId); // Manager accepts this new permission

        // Sanity check: Manager should now have this permission
        Response<StoreRolesDTO> initialRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(initialRolesRes.isSuccess());
        assertTrue(initialRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.REQUESTS_REPLY));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Response<Void>> task1 = () -> systemService.removeStoreManagerPermissions(storeId, OwnerUserId, ManagerUserId, permsToRemoveConcurrently);
        Callable<Response<Void>> task2 = () -> systemService.removeStoreManagerPermissions(storeId, SecondOwnerUserId, ManagerUserId, permsToRemoveConcurrently);

        Future<Response<Void>> future1 = executor.submit(task1);
        Future<Response<Void>> future2 = executor.submit(task2);

        Response<Void> result1 = future1.get();
        Response<Void> result2 = future2.get();

        executor.shutdown();

        assertTrue(result1.isSuccess() ^ result2.isSuccess()); // At least one should indicate success for the removal
        
        // Verify the manager no longer has the permission
        Response<StoreRolesDTO> finalRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(finalRolesRes.isSuccess());
        assertFalse(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.REQUESTS_REPLY));

        // Check the final count of permissions
        assertEquals(2, finalRolesRes.getData().getStoreManagers().get(ManagerUserId).size()); // Original 2 - 1 removed = 1
        assertTrue(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(finalRolesRes.getData().getStoreManagers().get(ManagerUserId).contains(StoreManagerPermission.VIEW_PURCHASES));
    }
}