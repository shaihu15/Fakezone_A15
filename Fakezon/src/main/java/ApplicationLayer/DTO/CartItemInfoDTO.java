package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItemInfoDTO {
    private int storeId;
    private int productId;
    private String storeName; // Optional: Convenient for display
    private String productName; // Optional: Convenient for display
    private int quantityInCart;
    private boolean inStock;
    private double unitPrice; // Optional: Price per unit at the time of adding to cart or current price
    private boolean isAuction;
    // No-arg constructor (needed by Jackson)
    public CartItemInfoDTO() {
    }

    // All-args constructor (optional, good for server-side creation)
    @JsonCreator // If using this for deserialization with final fields or specific mapping
    public CartItemInfoDTO(
            @JsonProperty("storeId") int storeId,
            @JsonProperty("productId") int productId,
            @JsonProperty("storeName") String storeName,
            @JsonProperty("productName") String productName,
            @JsonProperty("quantityInCart") int quantityInCart,
            @JsonProperty("inStock") boolean inStock,
            @JsonProperty("unitPrice") double unitPrice,
            @JsonProperty("isAuction") boolean isAuction) {
        this.storeId = storeId;
        this.productId = productId;
        this.storeName = storeName;
        this.productName = productName;
        this.quantityInCart = quantityInCart;
        this.inStock = inStock;
        this.unitPrice = unitPrice;
        this.isAuction = isAuction;
    }

    // Getters (and setters if fields aren't final and you need them)
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantityInCart() {
        return quantityInCart;
    }

    public void setQuantityInCart(int quantityInCart) {
        this.quantityInCart = quantityInCart;
    }

    public boolean isInStock() {
        return inStock;
    } // Jackson uses isInStock() for boolean getter

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isAuction(){
        return this.isAuction;
    }

    public void setIsAuction(boolean isAuction){
        this.isAuction = isAuction;
    }
}
