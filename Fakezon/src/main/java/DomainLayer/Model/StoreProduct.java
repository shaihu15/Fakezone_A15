package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;


public class StoreProduct {
    private int SproductID;
    private String name;
    private double basePrice;
    private int quantity;
    private Map<Integer, ProductRating> Pratings = new HashMap<>(); //HASH userID to product rating


   public StoreProduct(int SproductID, String name, double basePrice, int quantity) {
        this.SproductID = SproductID;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
    }
    public StoreProduct(StoreProduct storeProduct) {
        this.SproductID = storeProduct.SproductID;
        this.name = storeProduct.name;
        this.basePrice = storeProduct.basePrice;
        this.quantity = storeProduct.quantity;
    }

    public int getSproductID() {
        return SproductID;
    }
    public String getName() {
        return name;
    }
    public double getBasePrice() {
        return basePrice;
    }
    public synchronized int getQuantity() {
        return quantity;
    }
   
    //precondition: user is logged in and User has purchased the product - cheaked by service layer
   public void  addRating(int userID, double rating, String comment) {
        if (Pratings.containsKey(userID)) {
            Pratings.get(userID).updateRating(rating, comment);
        } else {
            Pratings.put(userID, new ProductRating(userID, rating, comment));
        }
    }
    public ProductRating getRatingByUser(int userID) {
        if (Pratings.containsKey(userID)) {
            return Pratings.get(userID);
        } else {
            throw new IllegalArgumentException("User with ID: " + userID + " has not rated the product with ID: " + SproductID);
        }
    }
    public  double getAverageRating() {
        double totalRating = 0;
        for (ProductRating rating : Pratings.values()) {
            totalRating += rating.getRating();
        }
        return totalRating / Pratings.size();
    }
    public synchronized void setQuantity(int quantity) {
        if(quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }
    public int getStoreId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStoreId'");
    }
}

