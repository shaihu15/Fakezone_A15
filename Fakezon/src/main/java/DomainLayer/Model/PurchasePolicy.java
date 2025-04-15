package DomainLayer.Model;

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

    
    public abstract boolean canPurchase(int userID, String productID, int quantity); // TO DO might change to some generic object like requestPurchase

}
