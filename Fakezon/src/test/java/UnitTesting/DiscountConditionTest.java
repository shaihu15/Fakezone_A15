package UnitTesting;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Model.DiscountCondition; 

class DiscountConditionTest {

    @Test
    void testConstructorAndGetters() {
        int triggerProductId = 1;
        int triggerQuantity = 2;
        int targetProductId = 3;
        int targetQuantity = 4;

        DiscountCondition condition = new DiscountCondition(triggerProductId, triggerQuantity, targetProductId, targetQuantity);

        assertEquals(triggerProductId, condition.getTriggerProductId());
        assertEquals(triggerQuantity, condition.getTriggerQuantity());
    }

    @Test
    void testIsApplicable() {
        DiscountCondition condition = new DiscountCondition(1, 5, 2, 3);

        assertFalse(condition.isApplicable(4));
        assertTrue(condition.isApplicable(5));
        assertTrue(condition.isApplicable(6));
    }
}