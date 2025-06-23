package DomainLayer.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.Enums.PCategory;
import jakarta.persistence.*;

import java.util.Locale.Category;

@Entity
@Table(name = "store_products")
public class StoreProduct {
    
    @Id
    private int SproductID;
    
    @Column(nullable = false)
    private int storeId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private double basePrice;
    
    @Column(nullable = false)
    private int quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PCategory category;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "store_product_id")
    @MapKeyColumn(name = "user_id")
    private Map<Integer, ProductRating> Pratings = new HashMap<>(); //HASH userID to product rating

    // Default constructor for JPA
    public StoreProduct() {}

   public StoreProduct(int SproductID, int storeId,String name, double basePrice, int quantity,PCategory category) {
        this.SproductID = SproductID;
        this.storeId =storeId;
        this.name = name;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.category = category;
    }
    public StoreProduct(StoreProduct storeProduct) {
        this.SproductID = storeProduct.SproductID;
        this.storeId = storeProduct.storeId;
        this.name = storeProduct.name;
        this.basePrice = storeProduct.basePrice;
        this.quantity = storeProduct.quantity;
        this.category = storeProduct.getCategory();
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
    public PCategory getCategory() {
        return category;
    }
    public int getStoreId()
    {
        return storeId;
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

    public synchronized void decrementProductQuantity(int quantity){
        if(this.quantity >= quantity)
        {
            this.quantity -=quantity;
        }
        else
        {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }
    public synchronized void incrementProductQuantity(int quantity){
        this.quantity +=quantity;
    }

    public synchronized List<ProductRating> getAllRatings(){
        return new ArrayList<>(this.Pratings.values());
    }

    public synchronized void setBasePrice(double basePrice) {
        if(basePrice <= 0) {
            throw new IllegalArgumentException("basePrice cannot be negative");
        }
        this.basePrice = basePrice;
    }

}

