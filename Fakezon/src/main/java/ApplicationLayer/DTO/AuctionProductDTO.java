package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Model.AuctionProduct;

public class AuctionProductDTO {
    @JsonProperty("productId")
    private final int productId;

    @JsonProperty("name")
    private final String name;
    @JsonProperty("basePrice")
    private final double basePrice;

    @JsonProperty("currentHighestBid")
    private final double currentHighestBid;

    @JsonProperty("averageRating")
    private final double averageRating;

    @JsonProperty("category")
    private final PCategory category;

    @JsonProperty("isDone")
    private boolean isDone;

    @JsonCreator
    public AuctionProductDTO(
        @JsonProperty("productId") int productId,
        @JsonProperty("name") String name,
        @JsonProperty("basePrice") double basePrice,
        @JsonProperty("currentHighestBid") double currentHighestBid,
        @JsonProperty("averageRating") double averageRating,
        @JsonProperty("category") PCategory category,
        @JsonProperty("isDone") boolean isDone
    ) {
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.currentHighestBid = currentHighestBid;
        this.averageRating = averageRating;
        this.category = category;
        this.isDone = isDone;
    }

    public AuctionProductDTO(AuctionProduct auctionProduct) {
        this.productId = auctionProduct.getStoreProduct().getSproductID(); // Use the original StoreProduct ID for matching
        this.name = auctionProduct.getStoreProduct().getName();
        this.basePrice = auctionProduct.getStoreProduct().getBasePrice();
        this.currentHighestBid = auctionProduct.getCurrentHighestBid();
        this.averageRating = auctionProduct.getStoreProduct().getAverageRating(); // Assuming getAverageRating() returns a double
        this.category = auctionProduct.getStoreProduct().getCategory();
        this.isDone = auctionProduct.isDone();
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
    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public PCategory getCategory(){
        return this.category;
    }

    public boolean isDone(){
        return this.isDone;
    }
}