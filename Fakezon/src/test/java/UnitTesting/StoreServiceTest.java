package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Services.StoreService;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;

public class StoreServiceTest {

    private StoreService storeService;
    private IStoreRepository storeRepository;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        storeRepository = mock(IStoreRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        storeService = new StoreService(storeRepository, eventPublisher);
    }

    @Test
    void testViewStore_Success() {
        // Arrange
        int storeId = 1;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getId()).thenReturn(storeId);
        when(mockStore.getName()).thenReturn("Test Store");
        when(mockStore.getStoreFounderID()).thenReturn(10);
        when(mockStore.isOpen()).thenReturn(true);
        when(mockStore.getStoreProducts()).thenReturn(new HashMap<>());
        when(mockStore.getRatings()).thenReturn(new HashMap<>());
        when(mockStore.getAverageRating()).thenReturn(4.5);

        // Act
        StoreDTO result = storeService.viewStore(storeId);

        // Assert
        assertNotNull(result);
        assertEquals(storeId, result.getStoreId());
        assertEquals("Test Store", result.getName());
        assertEquals(10, result.getFounderId());
        assertTrue(result.isOpen());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void testViewStore_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.viewStore(storeId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void testAddStore_Success() {
        // Arrange
        int userId = 1;
        String storeName = "New Store";
        when(storeRepository.findByName(storeName)).thenReturn(null);

        // Act
        int result = storeService.addStore(userId, storeName);

        // Assert
        assertTrue(result > 0); // Store ID should be positive
        verify(storeRepository, times(1)).findByName(storeName);
        verify(storeRepository, times(1)).addStore(any(Store.class));
    }

    @Test
    void testAddStore_StoreNameAlreadyExists_ThrowsException() {
        // Arrange
        int userId = 1;
        String storeName = "Existing Store";
        Store existingStore = mock(Store.class);
        when(storeRepository.findByName(storeName)).thenReturn(existingStore);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStore(userId, storeName);
        });

        assertEquals("Store name already exists", exception.getMessage());
        verify(storeRepository, times(1)).findByName(storeName);
        verify(storeRepository, never()).addStore(any(Store.class));
    }

    @Test
    void testAddStore_EmptyStoreName_ThrowsException() {
        // Arrange
        int userId = 1;
        String storeName = "";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStore(userId, storeName);
        });

        assertEquals("Store name is empty", exception.getMessage());
        verify(storeRepository, never()).findByName(anyString());
        verify(storeRepository, never()).addStore(any(Store.class));
    }

    @Test
    void testAddStore_NullStoreName_ThrowsException() {
        // Arrange
        int userId = 1;
        String storeName = null;

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStore(userId, storeName);
        });

        assertEquals("Store name is empty", exception.getMessage());
        verify(storeRepository, never()).findByName(anyString());
        verify(storeRepository, never()).addStore(any(Store.class));
    }

    @Test
    void testGetAllStores_Success() {
        // Arrange
        Collection<Store> mockStores = new ArrayList<>();
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        mockStores.add(store1);
        mockStores.add(store2);

        when(storeRepository.getAllStores()).thenReturn(mockStores);
        when(store1.getId()).thenReturn(1);
        when(store1.getName()).thenReturn("Store 1");
        when(store1.getStoreFounderID()).thenReturn(10);
        when(store1.isOpen()).thenReturn(true);
        when(store1.getStoreProducts()).thenReturn(new HashMap<>());
        when(store1.getRatings()).thenReturn(new HashMap<>());
        when(store1.getAverageRating()).thenReturn(4.0);

        when(store2.getId()).thenReturn(2);
        when(store2.getName()).thenReturn("Store 2");
        when(store2.getStoreFounderID()).thenReturn(20);
        when(store2.isOpen()).thenReturn(false);
        when(store2.getStoreProducts()).thenReturn(new HashMap<>());
        when(store2.getRatings()).thenReturn(new HashMap<>());
        when(store2.getAverageRating()).thenReturn(3.5);

        // Act
        List<StoreDTO> result = storeService.getAllStores();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Store 1", result.get(0).getName());
        assertEquals("Store 2", result.get(1).getName());
        verify(storeRepository, times(1)).getAllStores();
    }

    @Test
    void testSearchStores_Success() {
        // Arrange
        String keyword = "Tech";
        Collection<Store> mockStores = new ArrayList<>();
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        Store store3 = mock(Store.class);
        mockStores.add(store1);
        mockStores.add(store2);
        mockStores.add(store3);

        when(storeRepository.getAllStores()).thenReturn(mockStores);
        when(store1.getName()).thenReturn("Tech Store");
        when(store2.getName()).thenReturn("Food Market");
        when(store3.getName()).thenReturn("TechWorld");

        // Only stores with "Tech" in name should match
        when(store1.getId()).thenReturn(1);
        when(store1.getStoreFounderID()).thenReturn(10);
        when(store1.isOpen()).thenReturn(true);
        when(store1.getStoreProducts()).thenReturn(new HashMap<>());
        when(store1.getRatings()).thenReturn(new HashMap<>());
        when(store1.getAverageRating()).thenReturn(4.0);

        when(store3.getId()).thenReturn(3);
        when(store3.getStoreFounderID()).thenReturn(30);
        when(store3.isOpen()).thenReturn(true);
        when(store3.getStoreProducts()).thenReturn(new HashMap<>());
        when(store3.getRatings()).thenReturn(new HashMap<>());
        when(store3.getAverageRating()).thenReturn(4.5);

        // Act
        List<StoreDTO> result = storeService.searchStores(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tech Store", result.get(0).getName());
        assertEquals("TechWorld", result.get(1).getName());
        verify(storeRepository, times(1)).getAllStores();
    }

    @Test
    void testCloseStore_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        // Act
        storeService.closeStore(storeId, requesterId);

        // Assert
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).closeStore(requesterId);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testCloseStore_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.closeStore(storeId, requesterId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void testCloseStoreByAdmin_Success() {
        // Arrange
        int storeId = 1;
        int adminId = 100;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        // Act
        storeService.closeStoreByAdmin(storeId, adminId);

        // Assert
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).closeStoreByAdmin(adminId);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testCloseStoreByAdmin_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int adminId = 100;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.closeStoreByAdmin(storeId, adminId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void testAddStoreOwner_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        int newOwnerId = 20;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        // Act
        storeService.addStoreOwner(storeId, requesterId, newOwnerId);

        // Assert
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).addStoreOwner(requesterId, newOwnerId);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testAddStoreOwner_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        int newOwnerId = 20;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStoreOwner(storeId, requesterId, newOwnerId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void testGetStoreOwners_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        Store mockStore = mock(Store.class);
        List<Integer> expectedOwners = List.of(10, 20, 30);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getStoreOwners(requesterId)).thenReturn(expectedOwners);

        // Act
        List<Integer> result = storeService.getStoreOwners(storeId, requesterId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedOwners, result);
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).getStoreOwners(requesterId);
    }

    @Test
    void testGetStoreOwners_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStoreOwners(storeId, requesterId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void testGetStoreManagers_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        Store mockStore = mock(Store.class);
        HashMap<Integer, List<StoreManagerPermission>> expectedManagers = new HashMap<>();
        expectedManagers.put(30, List.of(StoreManagerPermission.INVENTORY));
        when(storeRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getStoreManagers(requesterId)).thenReturn(expectedManagers);

        // Act
        HashMap<Integer, List<StoreManagerPermission>> result = storeService.getStoreManagers(storeId, requesterId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedManagers, result);
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).getStoreManagers(requesterId);
    }

    @Test
    void testGetStoreManagers_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStoreManagers(storeId, requesterId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void testAddProductToStore_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        int productId = 100;
        String name = "Test Product";
        double basePrice = 29.99;
        int quantity = 50;
        PCategory category = PCategory.ELECTRONICS;
        Store mockStore = mock(Store.class);
        StoreProductDTO expectedProductDTO = mock(StoreProductDTO.class);
        
        when(storeRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.addStoreProduct(requesterId, productId, name, basePrice, quantity, category))
                .thenReturn(expectedProductDTO);

        // Act
        StoreProductDTO result = storeService.addProductToStore(storeId, requesterId, productId, name, basePrice, quantity, category);

        // Assert
        assertNotNull(result);
        assertEquals(expectedProductDTO, result);
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).addStoreProduct(requesterId, productId, name, basePrice, quantity, category);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testAddProductToStore_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        int productId = 100;
        String name = "Test Product";
        double basePrice = 29.99;
        int quantity = 50;
        PCategory category = PCategory.ELECTRONICS;
        
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addProductToStore(storeId, requesterId, productId, name, basePrice, quantity, category);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void testIsStoreOpen_Success() {
        // Arrange
        int storeId = 1;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.isOpen()).thenReturn(true);

        // Act
        boolean result = storeService.isStoreOpen(storeId);

        // Assert
        assertTrue(result);
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).isOpen();
    }

    @Test
    void testIsStoreOpen_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.isStoreOpen(storeId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void testRemoveStoreOwner_Success() {
        // Arrange
        int storeId = 1;
        int requesterId = 10;
        int ownerId = 20;
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        // Act
        storeService.removeStoreOwner(storeId, requesterId, ownerId);

        // Assert
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).removeStoreOwner(requesterId, ownerId);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testRemoveStoreOwner_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int requesterId = 10;
        int ownerId = 20;
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.removeStoreOwner(storeId, requesterId, ownerId);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    void testAddStoreRating_Success() {
        // Arrange
        int storeId = 1;
        int userId = 10;
        double rating = 4.5;
        String comment = "Great store!";
        Store mockStore = mock(Store.class);
        when(storeRepository.findById(storeId)).thenReturn(mockStore);

        // Act
        storeService.addStoreRating(storeId, userId, rating, comment);

        // Assert
        verify(storeRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).addRating(userId, rating, comment);
        verify(storeRepository, times(1)).save(mockStore);
    }

    @Test
    void testAddStoreRating_StoreNotFound_ThrowsException() {
        // Arrange
        int storeId = 999;
        int userId = 10;
        double rating = 4.5;
        String comment = "Great store!";
        when(storeRepository.findById(storeId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            storeService.addStoreRating(storeId, userId, rating, comment);
        });

        assertEquals("Store not found", exception.getMessage());
        verify(storeRepository, times(1)).findById(storeId);
        verify(storeRepository, never()).save(any(Store.class));
    }
} 