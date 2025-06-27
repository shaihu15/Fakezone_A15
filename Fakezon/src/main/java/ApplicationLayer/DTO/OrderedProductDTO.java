package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderedProductDTO {
    @JsonProperty("productId")
    private final int productId;
    @JsonProperty("name")
    private final String name;
    @JsonProperty("price")
    private final double price;
    @JsonProperty("quantity")
    private final int quantity;

    @JsonCreator
    public OrderedProductDTO(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
