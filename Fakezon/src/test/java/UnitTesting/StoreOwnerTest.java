package UnitTesting;

import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Model.StoreOwner;

class StoreOwnerTest {

    @Test
    void testConstructorAndGetRoleName() {
        StoreOwner owner = new StoreOwner();
        assertEquals(RoleName.STORE_OWNER, owner.getRoleName());
    }
}