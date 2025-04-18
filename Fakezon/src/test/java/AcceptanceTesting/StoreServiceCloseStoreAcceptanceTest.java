package AcceptanceTesting;

import DomainLayer.IRepository.IStoreRepository;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StoreServiceCloseStoreAcceptanceTest {

    private StoreService storeService;
    private IStoreRepository storeRepository;
    private Store store;
    private int storeId = 1;
    private int founderId = 10;
    private int nonFounderId = 20;

    @BeforeEach
    void setUp() {
        storeRepository = mock(IStoreRepository.class);
        store = new Store("Test Store", storeId, founderId);
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

        IllegalAccessError ex = assertThrows(IllegalAccessError.class, () ->
                storeService.closeStore(storeId, nonFounderId));

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
}
