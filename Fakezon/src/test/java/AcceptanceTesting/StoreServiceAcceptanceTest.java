package AcceptanceTesting;

import DomainLayer.IRepository.IStoreRepository;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreRating;
import DomainLayer.Enums.StoreManagerPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StoreServiceAcceptanceTest {

    private StoreService storeService;
    private IStoreRepository storeRepository;
    private Store store;
    private int storeId = 1;
    private int founderId = 10;
    private int noPermsId = 20;
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        storeRepository = mock(IStoreRepository.class);
        store = new Store("Test Store", founderId, publisher);
        storeService = new StoreService(storeRepository, publisher);
        
    }

    @Test
    void closeStore_Founder_Success() {
        when(storeRepository.findById(storeId)).thenReturn(store);

        assertTrue(store.isOpen());
        storeService.closeStore(storeId, founderId);
        assertFalse(store.isOpen());

        verify(storeRepository).findById(storeId);
    }

    @Test
    void closeStore_NotFounder_ThrowsAccessError() {
        when(storeRepository.findById(storeId)).thenReturn(store);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.closeStore(storeId, noPermsId));

        assertTrue(ex.getMessage().contains("not a Store Founder"));
        assertTrue(store.isOpen());

        verify(storeRepository).findById(storeId);
    }

    @Test
    void closeStore_AlreadyClosed_ThrowsIllegalArgument() {
        when(storeRepository.findById(storeId)).thenReturn(store);

        storeService.closeStore(storeId, founderId);
        assertFalse(store.isOpen());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.closeStore(storeId, founderId));

        assertTrue(ex.getMessage().contains("already closed"));
    }

    @Test
    void closeStore_StoreNotFound_ShouldThrow() {
        when(storeRepository.findById(storeId)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.closeStore(storeId, founderId));

        assertTrue(ex.getMessage().contains("Store not found"));
    }
    @Test
    void ratingStore_ValidRating_Success() {
        when(storeRepository.findById(storeId)).thenReturn(store);
        assertTrue(store.isOpen());
        storeService.addStoreRating(storeId, founderId, 4.5, "Great store!");
        StoreRating rating = store.getStoreRatingByUser(founderId);
        assertNotNull(rating);
        assertEquals(4.5, rating.getRating(), 0.01);
        verify(storeRepository).findById(storeId);
    }
    @Test
    void ratingStore_StoreNotFound_ShouldThrow() {
        when(storeRepository.findById(storeId)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreRating(storeId, founderId, 4.5, "Great store!"));

        assertTrue(ex.getMessage().contains("Store not found"));
    }

    @Test
    void getStoreOwners_OwnerRequest_Success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.contains(founderId));
        verify(storeRepository).findById(storeId);
    }
    @Test
    void addStoreOwner_OwnerRequest_UserAccept_Success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        storeService.acceptAssignment(storeId, noPermsId);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.contains(noPermsId));
        verify(storeRepository, times(3)).findById(storeId);
    }

    @Test
    void addStoreOwner_StoreNotFound_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, founderId, noPermsId));

        assertTrue(ex.getMessage().contains("Store not found"));
    }

    @Test
    void addStoreOwner_noPermsRequest_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, noPermsId, noPermsId));

        assertTrue(ex.getMessage().contains("is not a valid store owner"));
    }

    @Test
    void addStoreOwner_alreadyOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        storeService.acceptAssignment(storeId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, founderId, noPermsId));

        assertTrue(ex.getMessage().contains("is already a store owner"));
    }

    @Test
    void addStoreManager_ownerRequestUserAccept_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        HashMap<Integer, List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        assertTrue(managers.containsKey(noPermsId));
        verify(storeRepository, times(3)).findById(storeId);
    }
 
    @Test
    void addStoreManager_noPermsRequest_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManager(storeId, noPermsId, noPermsId, perms));

        assertTrue(ex.getMessage().contains("is not a valid store owner"));
    }

    @Test
    void addStoreManager_emptyPermList_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = null;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManager(storeId, founderId, noPermsId, perms));

        assertTrue(ex.getMessage().contains("Permissions list is empty"));

        List<StoreManagerPermission> perms2 = new ArrayList<>();
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManager(storeId, founderId, noPermsId, perms2));

        assertTrue(ex2.getMessage().contains("Permissions list is empty"));

    }

    @Test
    void addStoreManager_alreadyManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManager(storeId, founderId, noPermsId, perms));

        assertTrue(ex.getMessage().contains("is already a store manager"));
    }

    @Test
    void addStoreOwner_alreadyManager_Success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        storeService.acceptAssignment(storeId, noPermsId);
        assertFalse(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        assertTrue(storeService.getStoreOwners(storeId, founderId).contains(noPermsId));
        verify(storeRepository,times(7)).findById(storeId);
    }

    @Test
    void addStoreOwner_alreadyManager_notTheSameAppointor_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        int tempOwner = 1234;
        storeService.addStoreOwner(storeId, founderId, tempOwner);
        storeService.acceptAssignment(storeId, tempOwner);
        assertTrue(storeService.getStoreOwners(storeId, founderId).contains(tempOwner));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, tempOwner, noPermsId));

        assertTrue(ex.getMessage().contains("appointor can reassign"));
    }

    @Test
    void addStoreManagerPermissions_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        List<StoreManagerPermission> newPerms = new ArrayList<>(List.of(StoreManagerPermission.DISCOUNT_POLICY));
        storeService.addStoreManagerPermissions(storeId, founderId, noPermsId, newPerms);
        List<StoreManagerPermission> returnedPerms = storeService.getStoreManagers(storeId, founderId).get(noPermsId);
        boolean equal = returnedPerms.size() == 2 && returnedPerms.contains(StoreManagerPermission.INVENTORY) && returnedPerms.contains(StoreManagerPermission.DISCOUNT_POLICY);
        assertTrue(equal);
        storeService.addStoreManagerPermissions(storeId, founderId, noPermsId, new ArrayList<>(List.of(StoreManagerPermission.DISCOUNT_POLICY))); // should succeed with no change
        List<StoreManagerPermission> returnedPerms2 = storeService.getStoreManagers(storeId, founderId).get(noPermsId);
        assertTrue(returnedPerms.equals(returnedPerms2));
        verify(storeRepository, times(6)).findById(storeId);
    }

    @Test
    void addStoreManagerPermissions_notOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        List<StoreManagerPermission> newPerms = new ArrayList<>(List.of(StoreManagerPermission.DISCOUNT_POLICY));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManagerPermissions(storeId, noPermsId, noPermsId, newPerms));
        assertTrue(ex.getMessage().contains("not a valid store owner"));
    }

    @Test
    void addStoreManagerPermissions_notManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManagerPermissions(storeId, founderId, noPermsId, perms));
        assertTrue(ex.getMessage().contains("not a valid store manager"));
    }
 
    @Test
    void addStoreManagerPermissions_notFather_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 222;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, tmp_owner, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManagerPermissions(storeId, founderId, noPermsId, perms));
        assertTrue(ex.getMessage().contains("appointor can change/remove"));
    }

    @Test
    void removeStoreManagerPermissions_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        storeService.removeStoreManagerPermissions(storeId, founderId, noPermsId, new ArrayList<>(List.of(StoreManagerPermission.INVENTORY)));
        List<StoreManagerPermission> returnedPerms = storeService.getStoreManagers(storeId, founderId) .get(noPermsId);
        assertTrue(returnedPerms.equals(new ArrayList<>(List.of(StoreManagerPermission.DISCOUNT_POLICY))));
        verify(storeRepository, times(4)).findById(storeId);
    }

    @Test
    void removeStoreManagerPermissions_notOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManagerPermissions(storeId, noPermsId, noPermsId, new ArrayList<>(List.of(StoreManagerPermission.INVENTORY))));
        assertTrue(ex.getMessage().contains("not a valid store owner"));
    }

    @Test
    void removeStoreManagerPermissions_notManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManagerPermissions(storeId, founderId, noPermsId, perms));
        assertTrue(ex.getMessage().contains("not a valid store manager"));
    }
 
    @Test
    void removeStoreManagerPermissions_managerDoesNotHaveRequestedPermission_shouldThrow_shouldReset(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.PURCHASE_POLICY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManagerPermissions(storeId, founderId, noPermsId, new ArrayList<>(List.of(StoreManagerPermission.INVENTORY ,StoreManagerPermission.DISCOUNT_POLICY))));
        assertTrue(ex.getMessage().contains("can not remove permission"));
        List<StoreManagerPermission> returnedPerms = storeService.getStoreManagers(storeId, founderId).get(noPermsId);
        boolean equal = returnedPerms.size() == 2 && returnedPerms.contains(StoreManagerPermission.INVENTORY) && returnedPerms.contains(StoreManagerPermission.PURCHASE_POLICY); // should reset to original perms on failure
        assertTrue(equal);
        verify(storeRepository,times(4)).findById(storeId);
    }

    @Test
    void removeStoreManagerPermissions_managerPermissionsLeftEmpty_shouldThrow_shouldReset(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.PURCHASE_POLICY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManagerPermissions(storeId, founderId, noPermsId, new ArrayList<>(perms)));
        assertTrue(ex.getMessage().contains("permissions can not be empty"));
        List<StoreManagerPermission> returnedPerms = storeService.getStoreManagers(storeId, founderId).get(noPermsId);
        boolean equal = returnedPerms.size() == 2 && returnedPerms.contains(StoreManagerPermission.INVENTORY) && returnedPerms.contains(StoreManagerPermission.PURCHASE_POLICY); // should reset to original perms on failure
        assertTrue(equal);
        verify(storeRepository,times(4)).findById(storeId);
    }

    @Test
    void removeStoreManagerPermissions_notFather_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 222;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.PURCHASE_POLICY));
        storeService.addStoreManager(storeId, tmp_owner, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        List<StoreManagerPermission> permsToRemove = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManagerPermissions(storeId, founderId, noPermsId, permsToRemove));
        assertTrue(ex.getMessage().contains("appointor can change/remove"));
    }

    @Test
    void removeStoreOwner_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        int tmp_owner2 = 40;
        int tmp_mngr = 50;
        int tmp_mngr2 = 60;
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreOwner(storeId, tmp_owner, tmp_owner2);
        storeService.acceptAssignment(storeId, tmp_owner2);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr, perms);
        storeService.acceptAssignment(storeId, tmp_mngr);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr2, perms);
        storeService.acceptAssignment(storeId, tmp_mngr2);
        storeService.removeStoreOwner(storeId, founderId, tmp_owner);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        HashMap<Integer,List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        assertTrue(!owners.contains(tmp_owner));
        assertTrue(!owners.contains(tmp_owner2));
        assertTrue(!managers.containsKey(tmp_mngr));
        assertTrue(!managers.containsKey(tmp_mngr));
        assertTrue(owners.contains(founderId) && owners.size() == 1);
        verify(storeRepository,times(11)).findById(storeId);
    }
    
    @Test
    void removeStoreOwner_removeSelf_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        int tmp_owner2 = 40;
        int tmp_mngr = 50;
        int tmp_mngr2 = 60;
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreOwner(storeId, tmp_owner, tmp_owner2);
        storeService.acceptAssignment(storeId, tmp_owner2);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr, perms);
        storeService.acceptAssignment(storeId, tmp_mngr);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr2, perms);
        storeService.acceptAssignment(storeId, tmp_mngr2);
        storeService.removeStoreOwner(storeId, tmp_owner, tmp_owner);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        HashMap<Integer,List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        assertTrue(!owners.contains(tmp_owner));
        assertTrue(!owners.contains(tmp_owner2));
        assertTrue(!managers.containsKey(tmp_mngr));
        assertTrue(!managers.containsKey(tmp_mngr));
        assertTrue(owners.contains(founderId) && owners.size() == 1);
        verify(storeRepository,times(11)).findById(storeId);
    }

    @Test
    void removeStoreOwner_requestNotOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreOwner(storeId, noPermsId, tmp_owner));
        assertTrue(ex.getMessage().contains(noPermsId + " is not a valid store owner"));
        assertTrue(owners.equals(storeService.getStoreOwners(storeId, founderId)));
    }

    @Test
    void removeStoreOwner_toRemoveNotOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreOwner(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains(noPermsId + " is not a valid store owner"));
        assertTrue(owners.equals(storeService.getStoreOwners(storeId, founderId)));
    }
    
    @Test
    void removeStoreOwner_notFather_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreOwner(storeId, tmp_owner, noPermsId);
        storeService.acceptAssignment(storeId, noPermsId);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreOwner(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains("appointor can change/remove"));
        assertTrue(owners.equals(storeService.getStoreOwners(storeId, founderId)));
    }

    @Test
    void removeStoreOwner_isFounder_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreOwner(storeId, founderId, founderId));
        assertTrue(ex.getMessage().contains("Can not remove Store Founder"));
        assertTrue(owners.equals(storeService.getStoreOwners(storeId, founderId)));
    }
 
    @Test
    void removeStoreManager_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        int tmp_owner2 = 40;
        int tmp_mngr = 50;
        int tmp_mngr2 = 60;
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreOwner(storeId, tmp_owner, tmp_owner2);
        storeService.acceptAssignment(storeId, tmp_owner2);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr, perms);
        storeService.acceptAssignment(storeId, tmp_mngr);
        storeService.addStoreManager(storeId, tmp_owner2, tmp_mngr2, perms);
        storeService.acceptAssignment(storeId, tmp_mngr2);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        storeService.removeStoreManager(storeId, tmp_owner2, tmp_mngr2);
        HashMap<Integer,List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        assertTrue(owners.contains(tmp_owner));
        assertTrue(owners.contains(tmp_owner2));
        assertTrue(managers.containsKey(tmp_mngr));
        assertTrue(owners.equals(storeService.getStoreOwners(storeId, founderId)));
        assertTrue(!managers.containsKey(tmp_mngr2));
        verify(storeRepository,times(12)).findById(storeId);
    }

    @Test
    void removeStoreManager_requestNotOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_mngr = 30;
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, tmp_mngr, perms);
        storeService.acceptAssignment(storeId, tmp_mngr);
        HashMap<Integer,List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManager(storeId, noPermsId, tmp_mngr));
        assertTrue(ex.getMessage().contains(noPermsId + " is not a valid store owner"));
        assertTrue(managers.containsKey(tmp_mngr));
    }

    @Test
    void removeStoreManager_toRemoveNotManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManager(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains(noPermsId + " is not a valid store manager"));
    }
 
    @Test
    void removeStoreManager_notFather_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 30;
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreManager(storeId, tmp_owner, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        HashMap<Integer,List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.removeStoreManager(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains("appointor can change/remove"));
        assertTrue(managers.equals(storeService.getStoreManagers(storeId, founderId)));
    }

    @Test
    void addStoreOwner_OwnerRequestUserPending_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        List<Integer> pending = storeService.getPendingOwners(storeId, founderId);
        assertTrue(pending.contains(noPermsId));
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.equals(List.of(founderId)));
        verify(storeRepository, times(3)).findById(storeId);
    }

    @Test
    void declineAssignment_ForOwner_OwnerRequestUserDecline_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        List<Integer> pending = storeService.getPendingOwners(storeId, founderId);
        assertTrue(pending.contains(noPermsId));
        storeService.declineAssignment(storeId, noPermsId);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.equals(List.of(founderId)));
        pending = storeService.getPendingOwners(storeId, founderId);
        assertTrue(!pending.contains(noPermsId));
        verify(storeRepository, times(5)).findById(storeId);
    }

    @Test
    void acceptAssignment_ForOwner_OwnerRequestUserAccept_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        storeService.acceptAssignment(storeId, noPermsId);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.contains(noPermsId));
        verify(storeRepository, times(3)).findById(storeId);
    }

    @Test
    void addStoreManager_OwnerRequestUserPending_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        List<Integer> pending = storeService.getPendingManagers(storeId, founderId);
        assertTrue(pending.contains(noPermsId));
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.equals(List.of(founderId)));
        verify(storeRepository, times(3)).findById(storeId);
    }
 
    @Test
    void declineAssignment_ForManager_OwnerRequestUserDecline_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        List<Integer> pending = storeService.getPendingManagers(storeId, founderId);
        assertTrue(pending.contains(noPermsId));
        storeService.declineAssignment(storeId, noPermsId);
        List<Integer> managers = new ArrayList<>(storeService.getStoreManagers(storeId, founderId).keySet());
        assertTrue(managers.isEmpty());
        pending = storeService.getPendingManagers(storeId, founderId);
        assertTrue(!pending.contains(noPermsId));
        verify(storeRepository, times(5)).findById(storeId);
    }

    @Test
    void acceptAssignment_ForManager_OwnerRequestUserAccept_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        storeService.acceptAssignment(storeId, noPermsId);
        List<Integer> managers = new ArrayList<>(storeService.getStoreManagers(storeId, founderId).keySet());
        assertTrue(managers.contains(noPermsId));
        verify(storeRepository, times(3)).findById(storeId);
    }

    @Test
    void acceptAssignment_UserNotPending_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                             () -> storeService.acceptAssignment(storeId, noPermsId));
        assertTrue(ex.getMessage().contains("User " + noPermsId + " has no pending assignments"));
        assertTrue(!storeService.getStoreOwners(storeId, founderId).contains(noPermsId));
        assertTrue(!storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
    }

    @Test
    void acceptAssignment_ForOwner_AppointorNoLongerOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 123;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        storeService.addStoreOwner(storeId, tmp_owner, noPermsId);
        storeService.removeStoreOwner(storeId, founderId, tmp_owner);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.acceptAssignment(storeId, noPermsId));
        assertTrue(ex.getMessage().contains("is no longer a valid store owner"));
        assertTrue(!storeService.getStoreOwners(storeId, founderId).contains(noPermsId));
        assertTrue(!storeService.getPendingOwners(storeId, founderId).contains(noPermsId));
    }
 
    @Test
    void acceptAssignment_ForManager_AppointorNoLongerOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        int tmp_owner = 123;
        storeService.addStoreOwner(storeId, founderId, tmp_owner);
        storeService.acceptAssignment(storeId, tmp_owner);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, tmp_owner, noPermsId, perms);
        storeService.removeStoreOwner(storeId, founderId, tmp_owner);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.acceptAssignment(storeId, noPermsId));
        assertTrue(ex.getMessage().contains("is no longer a valid store owner"));
        assertTrue(!storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        assertTrue(!storeService.getPendingManagers(storeId, founderId).contains(noPermsId));
    }
    
    @Test
    void addStoreOwner_AppointeeAlreadyPendingManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.addStoreOwner(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains("Already pending user " + noPermsId + " approval for managment"));
        assertTrue(storeService.getPendingManagers(storeId, founderId).contains(noPermsId));
    }

    @Test
    void addStoreOwner_AppointeeAlreadyPendingOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.addStoreOwner(storeId, founderId, noPermsId));
        assertTrue(ex.getMessage().contains("Already pending user " + noPermsId + " approval for ownership"));
        assertTrue(storeService.getPendingOwners(storeId, founderId).contains(noPermsId));
    }

    @Test
    void addStoreManager_AppointeeAlreadyPendingManager_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.addStoreManager(storeId, founderId, noPermsId, perms));
        assertTrue(ex.getMessage().contains("Already pending user " + noPermsId + " approval for managment"));
        assertTrue(storeService.getPendingManagers(storeId, founderId).contains(noPermsId));
    }
    
    @Test
    void addStoreManager_AppointeeAlreadyPendingOwner_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = new ArrayList<>(List.of(StoreManagerPermission.INVENTORY));
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                    () -> storeService.addStoreManager(storeId, founderId, noPermsId, perms));
        assertTrue(ex.getMessage().contains("Already pending user " + noPermsId + " approval for ownership"));
        assertTrue(storeService.getPendingOwners(storeId, founderId).contains(noPermsId));
    }
}
