package DomainLayer.Model;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("AND")
public class AndDiscount extends BaseDiscountPolicy {
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_policy_id")
    private List<DiscountConditionEntity> conditionEntities;
    
    @Transient
    private List<Predicate<Cart>> conditions;
    
    // Default constructor for JPA
    protected AndDiscount() {
        super();
    }

    public AndDiscount(int policyID, List<Predicate<Cart>> conditions, double percentage, IDiscountScope scope) {
        super(policyID, percentage, scope);
        this.conditions = conditions;
        // For now, we'll need to convert conditions manually when persisting
        // This would be done in the Store class when saving
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
        if (conditions != null) {
            return conditions.stream().allMatch(condition -> condition.test(cart));
        } else if (conditionEntities != null) {
            // Reconstruct conditions from entities
            conditions = conditionEntities.stream()
                .map(DiscountConditionEntity::toPredicate)
                .collect(Collectors.toList());
            return conditions.stream().allMatch(condition -> condition.test(cart));
        }
        return false;
    }
    
    public void setConditionEntities(List<DiscountConditionEntity> conditionEntities) {
        this.conditionEntities = conditionEntities;
        // Clear transient conditions to force reconstruction
        this.conditions = null;
    }
    
    public List<DiscountConditionEntity> getConditionEntities() {
        return conditionEntities;
    }
}
