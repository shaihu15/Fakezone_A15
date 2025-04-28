package DomainLayer.Model.helpers;

public class AuctionFailedToOwnersEvent {
    private final int storeId;
    private final int productID;
    private final double basePrice;

    public AuctionFailedToOwnersEvent(int storeId, int productID, double basePrice) {
        this.storeId = storeId;
        this.productID = productID;
        this.basePrice = basePrice;
    }

    public int getStoreId(){
        return this.storeId;
    }

    public double getBasePrice(){
        return this.basePrice;
    }
    public int getProductID(){
        return this.productID;
    }

}
