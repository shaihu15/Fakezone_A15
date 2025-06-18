package UnitTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DomainLayer.Model.Registered;
import DomainLayer.Model.helpers.StoreMsg;

public class RemoveAssignmentMessageTest {
    private Registered registered;
    private final String email = "user@example.com";
    private final String password = "pass123";
    private final LocalDate dob = LocalDate.of(2000, 1, 1);
    private final String state = "IL";

    @BeforeEach
    void setUp() {
        registered = new Registered(email, password, dob, state);
    }

    @Test
    void testRemoveAssignmentMessage_RemovesExisting() {
        // Add a message and ensure it's present
        int msgId = registered.addAssignmentMessage(new StoreMsg(10, -1, "Test message", null));
        Map<Integer, StoreMsg> before = registered.getAssignmentMessages();
        assertTrue(before.containsKey(msgId), "Assignment message should be present before removal");

        // Invoke removal
        registered.removeMsgById(msgId);

        // Verify it's removed
        Map<Integer, StoreMsg> after = registered.getAssignmentMessages();
        assertFalse(after.containsKey(msgId), "Assignment message should be removed");
    }

    @Test
    void testRemoveAssignmentMessage_MultipleKeepsOthers() {
        // Add two messages
        int msgId1 = registered.addAssignmentMessage(new StoreMsg(1, -1, "first", null));
        int msgId2 = registered.addAssignmentMessage(new StoreMsg(2, -1, "second", null));

        // Remove one
        registered.removeMsgById(msgId1);

        // Verify only the other remains
        Map<Integer, StoreMsg> result = registered.getAssignmentMessages();
        assertFalse(result.containsKey(msgId1), "First message should be removed");
        assertTrue(result.containsKey(msgId2), "Second message should remain");
        assertEquals(1, result.size(), "Only one message should remain");
    }

    @Test
    void testRemoveAssignmentMessage_NonExisting_NoChange() {
        // Add a message for storeId 3
        int msgId = registered.addAssignmentMessage(new StoreMsg(3, -1, "only-message", null));

        // Attempt to remove a non-existing storeId
        registered.removeMsgById(msgId+1);

        // Verify original message still present
        Map<Integer, StoreMsg> result = registered.getAssignmentMessages();
        assertTrue(result.containsKey(msgId), "Original assignment message should remain");
        assertEquals(1, result.size(), "Size should remain unchanged");
    }
    
}
