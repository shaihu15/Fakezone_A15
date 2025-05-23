package UnitTesting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import DomainLayer.Enums.RoleName;
import DomainLayer.Model.SystemAdministrator;

public class SystemAdministratorTest {

    @Test
    void testConstructor() {
        SystemAdministrator admin = new SystemAdministrator();
        // Just ensure object is created
        assertEquals(SystemAdministrator.class, admin.getClass());
    }

    @Test
    void testGetRoleName() {
        SystemAdministrator admin = new SystemAdministrator();
        assertEquals(RoleName.SYSTEM_ADMINISTRATOR, admin.getRoleName());
    }
}