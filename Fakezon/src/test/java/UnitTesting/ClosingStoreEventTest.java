package UnitTesting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.ClosingStoreEvent;

class ClosingStoreEventTest {

    @Test
    void testConstructorAndGetId() {
        int storeId = 123;
        ClosingStoreEvent event = new ClosingStoreEvent(storeId);
        assertEquals(storeId, event.getId());
    }
}