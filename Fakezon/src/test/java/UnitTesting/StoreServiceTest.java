package UnitTesting;
import static org.junit.jupiter.api.Assertions.*;
import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
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
        storeRepository = mock(IStoreRepository.class);
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
}
