package UnitTesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;

class ResponseFromStoreEventTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 5;
        int userId = 10;
        String message = "Test message";

        ResponseFromStoreEvent event = new ResponseFromStoreEvent(storeId, userId, message);

        assertEquals(storeId, event.getStoreId());
        assertEquals(userId, event.getUserId());
        assertEquals(message, event.getMessage());
    }
}