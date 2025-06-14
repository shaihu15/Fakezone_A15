package DomainLayer.Model.helpers.OfferEvents;

public class CounterOfferDeclineEvent {
    private int storeId;
    private int productId;
    private int userId;
    private double offerAmount;
    
    public CounterOfferDeclineEvent(int storeId, int productID, int userId, double offerAmount){
        this.storeId = storeId;
        this.productId = productID;
        this.userId = userId;
        this.offerAmount = offerAmount;
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
}
