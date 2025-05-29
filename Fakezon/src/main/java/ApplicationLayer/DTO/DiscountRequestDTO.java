package ApplicationLayer.DTO;

import java.util.List;

public class DiscountRequestDTO {
    private String discountType; // "SIMPLE", "CONDITION", "AND", "OR", "XOR"
    private String scope; // "PRODUCTS", "STORE"
    private List<Integer> productIds;
    private List<DiscountConditionDTO> conditions;
    private double percentage;

    // Getters and setters
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public List<Integer> getProductIds() { return productIds; }
    public void setProductIds(List<Integer> productIds) { this.productIds = productIds; }
    public List<DiscountConditionDTO> getConditions() { return conditions; }
    public void setConditions(List<DiscountConditionDTO> conditions) { this.conditions = conditions; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
} 