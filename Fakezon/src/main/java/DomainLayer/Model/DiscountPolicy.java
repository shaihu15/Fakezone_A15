package DomainLayer.Model;

import java.util.List;

public abstract class DiscountPolicy {
    private int policyID;
    private String policyName;
    private String description;
    private List<DiscountCondition> conditions; // Condition object to check if the discount policy is applicable
    private double discountPrecentegeAmount;
    public DiscountPolicy(int policyID, String policyName, String description,List<DiscountCondition> conditions, double discountPrecentegeAmount) {
        this.policyID = policyID;
        this.policyName = policyName;
        this.description = description;
        this.conditions = conditions;
        this.discountPrecentegeAmount = discountPrecentegeAmount;

    }

    public int getPolicyID() {
        return policyID;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getDescription() {
        return description;
    }

    public List<DiscountCondition> getConditions() {
        return conditions;
    }

    public abstract double calculateNewPrice(double basePrice, int quantity); // Calculate the discount based on the base price and quantity
    public abstract boolean isApplicable(int quantity); // Check if the discount policy is applicable based on the quantity of products being purchased
    
}
