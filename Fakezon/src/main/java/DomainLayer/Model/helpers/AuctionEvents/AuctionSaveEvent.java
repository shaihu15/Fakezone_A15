package DomainLayer.Model.helpers.AuctionEvents;

public class AuctionSaveEvent {
    int storeId;
    int productId;
    
    private AuctionSaveEvent() {
        // Default constructor for JPA
    }
    public AuctionSaveEvent(int storeId, int productId) {
        this.storeId = storeId;
        this.productId = productId;
    }
    public int getStoreId() {
        return storeId;
    }
    public int getProductId() {
        return productId;
    }
}
