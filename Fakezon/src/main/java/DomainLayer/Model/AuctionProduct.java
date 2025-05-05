package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;


public class AuctionProduct extends StoreProduct {
    private double currentHighestBid;
    private int daysToEnd;
    private int productID;
    private int userIDHighestBid;
    private Map<Integer, Boolean> bidApprovedByOwners = new HashMap<>(); // userID -> approved

    public AuctionProduct(StoreProduct storeProduct, double basePrice, int daysToEnd) {
        super(storeProduct);
        this.currentHighestBid = basePrice;
        this.daysToEnd = daysToEnd;
        this.productID = storeProduct.getSproductID();
        this.userIDHighestBid = -1;// -1 means no bids yet
        this.bidApprovedByOwners = new HashMap<>();
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public int getDaysToEnd() {
        return daysToEnd;
    }

    public int getProductID() {
        return productID;
    }
    public int getUserIDHighestBid() {
        return userIDHighestBid;
    }
    public synchronized boolean addBid( int userID,double bidAmount) {
        
        if (bidAmount > currentHighestBid) {
            currentHighestBid = bidAmount;
            userIDHighestBid = userID;
            return true;
        }
        return false;
    }

    public void setOwnersToApprove(List<Integer> owners) {
        for (int ownerID : owners) {
            bidApprovedByOwners.put(ownerID, false); // Initialize all owners as not approved
        }
    }

    public void setBidApprovedByOwners(int userID, boolean approved) {
        bidApprovedByOwners.put(userID, approved);
    }
    public boolean isApprovedByAllOwners() {
        for (Map.Entry<Integer, Boolean> entry : bidApprovedByOwners.entrySet()) {
            if (!entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public StoreProductDTO toDTO() {
        return new StoreProductDTO(this.getSproductID(), this.getName(), this.getBasePrice(), this.getQuantity(),
                this.getAverageRating(), this.getStoreId(), this.getCategory()); 
    }

    public void addDays(int days){
        if(days <= 0){
            throw new IllegalArgumentException("Added days must be greater than 0");
        }
        daysToEnd += days;
    }

    
}
