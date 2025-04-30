package UnitTesting;
import static org.junit.jupiter.api.Assertions.*;
import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import InfrastructureLayer.Repositories.StoreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ApplicationLayer.DTO.StoreRolesDTO;

import DomainLayer.Enums.StoreManagerPermission;
import java.util.List;

import static org.mockito.Mockito.*;

class StoreServiceTest {

    private IStoreRepository storeRepository;
    private StoreService storeService;
    private Store mockStore;
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        storeRepository = new StoreRepository(); // Assuming StoreRepository is a concrete implementation of IStoreRepository
        storeService = new StoreService(storeRepository, publisher);
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
    @Test
    void testReceivingMessage_validStoreId_ShouldSucceed() {
        int founderId = 1;
        String storeName = "Test Store2";
        int storeId = storeService.addStore(founderId, storeName);

        // recieving message from user
        String message = "Hello, this is a test message.";
        storeService.receivingMessage(storeId, founderId, message);

        // Verify that the message was received successfully
        assertEquals(message, storeService.getMessagesFromUsers(founderId, storeId).peek().getValue(), "Message should be received successfully");
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
        assertEquals(message, storeService.getMessagesFromStore(founderId, storeId).peek().getValue(), "Message should be sent successfully");
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
        storeService.addStoreOwner(storeId,founderId, newOwnerId);// Add a new owner
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
        assertTrue(storeRolesDTO.getStoreOwners().contains(newOwnerId), "New owner should be in the list of store owners");
        // Check managers list and permissions
        assertTrue(storeRolesDTO.getStoreManagers().containsKey(managerId), "Manager should be in the list of store managers");
        assertEquals(permissions, storeRolesDTO.getStoreManagers().get(managerId), "Manager permissions should match the expected value");
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
        // String expectedMessagePart = "not authorized"; // Adjust based on your actual exception message
        // assertTrue(exception.getMessage().toLowerCase().contains(expectedMessagePart),
        //     "Exception message should indicate unauthorized access");
    }



}
