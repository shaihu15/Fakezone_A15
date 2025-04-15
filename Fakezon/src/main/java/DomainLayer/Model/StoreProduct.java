package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;


public class StoreProduct {
    private int SproductID;
    private String name;
    private double basePrice;
    private int quantity;
    private ProductState state;
    private Map<Integer, ProductRating> Pratings = new HashMap<>(); //HASH userID to product rating


   public StoreProduct(int SproductID, String name, double basePrice, int quantity, ProductState state) {
        this.SproductID = SproductID;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.state = state;
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
    public int getQuantity() {
        return quantity;
    }
    public ProductState getState() {
        return state;
    }
    public void setState(ProductState state) {
        this.state = state;
    }
    //precondition: user is logged in and User has purchased the product - cheaked by service layer
   public void  addRating(int userID, double rating, String comment) {
        if (Pratings.containsKey(userID)) {
            Pratings.get(userID).updateRating(rating, comment);
        } else {
            Pratings.put(userID, new ProductRating(userID, rating, comment));
        }
    }
    public  double getAverageRating() {
        double totalRating = 0;
        for (ProductRating rating : Pratings.values()) {
            totalRating += rating.getRating();
        }
        return totalRating / Pratings.size();
    }
}

