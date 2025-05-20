package DomainLayer.Model.helpers.AuctionEvents;

public class AuctionGotHigherBidEvent {
    private final int storeId;
    private final int productID;
    private final int userIDPrevHighestBid;
    private final double currentHighestBid;

    public AuctionGotHigherBidEvent(int storeId, int productID, int userIDPrevHighestBid, double currentHighestBid) {
        this.storeId = storeId;
        this.productID = productID;
        this.userIDPrevHighestBid = userIDPrevHighestBid;
        this.currentHighestBid = currentHighestBid;
    }

    public int getStoreId(){
        return this.storeId;
    }
    public int getUserIDPrevHighestBid(){
        return this.userIDPrevHighestBid;
    }
    public double getCurrentHighestBid(){
        return this.currentHighestBid;
    }
    public int getProductID(){
        return this.productID;
    }


}
