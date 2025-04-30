package DomainLayer.Model.helpers.AuctionEvents;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;

public class AuctionApprovedBidEvent {
    private final int storeId;
    private final int productID;
    private final int userIDHighestBid;
    private final double currentHighestBid;
    private final StoreProductDTO storeProductDTO;

    public AuctionApprovedBidEvent(int storeId, int productID, int userIDHighestBid, double currentHighestBid, StoreProductDTO storeProductDTO) {
        this.storeId = storeId;
        this.productID = productID;
        this.userIDHighestBid = userIDHighestBid;
        this.currentHighestBid = currentHighestBid;
        this.storeProductDTO = storeProductDTO;
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
    public StoreProductDTO getStoreProductDTO(){
        return this.storeProductDTO;
    }

}
