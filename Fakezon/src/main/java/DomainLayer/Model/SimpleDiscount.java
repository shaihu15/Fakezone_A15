package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;

public class SimpleDiscount implements IDiscountPolicy {
    private int policyID;
    private double percentage;
    private IDiscountScope scope;

    public SimpleDiscount(int policyID, double percentage, IDiscountScope scope) {
        this.policyID = policyID;
        this.percentage = percentage;
        this.scope = scope;
    }

    @Override
    public double apply(Cart cart) {
        if (!isApplicable(cart)) {
            return 0;
        }
        return scope.getEligibleAmount(cart) * percentage / 100;
    }

    @Override
    public boolean isApplicable(Cart cart) {
        return DiscountCondition.alwaysTrue().test(cart);
    }

    @Override
    public int getPolicyID() {
        return policyID;
    }
}
