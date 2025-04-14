package DomainLayer.Model;

public abstract class DiscountPolicy {
    private String policyID;
    private String policyName;
    private String description;

    public DiscountPolicy(String policyID, String policyName, String description) {
        this.policyID = policyID;
        this.policyName = policyName;
        this.description = description;
    }

    public String getPolicyID() {
        return policyID;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getDescription() {
        return description;
    }

    public abstract double calculateDiscount(double basePrice, int quantity); // Calculate the discount based on the base price and quantity
    public abstract boolean isApplicable(int quantity); // Check if the discount policy is applicable based on the quantity of products being purchased
    
}
