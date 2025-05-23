package DomainLayer.Model;

import java.util.List;
import java.util.function.Predicate;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;

public class AndDiscount implements IDiscountPolicy {
    private int policyID;
    private List<Predicate<Cart>> conditions;
    private double percentage;
    private IDiscountScope scope;

    public AndDiscount(int policyID, List<Predicate<Cart>> conditions, double percentage, IDiscountScope scope) {
        this.policyID = policyID;
        this.conditions = conditions;
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
        return conditions.stream().allMatch(condition -> condition.test(cart));
    }

    @Override
    public int getPolicyID() {
        return policyID;
    }
}
