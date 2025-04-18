package UnitTesting;
import static org.junit.jupiter.api.Assertions.*;
import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import InfrastructureLayer.Repositories.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class StoreServiceTest {

    private IStoreRepository storeRepository;
    private StoreService storeService;
    private Store mockStore;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepository(); // Assuming StoreRepository is a concrete implementation of IStoreRepository
        storeService = new StoreService(storeRepository);
        mockStore = mock(Store.class);
    }

    @Test
    void testCloseStore_Successful() {
        int storeId = 1;
        int requesterId = 10;

        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        storeService.closeStore(storeId, requesterId);

        verify(storeRepository).findById(storeId);
        verify(mockStore).closeStore(requesterId);
    }

    @Test
    void testCloseStore_StoreNotFound() {
        int storeId = 1;
        int requesterId = 10;

        when(storeRepository.findById(storeId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.closeStore(storeId, requesterId);
        });

        verify(storeRepository).findById(storeId);
    }
    @Test
    void testAddStoreRating_Successful() {
        int storeId = 1;
        int userId = 10;
        int rating = 5;
        String comment = "Great store!";

        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        storeService.addStoreRating(storeId, userId, rating, comment);

        verify(storeRepository).findById(storeId);
    }
    @Test
    void testAddStoreRating_StoreNotFound() {
        int storeId = 1;
        int userId = 10;
        int rating = 5;
        String comment = "Great store!";

        when(storeRepository.findById(storeId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStoreRating(storeId, userId, rating, comment);
        });

        verify(storeRepository).findById(storeId);
    }
    @Test
    void testOpenStore_Successful() {
        String storeName = "Test Store";
        int requesterId = 5;

        assertNull(storeRepository.findByName(storeName));
        int storeId = storeService.openStore(requesterId, storeName);
        assertTrue(storeId > 0);
        Store store1 = storeRepository.findById(storeId);
        assertNotNull(store1);
        assertTrue(store1.getStoreFounderID() == requesterId);
        assertTrue(store1.getName().equals(storeName));
    }
    @Test
    void testOpenStore_StoreAllreadyOpen() {
        String OldStoreName = "Test Store";
        int requesterId = 5;

        assertNull(storeRepository.findByName(OldStoreName));
        int storeId = storeService.openStore(requesterId, OldStoreName);
        Store store = storeRepository.findById(storeId);
        assertNotNull(store);
        String newStoreName = store.getName();
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.openStore(requesterId, newStoreName);
        });
    }


}
