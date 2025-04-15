package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;

public class User {
    private Cart cart;
    private UserType userType; // Guest or Registered
    private int userID;
    private HashMap<Integer, Order> orders; // userID -> Order
    private HashMap<Integer,List<Integer>> productsPurchase; // userID -> List of productIDs

    public User() {
        this.cart = new Cart();
        this.userType = new Guest(this); // default state is Guest
    }

    public Cart getCart() {
        return cart;
    }
    public boolean didPurchaseStore(int storeID) {
        return productsPurchase.containsKey(storeID);
    }
    public boolean didPurchaseProduct(int storeID,int productID) {
        if (productsPurchase.containsKey(storeID)) {
            return productsPurchase.get(storeID).contains(productID);
        }
        return false;
    }
}
