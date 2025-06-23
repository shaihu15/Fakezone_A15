package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "auction_products")
@DiscriminatorValue("AUCTION_PRODUCT")
public class AuctionProduct extends StoreProduct {
    
    @Column(name = "auction_store_id")
    private Integer auctionStoreId;
    
    @Column(nullable = false)
    private double currentHighestBid;
    
    @Column(nullable = false)
    private int MinutesToEnd;
    
    @Column(nullable = false)
    private int productID;
    
    @Column(nullable = false)
    private int userIDHighestBid;
    
    @Column(nullable = false)
    private boolean isDone;

    // Default constructor for JPA
    public AuctionProduct() {
        super();
    }

    public AuctionProduct(StoreProduct storeProduct, double basePrice, int MinutesToEnd) {
        // Use the constructor with individual parameters, but don't pass the ID
        super(0, storeProduct.getStoreId(), storeProduct.getName(), storeProduct.getBasePrice(), 
              storeProduct.getQuantity(), storeProduct.getCategory());
        
        // Set auction-specific fields
        this.currentHighestBid = basePrice;
        this.MinutesToEnd = MinutesToEnd;
        // In unit tests (non-JPA), the ID will be 0, so we'll set it later in the Store class
        // In production (JPA), use the StoreProduct's auto-generated ID
        this.productID = storeProduct.getSproductID();
        this.userIDHighestBid = -1;// -1 means no bids yet
        this.isDone = false;
        this.auctionStoreId = storeProduct.getStoreId();
    }


    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public int getMinutesToEnd() {
        return MinutesToEnd;
    }

    public int getProductID() {
        return productID;
    }
    public int getUserIDHighestBid() {
        return userIDHighestBid;
    }
    //if userID did a higher bid it return the previous userIDHighestBid or -1 if he is the first
    //if not it will return userID
    public synchronized int addBid( int userID,double bidAmount) {
        int prev = userID;
        if (bidAmount > currentHighestBid) {
            prev = userIDHighestBid;
            currentHighestBid = bidAmount;
            userIDHighestBid = userID;
        }
        return prev;
    }

    public StoreProductDTO toDTO(int storeId) {
        return new StoreProductDTO(this.getSproductID(), this.getName(), this.getBasePrice(), this.getQuantity(),
                this.getAverageRating(), storeId, this.getCategory()); 
    }

    public void addMinutes(int minutes){
        if(minutes <= 0){
            throw new IllegalArgumentException("Added minutes must be greater than 0");
        }
        MinutesToEnd += minutes;
    }

    public boolean isDone(){
        return isDone;
    }

    public void setIsDone(boolean isDone){
        this.isDone = isDone;
    }

    public Integer getAuctionStoreId() {
        return auctionStoreId;
    }

    public void setAuctionStoreId(Integer auctionStoreId) {
        this.auctionStoreId = auctionStoreId;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

}
