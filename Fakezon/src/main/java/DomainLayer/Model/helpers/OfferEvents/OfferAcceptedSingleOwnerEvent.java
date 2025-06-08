package DomainLayer.Model.helpers.OfferEvents;

public class OfferAcceptedSingleOwnerEvent {
    private int storeId;
    private int productId;
    private int userId;
    private double offerAmount;
    private int ownerId;

    public OfferAcceptedSingleOwnerEvent(int storeId, int productID, int userId, double offerAmount, int ownerId){
        this.storeId = storeId;
        this.productId = productID;
        this.userId = userId;
        this.offerAmount = offerAmount;
        this.ownerId = ownerId;
    }

    public int getStoreId(){
        return storeId;
    }

    public int getProductId(){
        return productId;
    }

    public int getUserId(){
        return userId;
    }
    
    public double getOfferAmount(){
        return offerAmount;
    }

    public int getOwnerId(){
        return ownerId;
    }
}
