package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;
import java.util.function.Predicate;

@Entity
@DiscriminatorValue("CONDITION")
public class ConditionDiscount extends BaseDiscountPolicy {
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "condition_entity_id")
    private DiscountConditionEntity conditionEntity;
    
    @Transient
    private Predicate<Cart> condition;
    
    // Default constructor for JPA
    protected ConditionDiscount() {
        super();
    }

    public ConditionDiscount(int policyID, double percentage, IDiscountScope scope, Predicate<Cart> condition) {  
        super(policyID, percentage, scope);
        this.condition = condition;
    }

    @Override
    public double apply(Cart cart) {
        Predicate<Cart> conditionToUse = getCondition();
        if (!conditionToUse.test(cart)) {
            return 0;
        }
        return getScope().getEligibleAmount(cart) * getPercentage() / 100;
    }

    @Override
    public boolean isApplicable(Cart cart) {
        return getCondition().test(cart);
    }
    
    private Predicate<Cart> getCondition() {
        if (condition != null) {
            return condition;
        } else if (conditionEntity != null) {
            // Reconstruct condition from entity
            condition = conditionEntity.toPredicate();
            return condition;
        }
        // Default fallback
        return DiscountCondition.alwaysTrue();
    }
    
    public void setConditionEntity(DiscountConditionEntity conditionEntity) {
        this.conditionEntity = conditionEntity;
        this.condition = null;
    }
    
    public DiscountConditionEntity getConditionEntity() {
        return conditionEntity;
    }
}
