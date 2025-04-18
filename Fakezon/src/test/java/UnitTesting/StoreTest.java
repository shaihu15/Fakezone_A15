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
}
