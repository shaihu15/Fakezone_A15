package DomainLayer.Model.helpers.AuctionEvents;

public class AuctionEndedToOwnersEvent {
    private final int storeId;
    private final int productID;
    private final int userIDHighestBid;
    private final double currentHighestBid;

    public AuctionEndedToOwnersEvent(int storeId, int productID, int userIDHighestBid, double currentHighestBid) {
        this.storeId = storeId;
        this.productID = productID;
        this.userIDHighestBid = userIDHighestBid;
        this.currentHighestBid = currentHighestBid;
    }

    public int getStoreId(){
        return this.storeId;
    }
    public int getUserIDHighestBid(){
        return this.userIDHighestBid;
    }
    public double getCurrentHighestBid(){
        return this.currentHighestBid;
    }
    public int getProductID(){
        return this.productID;
    }

}
