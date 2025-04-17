package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;

import DomainLayer.IRepository.IRegisteredRole;

public class User {
    private Cart cart;
    private UserType userType; // Guest or Registered
    private int userID;
    private String email;
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
    public void setUserType(UserType userType) {
        this.userType = userType;
    }
    public UserType getUserType() {
        return userType;
    }
    public boolean logout() {
        return userType.logout();
    }
    public void addRole(int storeID, IRegisteredRole role) {
        userType.addRole(storeID, role);
    }
    public void removeRole(int storeID) {
        userType.removeRole(storeID);
    }
    public IRegisteredRole getRoleByStoreID(int storeID) {
        return userType.getRoleByStoreID(storeID);
    }
    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        return userType.getAllRoles();
    }
    public int getUserID() {
        return userID;
    }
    public String getEmail() {
        return email;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public HashMap<Integer, Order> getOrders() {
        return orders;
    }
    public void setOrders(HashMap<Integer, Order> orders) {
        this.orders = orders;
    }
    public HashMap<Integer, List<Integer>> getProductsPurchase() {
        return productsPurchase;
    }
    public void setProductsPurchase(HashMap<Integer, List<Integer>> productsPurchase) {
        this.productsPurchase = productsPurchase;
    }
}
