package ApplicationLayer.DTO;


import com.fasterxml.jackson.annotation.JsonProperty;

import ApplicationLayer.Enums.PCategory;
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

    @JsonProperty("category")
    private final PCategory category; // Assuming category is a string representation

    @JsonProperty("averageRating")
    private final double averageRating;
    // Add no-args constructor with default values for final fields
    public StoreProductDTO() {
        this.productId = 0;
        this.name = null;
        this.basePrice = 0.0;
        this.quantity = 0;
        this.storeId = 0;
        this.category = null;
        this.averageRating = 0.0;
    }

    public StoreProductDTO(int productId, String name, double basePrice, int quantity,
            double averageRating, int storeId, PCategory category) {   
        this.category = category;
        this.storeId = storeId;
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.averageRating = averageRating;
    }

    public StoreProductDTO(StoreProduct storeProduct) {
        this.productId = storeProduct.getSproductID();
        this.storeId = storeProduct.getStoreId();
        this.name = storeProduct.getName();
        this.basePrice = storeProduct.getBasePrice();
        this.quantity = storeProduct.getQuantity();
        this.averageRating = storeProduct.getAverageRating(); // Assuming getAverageRating() returns a double
        this.category = storeProduct.getCategory(); // Assuming StoreProduct has a method to get category
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
    public PCategory getCategory() {
        return category;
    }
}
