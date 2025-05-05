package DomainLayer.Model;

public class DiscountCondition {
    private int triggerProductId;
    private int triggerQuantity;
    private int targetProductId;
    private int targetQuantity;

    public DiscountCondition(int triggerProductId, int triggerQuantity, int targetProductId, int targetQuantity) {
        this.triggerProductId = triggerProductId;
        this.triggerQuantity = triggerQuantity;
        this.targetProductId = targetProductId;
        this.targetQuantity = targetQuantity;

       
    }
    public boolean isApplicable(int quantity) {
        return quantity >= triggerQuantity;
    }

    
    public int getTriggerProductId() {
        return triggerProductId;
    }
    public int getTriggerQuantity() {
        return triggerQuantity;
    }

}
