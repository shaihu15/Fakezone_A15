package DomainLayer.Model;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("OR")
public class OrDiscount extends BaseDiscountPolicy {
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_policy_id")
    private List<DiscountConditionEntity> conditionEntities;
    
    @Transient
    private List<Predicate<Cart>> conditions;
    
    // Default constructor for JPA
    protected OrDiscount() {
        super();
    }

    public OrDiscount(int policyID, List<Predicate<Cart>> conditions, double percentage, IDiscountScope scope) {
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
        if (conditions != null) {
            return conditions.stream().anyMatch(condition -> condition.test(cart));
        } else if (conditionEntities != null) {
            // Reconstruct conditions from entities
            conditions = conditionEntities.stream()
                .map(DiscountConditionEntity::toPredicate)
                .collect(Collectors.toList());
            return conditions.stream().anyMatch(condition -> condition.test(cart));
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
