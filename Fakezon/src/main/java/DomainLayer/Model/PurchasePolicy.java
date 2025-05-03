package DomainLayer.Model;

import java.time.LocalDate;

public abstract class PurchasePolicy {
    private int policyID;
    private String policyName;
    private String description;
    public PurchasePolicy(int policyID, String policyName, String description) {
        this.policyID = policyID;
        this.policyName = policyName;
        this.description = description;
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

    
    public abstract boolean canPurchase(LocalDate dob, int productID, int quantity); // may not be needing the whole user object, just the id and age

}
