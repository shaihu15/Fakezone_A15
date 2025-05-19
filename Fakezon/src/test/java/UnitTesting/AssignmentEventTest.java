package UnitTesting;
import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.AssignmentEvent;

class AssignmentEventTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 10;
        int userId = 20;
        RoleName roleName = RoleName.STORE_MANAGER;

        AssignmentEvent event = new AssignmentEvent(storeId, userId, roleName);

        assertEquals(storeId, event.getStoreId());
        assertEquals(userId, event.getUserId());
        assertEquals(roleName, event.getRoleName());
    }
}