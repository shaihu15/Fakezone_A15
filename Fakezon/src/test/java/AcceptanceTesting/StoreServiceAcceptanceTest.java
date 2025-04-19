package AcceptanceTesting;

import DomainLayer.IRepository.IStoreRepository;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreRating;
import DomainLayer.Enums.StoreManagerPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

    @BeforeEach
    void setUp() {
        storeRepository = mock(IStoreRepository.class);
        store = new Store("Test Store", founderId);
        storeService = new StoreService(storeRepository);
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
    void addStoreOwner_OwnerRequest_Success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        List<Integer> owners = storeService.getStoreOwners(storeId, founderId);
        assertTrue(owners.contains(noPermsId));
        verify(storeRepository, times(2)).findById(storeId);
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, founderId, noPermsId));

        assertTrue(ex.getMessage().contains("is already a store owner"));
    }

    @Test
    void addStoreManager_ownerRequest_success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.INVENTORY);
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        HashMap<Integer, List<StoreManagerPermission>> managers = storeService.getStoreManagers(storeId, founderId);
        assertTrue(managers.containsKey(noPermsId));
        verify(storeRepository, times(2)).findById(storeId);
    }

    @Test
    void addStoreManager_noPermsRequest_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.INVENTORY);
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
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.INVENTORY);
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreManager(storeId, founderId, noPermsId, perms));

        assertTrue(ex.getMessage().contains("is already a store manager"));
    }

    @Test
    void addStoreOwner_alreadyManager_Success(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.INVENTORY);
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        storeService.addStoreOwner(storeId, founderId, noPermsId);
        assertFalse(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        assertTrue(storeService.getStoreOwners(storeId, founderId).contains(noPermsId));
        verify(storeRepository,times(5)).findById(storeId);
    }

    @Test
    void addStoreOwner_alreadyManager_notTheSameAppointor_shouldThrow(){
        when(storeRepository.findById(storeId)).thenReturn(store);
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.INVENTORY);
        storeService.addStoreManager(storeId, founderId, noPermsId, perms);
        assertTrue(storeService.getStoreManagers(storeId, founderId).containsKey(noPermsId));
        int tempOwner = 1234;
        storeService.addStoreOwner(storeId, founderId, tempOwner);
        assertTrue(storeService.getStoreOwners(storeId, founderId).contains(tempOwner));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storeService.addStoreOwner(storeId, tempOwner, noPermsId));

        assertTrue(ex.getMessage().contains("appointor can reassign"));
    }


}
