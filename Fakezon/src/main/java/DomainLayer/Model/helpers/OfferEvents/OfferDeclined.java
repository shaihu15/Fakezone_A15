package DomainLayer.Model.helpers.OfferEvents;

public class OfferDeclined {
    private int storeId;
    private int productId;
    private int userId;
    private double offerAmount;
    private int declinedBy;
    
    public OfferDeclined(int storeId, int productID, int userId, double offerAmount, int declinedBy){
        this.storeId = storeId;
        this.productId = productID;
        this.userId = userId;
        this.offerAmount = offerAmount;
        this.declinedBy = declinedBy;
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

    public int getDeclinedBy(){
        return declinedBy;
    }
}
