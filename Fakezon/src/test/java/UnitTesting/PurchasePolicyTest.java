package UnitTesting;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.PurchasePolicy;

class PurchasePolicyTest {

    // Concrete subclass for testing the abstract PurchasePolicy
    static class TestPurchasePolicy extends PurchasePolicy {
        public TestPurchasePolicy(int policyID, String policyName, String description) {
            super(policyID, policyName, description);
        }

        @Override
        public boolean canPurchase(LocalDate dob, int productID, int quantity) {
            // Simple implementation for testing
            return quantity > 0;
        }
    }

    @Test
    void testConstructorAndGetters() {
        int policyID = 1;
        String policyName = "Test Policy";
        String description = "Test Description";

        PurchasePolicy policy = new TestPurchasePolicy(policyID, policyName, description);

        assertEquals(policyID, policy.getPolicyID());
        assertEquals(policyName, policy.getPolicyName());
        assertEquals(description, policy.getDescription());
    }

    @Test
    void testCanPurchase() {
        PurchasePolicy policy = new TestPurchasePolicy(1, "Policy", "Desc");
        assertTrue(policy.canPurchase(LocalDate.now(), 10, 1));
        assertFalse(policy.canPurchase(LocalDate.now(), 10, 0));
    }
}
