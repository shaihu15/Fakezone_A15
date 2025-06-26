package DomainLayer.Model.helpers.AuctionEvents;

public class AuctionSaveEvent {
    int storeId;
    
    private AuctionSaveEvent() {
        // Default constructor for JPA
    }
    public AuctionSaveEvent(int storeId) {
        this.storeId = storeId;
    }
    public int getStoreId() {
        return storeId;
    }
}
