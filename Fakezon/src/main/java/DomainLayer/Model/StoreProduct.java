package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;

public class StoreProduct {
    private String SproductID;
    private String name;
    private double basePrice;
    private int quantity;
    private ProductState state;
    private Map<String, ProductRating> Pratings = new HashMap<>(); //HASH userID to product rating


   public StoreProduct(String SproductID, String name, double basePrice, int quantity, ProductState state) {
        this.SproductID = SproductID;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.state = state;
    }

    public String getSproductID() {
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
   public void  addRating(String userID, double rating, String comment) {
        if (Pratings.containsKey(userID)) {
            Pratings.get(userID).setRating(rating);
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

