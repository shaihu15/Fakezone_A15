package DomainLayer.Model;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("XOR")
public class XorDiscount extends BaseDiscountPolicy {
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_policy_id")
    private List<DiscountConditionEntity> conditionEntities;
    
    @Transient
    private List<Predicate<Cart>> conditions;
    
    // Default constructor for JPA
    protected XorDiscount() {
        super();
    }

    public XorDiscount(int policyID, List<Predicate<Cart>> conditions, double percentage, IDiscountScope scope) {
        super(policyID, percentage, scope);
        this.conditions = conditions;
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
        List<Predicate<Cart>> conditionsToUse = conditions;
        if (conditionsToUse == null && conditionEntities != null) {
            // Reconstruct conditions from entities
            conditionsToUse = conditionEntities.stream()
                .map(DiscountConditionEntity::toPredicate)
                .collect(Collectors.toList());
            this.conditions = conditionsToUse;
        }
        
        if (conditionsToUse != null) {
            long trueConditions = conditionsToUse.stream()
                .mapToLong(condition -> condition.test(cart) ? 1 : 0)
                .sum();
            return trueConditions == 1;
        }
        return false;
    }
    
    public void setConditionEntities(List<DiscountConditionEntity> conditionEntities) {
        this.conditionEntities = conditionEntities;
        this.conditions = null;
    }
    
    public List<DiscountConditionEntity> getConditionEntities() {
        return conditionEntities;
    }
} 
