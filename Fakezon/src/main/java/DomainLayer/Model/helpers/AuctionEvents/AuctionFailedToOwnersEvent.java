package DomainLayer.Model.helpers.AuctionEvents;

public class AuctionFailedToOwnersEvent {
    private final int storeId;
    private final int productID;
    private final double basePrice;
    private final String message;

    public AuctionFailedToOwnersEvent(int storeId, int productID, double basePrice, String message) {
        this.message = message;
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
    public String getMessage(){
        return this.message;
    }

}
