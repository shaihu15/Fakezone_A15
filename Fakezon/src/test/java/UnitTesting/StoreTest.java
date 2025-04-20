package UnitTesting;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.management.ManagementPermission;
import java.util.List;

public class StoreTest {
    private Store store;
    private int founderId = 10;
    private int storeId = 1;

    @BeforeEach
    void setUp(){
        store = new Store("Test Store", founderId);
    }

    @Test
    void closeStore_AsFounder_ShouldSucceed() {
        assertTrue(store.isOpen());
        store.closeStore(founderId);

        assertFalse(store.isOpen(), "Store should be closed by founder");
    }

    @Test
    void closeStore_AlreadyClosed_ShouldThrow() {
        store.closeStore(founderId);
        assertFalse(store.isOpen());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.closeStore(founderId),
                "Expected closeStore to throw if the store is already closed"
        );

        assertTrue(thrown.getMessage().contains("already closed"));
    }


    @Test
    void closeStore_NotFounder_ShouldThrow() {
        int nonFounderId = 99;

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
                () -> store.closeStore(nonFounderId),
                "Expected closeStore to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Requester ID: " + nonFounderId));
        assertTrue(store.isOpen(), "Store should still be open if close failed");
    }
    @Test
    void addRating_ValidRating_ShouldSucceed() {
        int userId = 1;
        double rating = 4.5;
        String comment = "Great product!";

        store.addRating(userId, rating, comment);

        assertEquals(rating, store.getStoreRatingByUser(userId).getRating(), "Rating should be added successfully");
    }
    @Test
    void addStoreProductRating_ValidRating_ShouldSucceed() {
        int userId = 1;
        int productId = 1;
        store.addStoreProduct(productId, "Test Product", 10.0, 100, null); // Assuming this method exists to add a product
        double rating = 4.5;
        String comment = "Great product!";

        store.addStoreProductRating(userId, productId, rating, comment);

        assertEquals(rating, store.getStoreProductRating(userId, productId).getRating(), "Product rating should be added successfully");
    }
    @Test
    void addStoreProductRating_ProductNotFound_ShouldThrow() {
        int userId = 1;
        int productId = 99; // Assuming this product does not exist
        double rating = 4.5;
        String comment = "Great product!";

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.addStoreProductRating(userId, productId, rating, comment),
                "Expected addStoreProductRating to throw if the product is not found"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> store.getStoreProductRating(userId, productId),
                "Expected getStoreProductRating to throw if the product is not found"
        );
    }
    
    // Test receiving message from user
    @Test
    void receivingMessageFromUser_ValidMessage_ShouldSucceed() {
        int userId = 1;
        String message = "Hello, this is a test message.";

        store.receivingMessage(userId, message);

        assertEquals(message, store.getMessagesFromUsers(founderId).peek().getValue(), "Message should be received successfully");
    }
    // Test sending message to user
    @Test
    void sendMessageToUser_ValidOwner_ShouldSucceed() {
        int userId = 1;
        String message = "Hello, this is a test message.";
        store.sendMessage(founderId,userId, message);

        assertEquals(message, store.getMessagesFromStore(founderId).peek().getValue(), "Message should be sent successfully");
    }
    @Test
    void sendMessageToUser_ValidManagerPermission_ShouldSucceed() {
        int userId = 1;
        int managerId = 2;
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.REQUESTS_REPLY)); // Assuming null permissions for simplicity
        String message = "Hello, this is a test message.";
        store.sendMessage(managerId,userId, message);

        assertEquals(message, store.getMessagesFromStore(founderId).peek().getValue(), "Message should be sent successfully");
    }
    @Test
    void sendMessageToUser_InValidManagerPermission_ShouldThrow() {
        int userId = 1;
        int managerId = 2;
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.DISCOUNT_POLICY)); // Assuming null permissions for simplicity
        String message = "Hello, this is a test message.";

        IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> store.sendMessage(managerId, userId, message),
            "Expected sendMessage to throw if the owner is invalid"
    );
}
    @Test
    void sendMessageToUser_InvalidOwner_ShouldThrow() {
        int invalidOwnerId = 99;
        int userId = 1;
        String message = "Hello, this is a test message.";

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.sendMessage(invalidOwnerId, userId, message),
                "Expected sendMessage to throw if the owner is invalid"
        );

        assertTrue(store.getMessagesFromStore(founderId).isEmpty(), "No message should be sent if the owner is invalid");
    }



}
