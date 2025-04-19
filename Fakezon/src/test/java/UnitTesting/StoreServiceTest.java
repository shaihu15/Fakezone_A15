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
        String storeName = "Test Store";
        int requesterId = 10;
        int storeId = storeService.addStore(requesterId, storeName);
        assertTrue(storeId > 0);
        Store store1 = storeRepository.findById(storeId);
        assertNotNull(store1);
        storeService.closeStore(storeId, requesterId);
        assertFalse(store1.isOpen());
    }

    @Test
    void testCloseStore_StoreNotFound() {
        int storeId = 1;
        int requesterId = 10;

        assertNull(storeRepository.findById(storeId));
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.closeStore(storeId, requesterId);
        });

    }
    @Test
    void testAddStoreRating_Successful() {
        int userId = 10;
        int rating = 5;
        String comment = "Great store!";
        String storeName = "Test Store";
        int requesterId = 10;
        int storeId = storeService.addStore(requesterId, storeName);
        assertTrue(storeId > 0);
        Store store1 = storeRepository.findById(storeId);
        assertNotNull(store1);
        storeService.addStoreRating(storeId, userId, rating, comment);
        assertEquals(rating, store1.getStoreRatingByUser(userId).getRating());}
    @Test
    void testAddStoreRating_StoreNotFound() {
        int storeId = 1;
        int userId = 10;
        int rating = 5;
        String comment = "Great store!";

        assertNull(storeRepository.findById(storeId));

        assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStoreRating(storeId, userId, rating, comment);
        });

    }
    @Test
    void testOpenStore_Successful() {
        String storeName = "Test Store";
        int requesterId = 5;

        assertNull(storeRepository.findByName(storeName));
        int storeId = storeService.addStore(requesterId, storeName);
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
        int storeId = storeService.addStore(requesterId, OldStoreName);
        Store store = storeRepository.findById(storeId);
        assertNotNull(store);
        String newStoreName = store.getName();
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStore(requesterId, newStoreName);
        });
    }


}
