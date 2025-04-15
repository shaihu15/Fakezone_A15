package DomainLayer.Model;

import java.util.HashMap;

public class Store {
    private String name;
    private int storeID;
    private HashMap<Integer, Product> products;
    private HashMap<Integer, StoreRating> ratings;

    public Store(String name, int storeID) {
        this.name = name;
        this.storeID = storeID;
        this.products = new HashMap<>();
        this.ratings = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public int getId(){
        return storeID;
    }
    //precondition: user is logged in and  previously made a purchase from the store - cheaked by service layer
    public void addRating(int userID, double rating, String comment) {
        ratings.put(userID, new StoreRating(userID, rating, comment));
    }
}
