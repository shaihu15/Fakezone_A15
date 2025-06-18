package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;


public class AuctionProduct extends StoreProduct {
    private double currentHighestBid;
    private int MinutesToEnd;
    private int productID;
    private int userIDHighestBid;
    private boolean isDone;

    public AuctionProduct(StoreProduct storeProduct, double basePrice, int MinutesToEnd) {
        super(storeProduct);
        this.currentHighestBid = basePrice;
        this.MinutesToEnd = MinutesToEnd;
        this.productID = storeProduct.getSproductID();
        this.userIDHighestBid = -1;// -1 means no bids yet
        this.isDone = false;
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

    
}
