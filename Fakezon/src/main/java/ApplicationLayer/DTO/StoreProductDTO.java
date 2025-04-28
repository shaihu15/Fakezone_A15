package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import DomainLayer.Model.StoreProduct;

public class StoreProductDTO {

    @JsonProperty("productId")
    private final int productId;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("basePrice")
    private final double basePrice;

    @JsonProperty("quantity")
    private final int quantity;

    @JsonProperty("storeId")
    private final int storeId;

    @JsonProperty("averageRating")
    private final double averageRating;

    public StoreProductDTO(int productId, String name, double basePrice, int quantity,
            double averageRating, int storeId) {
        this.storeId = storeId;
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.averageRating = averageRating;
    }

    public StoreProductDTO(StoreProduct storeProduct) {
        this.productId = storeProduct.getSproductID();
        this.name = storeProduct.getName();
        this.basePrice = storeProduct.getBasePrice();
        this.quantity = storeProduct.getQuantity();
        this.averageRating = storeProduct.getAverageRating(); // Assuming getAverageRating() returns a double
        this.storeId = storeProduct.getStoreId(); // Assuming StoreProduct has a method to get store ID
    }

    public int getStoreId() {
        return storeId;
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

    public double getAverageRating() {
        return averageRating;
    }
}
