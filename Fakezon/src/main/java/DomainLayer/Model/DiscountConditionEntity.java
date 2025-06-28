package DomainLayer.Model;

import jakarta.persistence.*;
import java.util.function.Predicate;

@Entity
@Table(name = "discount_conditions")
public class DiscountConditionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;
    
    @Column(name = "condition_type")
    private String conditionType; // "ALWAYS_TRUE", "CONTAINS_PRODUCT", "TOTAL_ABOVE"
    
    @Column(name = "parameter_value")
    private String parameterValue; // Store parameters as string (e.g., productId, threshold)
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Default constructor for JPA
    protected DiscountConditionEntity() {
    }
    
    public DiscountConditionEntity(String conditionType, String parameterValue) {
        this.conditionType = conditionType;
        this.parameterValue = parameterValue;
    }
    
    // Factory method to create from a known condition type
    public static DiscountConditionEntity fromPredicate(String type, String parameter) {
        return new DiscountConditionEntity(type, parameter);
    }
    
    // Convert back to Predicate<Cart>
    public Predicate<Cart> toPredicate() {
        switch (conditionType) {
            case "ALWAYS_TRUE":
                return DiscountCondition.alwaysTrue();
            case "CONTAINS_PRODUCT":
                return DiscountCondition.containsProduct(Integer.parseInt(parameterValue));
            // Add more cases as needed
            default:
                return DiscountCondition.alwaysTrue(); // Default fallback
        }
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public String getConditionType() {
        return conditionType;
    }
    
    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }
    
    public String getParameterValue() {
        return parameterValue;
    }
    
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
} 