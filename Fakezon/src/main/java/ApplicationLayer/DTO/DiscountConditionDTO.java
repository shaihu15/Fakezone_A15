package ApplicationLayer.DTO;

public class DiscountConditionDTO {
    private String type;
    private Double threshold;
    private Integer productId;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
} 