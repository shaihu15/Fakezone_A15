package UnitTesting;


import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.DiscountPolicy;
import DomainLayer.Model.DiscountCondition;
class DiscountPolicyTest {

    // Concrete subclass for testing the abstract DiscountPolicy
    static class TestDiscountPolicy extends DiscountPolicy {
        public TestDiscountPolicy(int policyID, String policyName, String description, List<DiscountCondition> conditions, double discountPrecentegeAmount) {
            super(policyID, policyName, description, conditions, discountPrecentegeAmount);
        }

        @Override
        public double calculateNewPrice(double basePrice, int quantity) {
            // Simple implementation for testing
            return basePrice * quantity * 0.9;
        }

        @Override
        public boolean isApplicable(int quantity) {
            // Simple implementation for testing
            return quantity > 0;
        }
    }

    @Test
    void testConstructorAndGetters() {
        int policyID = 1;
        String policyName = "Test Policy";
        String description = "Test Description";
        DiscountCondition condition = new DiscountCondition(1, 2, 3, 4);
        List<DiscountCondition> conditions = Collections.singletonList(condition);
        double discountPrecentegeAmount = 0.15;

        DiscountPolicy policy = new TestDiscountPolicy(policyID, policyName, description, conditions, discountPrecentegeAmount);

        assertEquals(policyID, policy.getPolicyID());
        assertEquals(policyName, policy.getPolicyName());
        assertEquals(description, policy.getDescription());
        assertEquals(conditions, policy.getConditions());
    }

    @Test
    void testAbstractMethods() {
        DiscountPolicy policy = new TestDiscountPolicy(1, "Policy", "Desc", Collections.emptyList(), 0.1);

        assertEquals(9.0, policy.calculateNewPrice(10.0, 1));
        assertTrue(policy.isApplicable(1));
        assertFalse(policy.isApplicable(0));
    }
}