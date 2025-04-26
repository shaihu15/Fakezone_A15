package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import DomainLayer.Model.AuctionProduct;

public class AuctionProductDTO {
    @JsonProperty("productId")
    private final int productId;

    @JsonProperty("name")
    private final String name;
    @JsonProperty("basePrice")
    private final double basePrice;

    @JsonProperty("CurrentHighestBid")
    private final double currentHighestBid;

    @JsonProperty("averageRating")
    private final double averageRating;

    public AuctionProductDTO(AuctionProduct auctionProduct) {
        this.productId = auctionProduct.getSproductID();
        this.name = auctionProduct.getName();
        this.basePrice = auctionProduct.getBasePrice();
        this.currentHighestBid = auctionProduct.getCurrentHighestBid();
        this.averageRating = auctionProduct.getAverageRating(); // Assuming getAverageRating() returns a double
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
}