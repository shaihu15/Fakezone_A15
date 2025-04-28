package DomainLayer.Model;


public class AuctionProduct extends StoreProduct {
    private double currentHighestBid;
    private int daysToEnd;
    private int productID;
    private int userIDHighestBid;



    public AuctionProduct(StoreProduct storeProduct, double basePrice, int daysToEnd) {
        super(storeProduct);
        this.currentHighestBid = basePrice;
        this.daysToEnd = daysToEnd;
        this.productID = storeProduct.getSproductID();
        this.userIDHighestBid = -1;// -1 means no bids yet
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public int getDaysToEnd() {
        return daysToEnd;
    }

    public int getProductID() {
        return productID;
    }
    public int getUserIDHighestBid() {
        return userIDHighestBid;
    }
    public synchronized boolean addBid( int userID,double bidAmount) {
        
        if (bidAmount > currentHighestBid) {
            if(this.userIDHighestBid == -1)
            {
                this.setQuantity(this.getQuantity()-1);
            }
            currentHighestBid = bidAmount;
            userIDHighestBid = userID;
            return true;
        }
        return false;
    }
    public void failedAuction() {
        this.setQuantity(this.getQuantity()+1);

    }

    public void addDays(int days){
        if(days <= 0){
            throw new IllegalArgumentException("Added days must be greater than 0");
        }
        daysToEnd += days;
    }

    
}
