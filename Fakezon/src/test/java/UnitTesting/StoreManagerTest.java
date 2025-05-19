package UnitTesting;

import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.StoreManager;

class StoreManagerTest {

    @Test
    void testConstructorAndGetRoleName() {
        StoreManager manager = new StoreManager();
        assertEquals(RoleName.STORE_MANAGER, manager.getRoleName());
    }
}