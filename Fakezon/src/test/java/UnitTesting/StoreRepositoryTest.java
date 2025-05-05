package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Model.Store;
import InfrastructureLayer.Repositories.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.mockito.Mockito.*;

public class StoreRepositoryTest {

    private StoreRepository repository;
    private Store store1;
    private Store store2;
    private ApplicationEventPublisher mockPublisher;

    @BeforeEach
    void setUp() {
        repository = new StoreRepository();
        mockPublisher = mock(ApplicationEventPublisher.class);

        store1 = new Store("Store One", 1, mockPublisher);
        store2 = new Store("Store Two", 2, mockPublisher);
    }

    //findById
    @Test
    void givenExistingStoreId_whenFindById_thenReturnStore() {
        repository.addStore(store1);
        Store result = repository.findById(store1.getId());
        assertNotNull(result);
        assertEquals(store1.getId(), result.getId());
    }

    @Test  
    void givenNonExistingStoreId_whenFindById_thenReturnNull() {
        Store result = repository.findById(999); // Assuming 999 does not exist
        assertNull(result);
    }
    
    //findByName
    @Test
    void givenExistingStoreName_whenFindByName_thenReturnStore() {
        repository.addStore(store1);
        Store result = repository.findByName("Store One");
        assertNotNull(result);
        assertEquals(store1.getId(), result.getId());
    }

    @Test
    void givenNonExistingStoreName_whenFindByName_thenReturnNull() {
        repository.addStore(store1);
        Store result = repository.findByName("Nonexistent Store");
        assertNull(result);
    }

    //getAllStores
    @Test
    void givenNoStores_whenGetAllStores_thenReturnEmptyCollection() {
        Collection<Store> allStores = repository.getAllStores();
        assertTrue(allStores.isEmpty());
    }

    @Test
    void givenStores_whenGetAllStores_thenReturnAllStores() {
        repository.addStore(store1);
        repository.addStore(store2);
        Collection<Store> allStores = repository.getAllStores();
        assertEquals(2, allStores.size());
        assertTrue(allStores.contains(store1));
        assertTrue(allStores.contains(store2));
    }

    //addStore
    @Test
    void givenValidStore_whenAddStore_thenStoreIsAdded() {
        repository.addStore(store1);
        assertEquals(store1, repository.findById(store1.getId()));
    }

    //delete
    @Test
    void givenExistingStoreId_whenDelete_thenStoreIsRemoved() {
        repository.addStore(store1);
        repository.delete(store1.getId());
        assertNull(repository.findById(store1.getId()));
    }
    @Test
    void givenNonExistingStoreId_whenDelete_thenNoException() {
        repository.addStore(store1);
        repository.delete(999); // Assuming 999 does not exist
        assertNotNull(repository.findById(store1.getId()));
    }

    //getTop10Stores
    @Test
    void givenNoStores_whenGetTop10Stores_thenReturnEmptyCollection() {
        Collection<Store> top10Stores = repository.getTop10Stores();
        assertTrue(top10Stores.isEmpty());
    }
    @Test
    void givenStores_whenGetTop10Stores_thenReturnTop10Stores() {
        for (int i = 1; i <= 15; i++) {
            Store store = new Store("S" + i, i, mockPublisher); 
            store.addRating(i, i, "Great store!"); 
            repository.addStore(store);
        }
    
        List<Store> top10 = new ArrayList<>(repository.getTop10Stores());
    
        assertEquals(10, top10.size());
        assertEquals("S15", top10.get(0).getName()); 
        assertEquals("S14", top10.get(1).getName()); 
        assertEquals("S13", top10.get(2).getName());
        assertEquals("S12", top10.get(3).getName());
        assertEquals("S11", top10.get(4).getName());
        assertEquals("S10", top10.get(5).getName());
        assertEquals("S9", top10.get(6).getName()); 
        assertEquals("S8", top10.get(7).getName());
        assertEquals("S7", top10.get(8).getName());
        assertEquals("S6", top10.get(9).getName()); 
    }
    

}
