package DomainLayer.Model;

public class Offer {
    private int userId;
    private int productId;
    private double offerAmount;

    public Offer(int userId, int productId, double offerAmount){
        this.userId = userId;
        this.productId = productId;
        this.offerAmount = offerAmount;
    }

    public int getUserId(){
        return userId;
    }

    public int getProductId(){
        return productId;
    }

    public double getOfferAmount(){
        return offerAmount;
    }
}
