package DomainLayer.Model;

public abstract class PurchasePolicy {
    private String policyID;
    private String policyName;
    private String description;
    public PurchasePolicy(String policyID, String policyName, String description) {
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

    
    public abstract boolean canPurchase(PurchaseRequest purchaseRequest);

}
