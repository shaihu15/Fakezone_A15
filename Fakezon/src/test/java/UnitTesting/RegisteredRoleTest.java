package UnitTesting;

import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.RegisteredRole;

class RegisteredRoleTest {

    // Concrete subclass for testing the abstract RegisteredRole
    static class TestRegisteredRole extends RegisteredRole {
        @Override
        public RoleName getRoleName() {
            return RoleName.UNASSIGNED;
        }
    }

    @Test
    void testConstructorAndGetRoleName() {
        RegisteredRole role = new TestRegisteredRole();
        assertEquals(RoleName.UNASSIGNED, role.getRoleName());
    }
}
