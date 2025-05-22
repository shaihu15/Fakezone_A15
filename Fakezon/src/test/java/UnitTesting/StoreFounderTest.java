package UnitTesting;
import DomainLayer.Enums.RoleName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.StoreFounder;

class StoreFounderTest {

    @Test
    void testConstructorAndGetRoleName() {
        StoreFounder founder = new StoreFounder();
        assertEquals(RoleName.STORE_FOUNDER, founder.getRoleName());
    }
}