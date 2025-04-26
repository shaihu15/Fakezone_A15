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
    private int managerId = 3;

    private int productId = 10;
    private int nonExistingProductId = 99;

    @BeforeEach
    void setUp(){
        store = new Store("Test Store", founderId);
        store.addStoreProduct(productId, "Test Product", 100.0, 5);

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
        store.addStoreProduct(productId, "Test Product", 10.0, 100);
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

    @Test
    void addAuctionProduct_AsOwner_Success() {
        assertDoesNotThrow(() -> store.addAuctionProduct(founderId, productId, 50.0, 7));
    }

    @Test
    void addAuctionProduct_NonExistingProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, nonExistingProductId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("does not exist"));
    }

    @Test
    void addAuctionProduct_NotAuthorizedUser_Fails() {
        int randomUserId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(randomUserId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("insufficient permissions"));
    }
    @Test
    void addAuctionProduct_AsManagerWithInventoryPermission_Success() {
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.INVENTORY));
        assertDoesNotThrow(() -> store.addAuctionProduct(managerId, productId, 50.0, 7));
    }

    @Test
    void addAuctionProduct_ManagerWithoutInventoryPermission_Fails() {
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.REQUESTS_REPLY));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(managerId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("insufficient permissions"));
    }
    @Test
    void addAuctionProduct_AlreadyExists_Fails() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("already"));
    }
    @Test
    void addAuctionProduct_ZeroDays_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, 0);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }
    @Test
    void addAuctionProduct_NegativeDays_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, -1);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }
    @Test
    void addAuctionProduct_NegativeBasePrice_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, -50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }

    @Test
    void addBidOnAuctionProduct_SuccessfulBid() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        boolean success = store.addBidOnAuctionProduct(founderId, productId, 55.0);
        assertTrue(success);
    }

    @Test
    void addBidOnAuctionProduct_NonExistingAuctionProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addBidOnAuctionProduct(founderId, nonExistingProductId, 60.0);
        });
        assertTrue(thrown.getMessage().contains("does not exist"));
    }

    @Test
    void isValidPurchaseAction_SuccessfulFlow() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        store.addBidOnAuctionProduct(founderId, productId, 60.0);
        assertDoesNotThrow(() -> store.isValidPurchaseAction(founderId, productId));
    }

    @Test
    void isValidPurchaseAction_WrongBidder_Fails() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        store.addBidOnAuctionProduct(founderId, productId, 60.0);

        int otherUserId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.isValidPurchaseAction(otherUserId, productId);
        });
        assertTrue(thrown.getMessage().contains("is not the highest bidder"));
    }

    @Test
    void isValidPurchaseAction_NonAuctionProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.isValidPurchaseAction(founderId, nonExistingProductId);
        });
        assertTrue(thrown.getMessage().contains("is not an auction product"));
    }


}

