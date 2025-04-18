package UnitTesting;
import DomainLayer.Model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {
    private Store store;
    private int founderId = 10;
    private int storeId = 1;

    @BeforeEach
    void setUp(){
        store = new Store("Test Store", storeId, founderId);
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

        IllegalAccessError thrown = assertThrows(
                IllegalAccessError.class,
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

        assertTrue(thrown.getMessage().contains("Product with ID: " + productId + " does not exist in store ID: " + storeId));
        assertThrows(
                IllegalArgumentException.class,
                () -> store.getStoreProductRating(userId, productId),
                "Expected getStoreProductRating to throw if the product is not found"
        );
    }


}
