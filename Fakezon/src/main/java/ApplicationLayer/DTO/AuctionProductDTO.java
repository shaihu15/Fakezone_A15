package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonCreator
    public AuctionProductDTO(
        @JsonProperty("productId") int productId,
        @JsonProperty("name") String name,
        @JsonProperty("basePrice") double basePrice,
        @JsonProperty("currentHighestBid") double currentHighestBid,
        @JsonProperty("averageRating") double averageRating,
        @JsonProperty("category") PCategory category
    ) {
        this.productId = productId;
        this.name = name;
        this.basePrice = basePrice;
        this.currentHighestBid = currentHighestBid;
        this.averageRating = averageRating;
        this.category = category;
    }

    public AuctionProductDTO(AuctionProduct auctionProduct) {
        this.productId = auctionProduct.getSproductID();
        this.name = auctionProduct.getName();
        this.basePrice = auctionProduct.getBasePrice();
        this.currentHighestBid = auctionProduct.getCurrentHighestBid();
        this.averageRating = auctionProduct.getAverageRating(); // Assuming getAverageRating() returns a double
        this.category = auctionProduct.getCategory();
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
}