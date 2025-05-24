package UnitTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import DomainLayer.Model.Registered;

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
        registered.AssignmentMessages(new SimpleEntry<>(10, "test-message"));
        Map<Integer, String> before = registered.getAssignmentMessages();
        assertTrue(before.containsKey(10), "Assignment message should be present before removal");

        // Invoke removal
        registered.removeAssignmentMessage(10);

        // Verify it's removed
        Map<Integer, String> after = registered.getAssignmentMessages();
        assertFalse(after.containsKey(10), "Assignment message should be removed");
    }

    @Test
    void testRemoveAssignmentMessage_MultipleKeepsOthers() {
        // Add two messages
        registered.AssignmentMessages(new SimpleEntry<>(1, "first"));
        registered.AssignmentMessages(new SimpleEntry<>(2, "second"));

        // Remove one
        registered.removeAssignmentMessage(1);

        // Verify only the other remains
        Map<Integer, String> result = registered.getAssignmentMessages();
        assertFalse(result.containsKey(1), "First message should be removed");
        assertTrue(result.containsKey(2), "Second message should remain");
        assertEquals(1, result.size(), "Only one message should remain");
    }

    @Test
    void testRemoveAssignmentMessage_NonExisting_NoChange() {
        // Add a message for storeId 3
        registered.AssignmentMessages(new SimpleEntry<>(3, "only-message"));

        // Attempt to remove a non-existing storeId
        registered.removeAssignmentMessage(4);

        // Verify original message still present
        Map<Integer, String> result = registered.getAssignmentMessages();
        assertTrue(result.containsKey(3), "Original assignment message should remain");
        assertEquals(1, result.size(), "Size should remain unchanged");
    }
    
}
