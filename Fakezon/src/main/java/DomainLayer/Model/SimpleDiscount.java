package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SIMPLE")
public class SimpleDiscount extends BaseDiscountPolicy {
    
    // Default constructor for JPA
    protected SimpleDiscount() {
        super();
    }

    public SimpleDiscount(int policyID, double percentage, IDiscountScope scope) {
        super(policyID, percentage, scope);
    }

    @Override
    public double apply(Cart cart) {
        if (!isApplicable(cart)) {
            return 0;
        }
        return getScope().getEligibleAmount(cart) * getPercentage() / 100;
    }

    @Override
    public boolean isApplicable(Cart cart) {
        return DiscountCondition.alwaysTrue().test(cart);
    }
}
