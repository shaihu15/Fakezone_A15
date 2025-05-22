package UnitTesting;

import DomainLayer.Model.UnassignedRole;

import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnassignedRoleTest {

    @Test
    void testConstructorAndGetRoleName() {
        UnassignedRole role = new UnassignedRole();
        assertEquals(RoleName.UNASSIGNED, role.getRoleName());
    }
}
