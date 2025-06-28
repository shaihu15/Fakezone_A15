package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

@Entity
@Table(name = "discount_policies")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "policy_type", discriminatorType = DiscriminatorType.STRING)
public abstract class BaseDiscountPolicy implements IDiscountPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private int policyID;
    
    @Column(name = "percentage")
    private double percentage;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "scope_id")
    private BaseDiscountScope scope;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Default constructor for JPA
    protected BaseDiscountPolicy() {
    }
    
    public BaseDiscountPolicy(int policyID, double percentage, IDiscountScope scope) {
        // Note: policyID parameter is ignored for auto-generated IDs
        this.percentage = percentage;
        this.scope = (BaseDiscountScope) scope;
    }
    
    @Override
    public int getPolicyID() {
        return policyID;
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    public IDiscountScope getScope() {
        return scope;
    }
    
    public void setScope(BaseDiscountScope scope) {
        this.scope = scope;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
} 