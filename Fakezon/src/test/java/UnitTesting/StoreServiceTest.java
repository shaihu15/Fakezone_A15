package UnitTesting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import ApplicationLayer.Response;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.context.ApplicationEventPublisher;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Store;
import DomainLayer.Model.helpers.UserMsg;
import InfrastructureLayer.Repositories.StoreRepository;
import ApplicationLayer.Enums.ErrorType;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.stream.Collectors;
import DomainLayer.Model.ProductRating;
class StoreServiceTest {

    private IStoreRepository storeRepository;
    private StoreService storeService;
    private Store mockStore;
    private ApplicationEventPublisher publisher;

    @Mock
    private IStoreRepository mockStoreRepository;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        storeRepository = new StoreRepository(); // Assuming StoreRepository is a concrete implementation of
                                                 // IStoreRepository
        storeService = new StoreService(storeRepository, publisher);
        mockStore = mock(Store.class);
        mockStoreRepository = mock(IStoreRepository.class);
        
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
        assertEquals(rating, store1.getStoreRatingByUser(userId).getRating());
    }

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

    @Test
    void testReceivingMessage_validStoreId_ShouldSucceed() {
        int founderId = 1;
        String storeName = "Test Store2";
        int storeId = storeService.addStore(founderId, storeName);

        // recieving message from user
        String message = "Hello, this is a test message.";
        storeService.receivingMessage(storeId, founderId, message);

        Response<Map<Integer, UserMsg>> ResMessages = storeService.getMessagesFromUsers(storeId, founderId);
        Map<Integer, UserMsg> messages = ResMessages.getData();
        assertFalse(messages.isEmpty(), "Messages from users should not be empty");
        assertEquals(message, messages.entrySet().iterator().next().getValue().getMsg());

    }

    @Test
    void testReceivingMessage_invalidStoreId_ShouldThrow() {
        int invalidStoreId = 999; // Assuming this store ID does not exist
        int founderId = 1;
        String message = "Hello, this is a test message.";

        // Attempt to receive a message from a non-existent store
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.receivingMessage(invalidStoreId, founderId, message);
        }, "Expected receivingMessage to throw if the store ID is invalid");
    }

    @Test
    void testSendMessageToUser_validStoreId_ShouldSucceed() {
        int founderId = 1;
        String storeName = "Test Store3";
        int storeId = storeService.addStore(founderId, storeName);

        // Sending message to user
        int userId = 2;
        String message = "Hello, this is a test message.";
        storeService.sendMessageToUser(founderId, storeId, userId, message);

        // Verify that the message was sent successfully
        assertEquals(message, storeService.getMessagesFromStore(founderId, storeId).peek().getValue(),
                "Message should be sent successfully");
    }

    @Test
    void testSendMessageToUser_invalidStoreId_ShouldThrow() {
        int invalidStoreId = 999; // Assuming this store ID does not exist
        int founderId = 1;
        int userId = 2;
        String message = "Hello, this is a test message.";

        // Attempt to send a message to a user from a non-existent store
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.sendMessageToUser(founderId, invalidStoreId, userId, message);
        }, "Expected sendMessageToUser to throw if the store ID is invalid");
    }

    @Test
    void testSendMessageToUser_invalidFounderId_ShouldThrow() {
        int founderId = 1;
        String storeName = "Test Store4";
        int storeId = storeService.addStore(founderId, storeName);

        // Sending message to user with an invalid founder ID
        int invalidFounderId = 999; // Assuming this founder ID does not exist
        int userId = 2;
        String message = "Hello, this is a test message.";

        // Attempt to send a message to a user with an invalid founder ID
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.sendMessageToUser(invalidFounderId, storeId, userId, message);
        }, "Expected sendMessageToUser to throw if the founder ID is invalid");
    }

    @Test
    void testGetStoreRoles_AsFounder_Success() {
        int founderId = 1;
        String storeName = "Test Store5";
        int storeId = storeService.addStore(founderId, storeName);
        assertTrue(storeId > 0, "Store ID should be a positive number");
        int newOwnerId = 2;
        int managerId = 3;
        storeService.addStoreOwner(storeId, founderId, newOwnerId);// Add a new owner
        storeService.acceptAssignment(storeId, newOwnerId);
        // Define permissions for the manager
        List<StoreManagerPermission> permissions = List.of(StoreManagerPermission.VIEW_ROLES);
        storeService.addStoreManager(storeId, founderId, managerId, permissions);// Add a manager with permissions
        storeService.acceptAssignment(storeId, managerId);
        StoreRolesDTO storeRolesDTO = storeService.getStoreRoles(storeId, founderId);
        // Validate the response
        assertNotNull(storeRolesDTO, "Store roles should not be null for the founder");
        assertEquals(storeId, storeRolesDTO.getStoreId(), "Store ID should match the requested store ID");
        assertEquals(storeName, storeRolesDTO.getStoreName(), "Store name should match the expected value");
        assertEquals(founderId, storeRolesDTO.getFounderId(), "Founder ID should match the expected value");
        // Check owners list
        assertTrue(storeRolesDTO.getStoreOwners().contains(founderId), "Founder should be in the list of store owners");
        assertTrue(storeRolesDTO.getStoreOwners().contains(newOwnerId),
                "New owner should be in the list of store owners");
        // Check managers list and permissions
        assertTrue(storeRolesDTO.getStoreManagers().containsKey(managerId),
                "Manager should be in the list of store managers");
        assertEquals(permissions, storeRolesDTO.getStoreManagers().get(managerId),
                "Manager permissions should match the expected value");
    }

    @Test
    void testGetStoreRoles_UnauthorizedUser_ShouldFailAndThrow() {
        int founderId = 1;
        String storeName = "Test Store6";
        // Create the store
        int storeId = storeService.addStore(founderId, storeName);
        assertTrue(storeId > 0, "Store ID should be a positive number");
        int unauthorizedUserId = 99; // Not a founder, owner, or manager
        // Try to get roles using an unauthorized user
        assertThrows(IllegalArgumentException.class, () -> {
            storeService.getStoreRoles(storeId, unauthorizedUserId);
        });
        // String expectedMessagePart = "not authorized"; // Adjust based on your actual
        // exception message
        // assertTrue(exception.getMessage().toLowerCase().contains(expectedMessagePart),
        // "Exception message should indicate unauthorized access");
    }

    @Test
    void testAddAuctionProductToStore_Success() {
        int storeId = storeService.addStore(1, "AuctionStore1");
        assertTrue(storeId > 0, "Store ID should be a positive number");
        StoreProductDTO storeProductDTO = storeService.addProductToStore(storeId, 1, 101, "AuctionStore1", 50.0, 5,
                PCategory.ELECTRONICS);
        // Adding an auction product to the store
        assertDoesNotThrow(() -> storeService.addAuctionProductToStore(storeId, 1, 101, 50.0, 5));

        // Verify that the product was added successfully
        List<ApplicationLayer.DTO.AuctionProductDTO> auctionProducts = storeService
                .getAuctionProductsFromStore(storeId);
        assertNotNull(auctionProducts);
        assertEquals(1, auctionProducts.size());
        assertEquals(101, auctionProducts.get(0).getProductId());
    }

    @Test
    void testAddAuctionProductToStore_StoreNotFound() {
        int invalidStoreId = 999; // Assuming this store ID does not exist
        assertThrows(IllegalArgumentException.class,
                () -> storeService.addAuctionProductToStore(invalidStoreId, 1, 101, 50.0, 5));

    }

    @Test
    void testAddBidOnAuctionProductInStore_Success() {
        int storeId = storeService.addStore(1, "AuctionStore2");
        assertTrue(storeId > 0, "Store ID should be a positive number");
        StoreProductDTO storeProductDTO = storeService.addProductToStore(storeId, 1, 102, "AuctionStore2", 50.0, 5,
                PCategory.ELECTRONICS);
        // Adding an auction product to the store
        storeService.addAuctionProductToStore(storeId, 1, 102, 50.0, 5);

        // Adding a bid on the auction product
        assertDoesNotThrow(() -> storeService.addBidOnAuctionProductInStore(storeId, 1, 102, 55.0));
    }



    @Test
    void testGetAuctionProductsFromStore_Success() {
        int storeId = storeService.addStore(1, "AuctionStore5");
        assertTrue(storeId > 0, "Store ID should be a positive number");
        StoreProductDTO storeProductDTO = storeService.addProductToStore(storeId, 1, 105, "AuctionStore5", 50.0, 5,
                PCategory.ELECTRONICS);
        // Adding an auction product to the store
        storeService.addAuctionProductToStore(storeId, 1, 105, 50.0, 5);

        // Retrieving auction products from the store
        List<ApplicationLayer.DTO.AuctionProductDTO> auctionProducts = storeService
                .getAuctionProductsFromStore(storeId);
        assertNotNull(auctionProducts);
        assertEquals(1, auctionProducts.size());
        assertEquals(105, auctionProducts.get(0).getProductId());
    }

    @Test
    void testGetAuctionProductsFromStore_StoreNotFound() {
        int invalidStoreId = 999; // Assuming this store ID does not exist
        assertThrows(IllegalArgumentException.class, () -> storeService.getAuctionProductsFromStore(invalidStoreId));
    }

    @Test
    void testAddBidOnAuctionProductInStore_BidTooLow() {
        int storeId = storeService.addStore(1, "AuctionStore6");
        assertTrue(storeId > 0, "Store ID should be a positive number");
        StoreProductDTO storeProductDTO = storeService.addProductToStore(storeId, 1, 106, "AuctionStore6", 50.0, 5,
                PCategory.ELECTRONICS);
        // Adding an auction product to the store
        storeService.addAuctionProductToStore(storeId, 1, 106, 50.0, 5);
        storeService.addBidOnAuctionProductInStore(storeId, 2, 106, 60.0);

        // Attempting to bid lower than current highest bid
        assertThrows(IllegalArgumentException.class,
                () -> storeService.addBidOnAuctionProductInStore(storeId, 3, 106, 55.0));
    }

    @Test
    void testgetMessagesFromUsers_Success() {
        int storeId = 1;
        Store mockStore = mock(Store.class);
        Map<Integer,UserMsg> messages = new HashMap<>();
        messages.put(1, new UserMsg(1, "Hello from user 1"));
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getMessagesFromUsers(1)).thenReturn(messages);

        StoreService service = new StoreService(mockStoreRepository, publisher);
        Response<Map<Integer, UserMsg>> response = service.getMessagesFromUsers(storeId,1);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
        assertEquals("Messages retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    @Test
    void testgetMessagesFromUsers_EmptyMessages() {
        int storeId = 2;
        Store mockStore = mock(Store.class);
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getMessagesFromUsers(1)).thenReturn(new HashMap<>());

        StoreService service = new StoreService(mockStoreRepository, publisher);
        Response<Map<Integer,UserMsg>> response = service.getMessagesFromUsers(storeId,1);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("No messages found", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
    @Test
    void testgetMessagesFromUsers_StoreThrowsException() {
        int storeId = 3;
        Store mockStore = mock(Store.class);
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getMessagesFromUsers(1)).thenThrow(new IllegalArgumentException("DB error"));
        StoreService service = new StoreService(mockStoreRepository, publisher);
    
        Response<Map<Integer,UserMsg>> response = service.getMessagesFromUsers(storeId,1);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during get messages: DB error"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
        @Test
        void testgetMessagesFromUsers_StoreNotFound() {
            int storeId = 999;
            when(mockStoreRepository.findById(storeId)).thenReturn(null);

            StoreService service = new StoreService(mockStoreRepository, publisher);
            assertThrows(IllegalArgumentException.class, () -> service.getMessagesFromUsers(storeId, 1));
        }
    @Test
    void testCalcAmount_SingleStore_Success() {
        int userId = 1;
        int storeId = 10;
        Cart cart = new Cart();
        cart.addProduct(storeId, 101, 2); // storeId, productId, quantity

        Store localMockStore = mock(Store.class);
        when(mockStoreRepository.findById(storeId)).thenReturn(localMockStore);
        when(localMockStore.calcAmount(eq(userId), anyMap(), any(LocalDate.class), any(Cart.class))).thenReturn(50.0);

        StoreService service = new StoreService(mockStoreRepository, publisher); // <-- Use the mock!
        LocalDate date = LocalDate.now();
        Map<Integer, Double> result = service.calcAmount(userId, cart, date);

        assertEquals(1, result.size());
        assertEquals(50.0, result.get(storeId), 0.001);
    }
    @Test
    void testCalcAmount_MultipleStores_Success() {
        int userId = 1;
        int storeId1 = 10, storeId2 = 20;
        Cart cart = new Cart();
        cart.addProduct(storeId1, 101, 2);
        cart.addProduct(storeId2, 201, 1);
    
        Store mockStore1 = mock(Store.class);
        Store mockStore2 = mock(Store.class);
        when(mockStoreRepository.findById(storeId1)).thenReturn(mockStore1);
        when(mockStoreRepository.findById(storeId2)).thenReturn(mockStore2);
        when(mockStore1.calcAmount(eq(userId), anyMap(), any(LocalDate.class), any(Cart.class))).thenReturn(30.0);
        when(mockStore2.calcAmount(eq(userId), anyMap(), any(LocalDate.class), any(Cart.class))).thenReturn(70.0);
    
        StoreService service = new StoreService(mockStoreRepository, publisher); // <-- Use the mock!
        LocalDate date = LocalDate.now();
        Map<Integer, Double> result = service.calcAmount(userId, cart, date);
    
        assertEquals(2, result.size());
        assertEquals(30.0, result.get(storeId1), 0.001);
        assertEquals(70.0, result.get(storeId2), 0.001);
    }

    @Test
    void testCalcAmount_StoreNotFound_ShouldThrow() {
        int userId = 1;
        int storeId = 999;
        Cart cart = new Cart();
        cart.addProduct(storeId, 101, 2);

        when(mockStoreRepository.findById(storeId)).thenReturn(null);

        LocalDate date = LocalDate.now();
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            storeService.calcAmount(userId, cart, date);
        });
        assertEquals("Store not found", ex.getMessage());
    }

    @Test
    void testCalcAmount_EmptyCart_ReturnsEmptyMap() {
        int userId = 1;
        Cart cart = new Cart();
        LocalDate date = LocalDate.now();

        Map<Integer, Double> result = storeService.calcAmount(userId, cart, date);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
        @Test
        void testInit_Success() {
            // If init() is public and has no parameters
            assertDoesNotThrow(() -> storeService.init());
        
        // Optionally, verify side effects, e.g.:
        // assertNotNull(storeService.getSomeInitializedField());
        // assertTrue(storeService.isInitialized());
    }
    
    @Test
    void testInit_Idempotent() {
        // If calling init() multiple times is allowed, test that it doesn't throw
        storeService.init();
        assertDoesNotThrow(() -> storeService.init());
    }
    

    
    @Test
    void testViewStore_Success() {
        int storeId = 1;
        Store mockStore = mock(Store.class);
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);

        StoreDTO expectedDTO = mock(StoreDTO.class);
        StoreService spyService = spy(new StoreService(mockStoreRepository, publisher));
        doReturn(expectedDTO).when(spyService).toStoreDTO(mockStore);

        StoreDTO result = spyService.viewStore(storeId);

        assertEquals(expectedDTO, result);
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(spyService, times(1)).toStoreDTO(mockStore);
    }
    
    @Test
    void testViewStore_StoreNotFound_ShouldThrow() {
        int storeId = 999;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> storeService.viewStore(storeId));
        assertEquals("Store not found", ex.getMessage());
    }
    
    @Test
    void testGetAllStores_EmptyList() {
        when(mockStoreRepository.getAllStores()).thenReturn(Collections.emptyList());
    
        StoreService spyService = spy(storeService);
        List<StoreDTO> result = spyService.getAllStores();
    
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetAllStores_WithStores() {
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        List<Store> stores = Arrays.asList(store1, store2);

        when(mockStoreRepository.getAllStores()).thenReturn(stores);

        StoreService spyService = spy(new StoreService(mockStoreRepository, publisher));
        StoreDTO dto1 = mock(StoreDTO.class);
        StoreDTO dto2 = mock(StoreDTO.class);
        doReturn(dto1).when(spyService).toStoreDTO(store1);
        doReturn(dto2).when(spyService).toStoreDTO(store2);

        List<StoreDTO> result = spyService.getAllStores();

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
        verify(mockStoreRepository, times(1)).getAllStores();
        verify(spyService, times(1)).toStoreDTO(store1);
        verify(spyService, times(1)).toStoreDTO(store2);
    }
    
    @Test
    void testSearchStores_Found() {
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        when(store1.getName()).thenReturn("SuperMart");
        when(store2.getName()).thenReturn("MiniMart");
        List<Store> stores = Arrays.asList(store1, store2);
    
        when(mockStoreRepository.getAllStores()).thenReturn(stores);
    
        StoreService spyService = spy(new StoreService(mockStoreRepository, publisher));
        StoreDTO dto1 = mock(StoreDTO.class);
        StoreDTO dto2 = mock(StoreDTO.class);
        doReturn(dto1).when(spyService).toStoreDTO(store1);
        doReturn(dto2).when(spyService).toStoreDTO(store2);
    
        List<StoreDTO> result = spyService.searchStores("Mart");
    
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }
    @Test
    void testSearchStores_NotFound() {
        Store store1 = mock(Store.class);
        when(store1.getName()).thenReturn("SuperMart");
        List<Store> stores = Arrays.asList(store1);

        when(mockStoreRepository.getAllStores()).thenReturn(stores);

        StoreService spyService = spy(new StoreService(mockStoreRepository, publisher));
        StoreDTO dto1 = mock(StoreDTO.class);
        doReturn(dto1).when(spyService).toStoreDTO(store1);

        List<StoreDTO> result = spyService.searchStores("Electronics");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockStoreRepository, times(1)).getAllStores();
    }
    @Test
    void testRemoveStoreOwner_StoreNotFound_ShouldThrow() {
        int storeId = 999, requesterId = 1, ownerId = 2;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.removeStoreOwner(storeId, requesterId, ownerId));
    }
    
    @Test
    void testGetStoreRoles_StoreNotFound_ShouldThrow() {
        int storeId = 999, userId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.getStoreRoles(storeId, userId));
    }
    
    @Test
    void testAddStoreManager_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1, managerId = 2;
        List<StoreManagerPermission> perms = List.of();
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.addStoreManager(storeId, ownerId, managerId, perms));
    }
    
    @Test
    void testAddStoreManagerPermissions_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1, managerId = 2;
        List<StoreManagerPermission> perms = List.of();
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.addStoreManagerPermissions(storeId, ownerId, managerId, perms));
    }
    
    @Test
    void testRemoveStoreManagerPermissions_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1, managerId = 2;
        List<StoreManagerPermission> perms = List.of();
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.removeStoreManagerPermissions(storeId, ownerId, managerId, perms));
    }
    
    @Test
    void testRemoveStoreManager_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1, managerId = 2;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.removeStoreManager(storeId, ownerId, managerId));
    }

    
    @Test
    void testGetMessagesFromUsers_StoreNotFound_ShouldThrow() {
        int userId = 1, storeId = 999;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.getMessagesFromUsers(userId, storeId));
    }
    
    @Test
    void testIsStoreOpen_StoreNotFound_ShouldThrow() {
        int storeId = 999;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.isStoreOpen(storeId));
    }
    
    @Test
    void testGetMessagesFromStore_StoreNotFound_ShouldThrow() {
        int userId = 1, storeId = 999;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.getMessagesFromStore(userId, storeId));
    }
    
    @Test
    void testGetProductFromStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, productId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.getProductFromStore(storeId, productId));
    }
    
    @Test
    void testAddBidOnAuctionProductInStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, userId = 1, productId = 1;
        double bid = 100.0;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.addBidOnAuctionProductInStore(storeId, userId, productId, bid));
    }
    
    @Test
    void testDeclineAssignment_StoreNotFound_ShouldThrow() {
        int storeId = 999, userId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.declineAssignment(storeId, userId));
    }
    @Test
    void testCheckIfProductsInStores_StoreNotFound_ShouldThrow() {
        int storeId = 999;
        Map<Integer, Integer> products = Map.of(1, 2);
        Map<Integer, Map<Integer, Integer>> cart = Map.of(storeId, products);
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        int userId = 1;
        Exception ex = assertThrows(IllegalArgumentException.class, () -> storeService.checkIfProductsInStores(userId, cart));
        assertNotNull(ex);
    }
    
    @Test
    void testDecrementProductsInStores_StoreNotFound_ShouldThrow() {
        int userId = 1, storeId = 999;
        Map<Integer, Integer> products = Map.of(1, 2);
        Map<Integer, Map<Integer, Integer>> cart = Map.of(storeId, products);
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.decrementProductsInStores(userId, cart));
    }
    
    @Test
    void testReturnProductsToStores_StoreNotFound_ShouldThrow() {
        int userId = 1, storeId = 999;
        Map<Integer, Map<Integer, Integer>> returnMap = Map.of(storeId, Map.of(1, 2));
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> storeService.returnProductsToStores(userId, returnMap));
    }


    @Test
    void testAddProductToStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1;
        int productId = 123;
        String productName = "TestProduct";
        double price = 10.0;
        int quantity = 5;
        PCategory category = PCategory.ELECTRONICS;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);

        StoreService service = new StoreService(mockStoreRepository, publisher);
        assertThrows(IllegalArgumentException.class, () -> service.addProductToStore(storeId, ownerId, productId, productName, price, quantity, category));
    }
    
    @Test
    void testUpdateProductInStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1;
        int productId = 123;
        String productName = "TestProduct";
        double price = 10.0;
        int quantity = 5;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        assertThrows(IllegalArgumentException.class, () -> service.updateProductInStore(storeId, ownerId, productId, productName, price, quantity));
    }
    
    @Test
    void testRemoveProductFromStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, ownerId = 1, productId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        assertThrows(IllegalArgumentException.class, () -> service.removeProductFromStore(storeId, ownerId, productId));
    }
    
    @Test
    void testIsValidPurchaseActionForUserInStore_StoreNotFound_ShouldThrow() {
        int storeId = 999, userId = 1, productId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        assertThrows(IllegalArgumentException.class, () -> service.isValidPurchaseActionForUserInStore(storeId, userId,productId));
    }
    
    @Test
    void testGetPendingOwners_StoreNotFound_ShouldThrow() {
        int storeId = 999, requesterId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);
        assertThrows(IllegalArgumentException.class, () -> service.getPendingOwners(storeId, requesterId));
    }
    
    @Test
    void testGetPendingManagers_StoreNotFound_ShouldThrow() {
        int storeId = 999, requesterId = 1;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);
        assertThrows(IllegalArgumentException.class, () -> service.getPendingManagers(storeId, requesterId));
    }
    @Test
    void testCanViewOrders_Success() {
        int storeId = 1, userId = 2;
        Store mockStore = mock(Store.class);
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.canViewOrders(userId)).thenReturn(true);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        boolean result = service.canViewOrders(storeId, userId);
    
        assertTrue(result);
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).canViewOrders(userId);
    }
    
    @Test
    void testCanViewOrders_StoreNotFound_ShouldThrow() {
        int storeId = 999, userId = 2;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.canViewOrders(storeId, userId));
        assertEquals("Store not found", ex.getMessage());
        verify(mockStoreRepository, times(1)).findById(storeId);
    }
    @Test
    void testAddStoreProductRating_Success() {
        int storeId = 1, productId = 2, userId = 3;
        double rating = 4.5;
        String comment = "Great product!";
        Store mockStore = mock(Store.class);
    
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        assertDoesNotThrow(() -> service.addStoreProductRating(storeId, productId, userId, rating, comment));
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).addStoreProductRating(userId, productId, rating, comment);
    }
    
    @Test
    void testAddStoreProductRating_StoreNotFound_ShouldThrow() {
        int storeId = 999, productId = 2, userId = 3;
        double rating = 4.5;
        String comment = "Great product!";
    
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            service.addStoreProductRating(storeId, productId, userId, rating, comment)
        );
        assertEquals("Store not found", ex.getMessage());
        verify(mockStoreRepository, times(1)).findById(storeId);
    }
    @Test
    void testClearAllData_CallsRepository() {
        StoreService service = new StoreService(mockStoreRepository, publisher);
        service.clearAllData();
        verify(mockStoreRepository, times(1)).clearAllData();
    }
    @Test
    void testGetStoreProductRatings_Success() {
        int storeId = 1, productId = 2;
        Store mockStore = mock(Store.class);
        List<ProductRating> ratings = Arrays.asList(
            mock(ProductRating.class), mock(ProductRating.class)
        );
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.getStoreProductAllRatings(productId)).thenReturn(ratings);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        List<ProductRating> result = service.getStoreProductRatings(storeId, productId);
    
        assertEquals(ratings, result);
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).getStoreProductAllRatings(productId);
    }
    
    @Test
    void testGetStoreProductRatings_StoreNotFound_ShouldThrow() {
        int storeId = 999, productId = 2;
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
    
        StoreService service = new StoreService(mockStoreRepository, publisher);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.getStoreProductRatings(storeId, productId));
        assertEquals("Store not found", ex.getMessage());
        verify(mockStoreRepository, times(1)).findById(storeId);
    }

     @Test
    void testIsStoreOwner_Success() {
        int storeId = 42;
        int userId = 7;

        // Arrange: mock repository returns a store, and store.isOwner(...) returns true
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.isOwner(userId)).thenReturn(true);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        // Act & Assert
        assertTrue(service.isStoreOwner(storeId, userId));
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).isOwner(userId);
    }

    @Test
    void testIsStoreOwner_StoreNotFound_ShouldThrow() {
        int storeId = 99;
        int userId = 7;

        // Arrange: repository returns null
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.isStoreOwner(storeId, userId)
        );
        assertEquals("Store not found", ex.getMessage());
        verify(mockStoreRepository, times(1)).findById(storeId);
    }
    
    @Test
    void testIsStoreManager_Success_ReturnsPermissions() {
        int storeId = 100;
        int userId = 8;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);

        // Arrange: repository returns a store, and store.isManagerAndGetPerms(...) returns our perms
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.isManagerAndGetPerms(userId)).thenReturn(perms);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        // Act
        List<StoreManagerPermission> result = service.isStoreManager(storeId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(perms, result);
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).isManagerAndGetPerms(userId);
    }

    @Test
    void testIsStoreManager_NotManager_ReturnsNull() {
        int storeId = 100;
        int userId = 9;

        // Arrange: repository returns a store, but store.isManagerAndGetPerms(...) returns null
        when(mockStoreRepository.findById(storeId)).thenReturn(mockStore);
        when(mockStore.isManagerAndGetPerms(userId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        // Act
        List<StoreManagerPermission> result = service.isStoreManager(storeId, userId);

        // Assert
        assertNull(result);
        verify(mockStoreRepository, times(1)).findById(storeId);
        verify(mockStore, times(1)).isManagerAndGetPerms(userId);
    }

    @Test
    void testIsStoreManager_StoreNotFound_ShouldThrow() {
        int storeId = 1234;
        int userId = 9;

        // Arrange: repository returns null
        when(mockStoreRepository.findById(storeId)).thenReturn(null);
        StoreService service = new StoreService(mockStoreRepository, publisher);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.isStoreManager(storeId, userId)
        );
        assertEquals("Store not found", ex.getMessage());
        verify(mockStoreRepository, times(1)).findById(storeId);
    }

}
