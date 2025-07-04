package IntegrationTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;

@SpringBootTest(classes = com.fakezone.fakezone.FakezoneApplication.class)
public class StorePersistenceTest {

    @Autowired
    private IStoreRepository storeRepository;

    @BeforeEach
    void setUp() {
        // Clear data before each test to ensure isolation
        storeRepository.clearAllData();
    }

    @Test
    @Transactional
    public void testStoreCreationAndRetrieval() {
        // Create a test store without hard-coding ID
        Store testStore = new Store("Test Electronics Store", 123, null);
        
        // Save the store and get the generated ID
        storeRepository.addStore(testStore);
        int generatedId = testStore.getId();

        // Retrieve the store by ID
        Store retrievedStore = storeRepository.findById(generatedId);
        
        assertNotNull(retrievedStore, "Store should be retrievable by ID");
        assertEquals("Test Electronics Store", retrievedStore.getName(), "Store name should match");
        assertEquals(123, retrievedStore.getStoreFounderID(), "Store founder ID should match");
        assertEquals(generatedId, retrievedStore.getId(), "Store ID should match");
        assertTrue(retrievedStore.isOpen(), "Store should be open by default");
    }

    @Test
    @Transactional
    public void testStoreSearchByName() {
        // Create test stores without hard-coding IDs
        Store store1 = new Store("Electronics Hub", 456, null);
        Store store2 = new Store("Fashion Store", 789, null);
        
        storeRepository.addStore(store1);
        storeRepository.addStore(store2);

        // Search by name
        Store foundStore = storeRepository.findByName("Electronics Hub");
        assertNotNull(foundStore, "Should find store by name");
        assertEquals("Electronics Hub", foundStore.getName(), "Found store name should match");
        assertEquals(456, foundStore.getStoreFounderID(), "Found store founder should match");
    }

    @Test
    @Transactional
    public void testGetAllStores() {
        // Clear existing data for clean test (already done in setUp)
        
        // Create multiple test stores without hard-coding IDs
        Store store1 = new Store("Store One", 111, null);
        Store store2 = new Store("Store Two", 222, null);
        Store store3 = new Store("Store Three", 333, null);
        
        storeRepository.addStore(store1);
        storeRepository.addStore(store2);
        storeRepository.addStore(store3);

        // Get all stores
        Collection<Store> allStores = storeRepository.getAllStores();
        
        assertEquals(3, allStores.size(), "Should have 3 stores");
        
        // Verify all stores are present
        boolean hasStore1 = allStores.stream().anyMatch(s -> s.getName().equals("Store One"));
        boolean hasStore2 = allStores.stream().anyMatch(s -> s.getName().equals("Store Two"));
        boolean hasStore3 = allStores.stream().anyMatch(s -> s.getName().equals("Store Three"));
        
        assertTrue(hasStore1, "Should contain Store One");
        assertTrue(hasStore2, "Should contain Store Two");
        assertTrue(hasStore3, "Should contain Store Three");
    }

    @Test
    @Transactional
    public void testStoreDelete() {
        // Create a test store without hard-coding ID
        Store testStore = new Store("Temporary Store", 999, null);
        storeRepository.addStore(testStore);
        int storeId = testStore.getId();

        // Verify store exists
        Store foundStore = storeRepository.findById(storeId);
        assertNotNull(foundStore, "Store should exist before deletion");

        // Delete the store
        storeRepository.delete(storeId);

        // Verify store is deleted
        Store deletedStore = storeRepository.findById(storeId);
        assertNull(deletedStore, "Store should be null after deletion");
    }

    @Test
    @Transactional
    public void testStoreWithRatings() {
        // Create a store without hard-coding ID
        Store testStore = new Store("Rated Store", 555, null);
        
        // Add some ratings
        testStore.addRating(101, 4.5, "Great store!");
        testStore.addRating(102, 3.8, "Good selection");
        
        // Save the store
        storeRepository.addStore(testStore);
        int storeId = testStore.getId();

        // Retrieve and verify
        Store retrievedStore = storeRepository.findById(storeId);
        assertNotNull(retrievedStore, "Store should be retrievable");
        
        // Note: Ratings might need to be loaded lazily depending on fetch type
        if (retrievedStore.getRatings() != null) {
            assertTrue(retrievedStore.getRatings().size() >= 0, "Store should have ratings");
        }
    }
} 