package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import java.util.function.Predicate;
public class ConditionDiscount implements IDiscountPolicy {
    private int policyID;
    private double percentage;
    private IDiscountScope scope;
    private Predicate<Cart> condition;

    public ConditionDiscount(int policyID, double percentage, IDiscountScope scope, Predicate<Cart> condition) {  
        this.policyID = policyID;
        this.percentage = percentage;
        this.scope = scope;
        this.condition = condition;
    }

    @Override
    public double apply(Cart cart) {
        if (!condition.test(cart)) {
            return 0;
        }
        return scope.getEligibleAmount(cart) * percentage / 100;
    }

    @Override
    public boolean isApplicable(Cart cart) {
        return condition.test(cart);
    }

    @Override
    public int getPolicyID() {
        return policyID;
    }
}
