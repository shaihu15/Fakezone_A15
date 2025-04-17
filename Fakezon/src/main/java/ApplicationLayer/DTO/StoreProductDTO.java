package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
public class StoreProductDTO {

    @JsonProperty("productId")
    private final int productId;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("basePrice")
    private final double basePrice;

    @JsonProperty("quantity")
    private final int quantity;

    @JsonProperty("state")
    private final String state;

    @JsonProperty("averageRating")
    private final double averageRating;

    public StoreProductDTO(int productId, String name, double basePrice, int quantity, String state, double averageRating) {
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.state = state;
        this.averageRating = averageRating;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getState() {
        return state;
    }

    public double getAverageRating() {
        return averageRating;
    }
}


