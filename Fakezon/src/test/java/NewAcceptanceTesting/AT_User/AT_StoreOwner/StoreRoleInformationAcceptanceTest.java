package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import NewAcceptanceTesting.TestHelper;

/**
 * Acceptance tests for Use-case 4.11: Request Role Information in Store
 * and Use-case 4.11: Request administrators' permissions
 */
public class StoreRoleInformationAcceptanceTest {

    private StoreService storeService;
    private IStoreRepository storeRepository;
    private Store store;
    private int storeId = 1;
    private int storeOwnerId = 10;
    private int regularUserId = 20;
    private int managerId = 30;
    private ApplicationEventPublisher publisher;
    private List<StoreManagerPermission> managerPermissions;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        storeRepository = mock(IStoreRepository.class);
        store = new Store("Test Store", storeOwnerId, publisher);
        storeService = new StoreService(storeRepository, publisher);
        
        // Setup manager with permissions
        managerPermissions = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        
        // Mock the store repository to return our test store
        when(storeRepository.findById(storeId)).thenReturn(store);
    }

    /**
     * Test Name: Successful Request for Role Information
     * 
     * Setup & Parameters:
     * 1. Valid session token of a store owner
     * 2. Valid store ID that the store owner owns
     * 
     * Expected Results:
     * 1. The system validates that the user associated with the SessionToken is a store owner.
     * 2. The store owner requests role information for their store.
     * 3. The system retrieves a list of assigned roles and their respective permissions.
     * 4. The system displays the role information to the store owner, including the roles and permissions for each.
     * 5. The store owner can see the assigned roles and their associated permissions.
     */
    @Test
    void requestRoleInformation_StoreOwner_Success() {
        // Add a manager to the store
        store.addStoreManager(storeOwnerId, managerId, managerPermissions);
        store.acceptAssignment(managerId);
        
        // Request role information
        StoreRolesDTO rolesDTO = storeService.getStoreRoles(storeId, storeOwnerId);
        
        // Verify the results
        assertNotNull(rolesDTO);
        assertEquals(storeId, rolesDTO.getStoreId());
        assertEquals("Test Store", rolesDTO.getStoreName());
        assertEquals(storeOwnerId, rolesDTO.getFounderId());
        
        // Verify store owners list contains the owner
        assertTrue(rolesDTO.getStoreOwners().contains(storeOwnerId));
        
        // Verify store managers map contains the manager with correct permissions
        assertTrue(rolesDTO.getStoreManagers().containsKey(managerId));
        assertEquals(managerPermissions, rolesDTO.getStoreManagers().get(managerId));
        
        // Verify the repository was called
        verify(storeRepository).findById(storeId);
    }

    /**
     * Test Name: No Assigned Roles in the Store
     * 
     * Setup & Parameters:
     * 1. Valid session token of a store owner
     * 2. Valid store ID that the store owner owns
     * 
     * Expected Results:
     * 1. The system validates that the user associated with the SessionToken is a store owner.
     * 2. The store owner requests role information for their store.
     * 3. The system checks if any roles are assigned to the store.
     * 4. The system detects that no roles are assigned in the store.
     * 5. The system notifies the store owner that there are no roles assigned in the store.
     */
    @Test
    void requestRoleInformation_NoAssignedRoles_Success() {
        // Create a new store with just the owner (no managers)
        Store emptyStore = new Store("Empty Store", storeOwnerId, publisher);
        when(storeRepository.findById(2)).thenReturn(emptyStore);
        
        // Request role information
        StoreRolesDTO rolesDTO = storeService.getStoreRoles(2, storeOwnerId);
        
        // Verify the results
        assertNotNull(rolesDTO);
        assertEquals(2, rolesDTO.getStoreId());
        assertEquals("Empty Store", rolesDTO.getStoreName());
        assertEquals(storeOwnerId, rolesDTO.getFounderId());
        
        // Verify store owners list contains only the owner
        assertTrue(rolesDTO.getStoreOwners().contains(storeOwnerId));
        assertEquals(1, rolesDTO.getStoreOwners().size());
        
        // Verify store managers map is empty
        assertTrue(rolesDTO.getStoreManagers().isEmpty());
        
        // Verify the repository was called
        verify(storeRepository).findById(2);
    }

    /**
     * Test Name: Unauthorized Access
     * 
     * Expected Results:
     * If a user without store owner permissions attempts to request role information, 
     * the system denies access and displays an error message
     */
    @Test
    void requestRoleInformation_UnauthorizedAccess_ThrowsError() {
        // Attempt to request role information as a non-owner
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.getStoreRoles(storeId, regularUserId));
        
        // Verify the error message
        assertTrue(ex.getMessage().contains("insufficient permissions"));
        
        // Verify the repository was called
        verify(storeRepository).findById(storeId);
    }

    /**
     * Test Name: Successful Request for Administrators' Permissions
     * 
     * Setup & Parameters:
     * 1. Valid session token of a store owner
     * 2. Valid store ID that the store owner owns
     * 
     * Expected Results:
     * 1. The system validates that the user associated with the SessionToken is a store owner.
     * 2. The store owner requests administrators' permissions for their store.
     * 3. The system retrieves the list of assigned roles and permissions within the store, specifically administrators' roles.
     * 4. The system displays the administrators' roles and permissions to the store owner.
     */
    @Test
    void requestAdministratorsPermissions_StoreOwner_Success() {
        // Add another owner and a manager with admin permissions
        int anotherOwnerId = 40;
        int adminManagerId = 50;
        
        // Add another store owner
        store.addStoreOwner(storeOwnerId, anotherOwnerId);
        store.acceptAssignment(anotherOwnerId);
        
        // Add a manager with admin permissions
        List<StoreManagerPermission> adminPermissions = new ArrayList<>(List.of(
            StoreManagerPermission.INVENTORY,
            StoreManagerPermission.VIEW_PURCHASES,
            StoreManagerPermission.VIEW_ROLES
        ));
        store.addStoreManager(storeOwnerId, adminManagerId, adminPermissions);
        store.acceptAssignment(adminManagerId);
        
        // Request role information
        StoreRolesDTO rolesDTO = storeService.getStoreRoles(storeId, storeOwnerId);
        
        // Verify the results
        assertNotNull(rolesDTO);
        
        // Verify store owners list contains both owners
        assertTrue(rolesDTO.getStoreOwners().contains(storeOwnerId));
        assertTrue(rolesDTO.getStoreOwners().contains(anotherOwnerId));
        assertEquals(2, rolesDTO.getStoreOwners().size());
        
        // Verify store managers map contains the admin manager with correct permissions
        assertTrue(rolesDTO.getStoreManagers().containsKey(adminManagerId));
        assertEquals(adminPermissions, rolesDTO.getStoreManagers().get(adminManagerId));
        
        // Verify the repository was called
        verify(storeRepository).findById(storeId);
    }

    /**
     * Test Name: Unauthorized Access for Administrators' Permissions
     * 
     * Expected Results:
     * If a user without store owner permissions attempts to request administrators' permissions,
     * the system denies access and displays an error message
     */
    @Test
    void requestAdministratorsPermissions_UnauthorizedAccess_ThrowsError() {
        // Attempt to request administrators' permissions as a non-owner
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.getStoreRoles(storeId, regularUserId));
        
        // Verify the error message
        assertTrue(ex.getMessage().contains("insufficient permissions"));
        
        // Verify the repository was called
        verify(storeRepository).findById(storeId);
    }
}
