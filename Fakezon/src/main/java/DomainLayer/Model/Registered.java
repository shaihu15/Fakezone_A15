package DomainLayer.Model;
import java.util.HashMap;
import java.util.List;

import DomainLayer.IRepository.IRegisteredRole;


public class Registered extends UserType{
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role
    private boolean isLoggedIn;
    private int userID;
    private String email;
    private HashMap<Integer, Order> orders; // userID -> Order
    private HashMap<Integer,List<Integer>> productsPurchase; // userID -> List of productIDs

    public Registered(User user){
        super(user);
        this.roles = new HashMap<>();
        this.isLoggedIn = true;
    }

    public boolean isRegistered(){
        return true;
    }

    public void addToCart(Product product){
        // same logic as Guest
    }

    public boolean logout(){
        this.isLoggedIn = false;
        this.user.setUserType(new Guest(this.user));
        return true;
    }
    public void addRole(int storeID, IRegisteredRole role){ // sytem admin (storeID = -1)or store owner
        roles.put(storeID, role);
    }
    public void removeRole(int storeID){ // sytem admin (storeID = -1)or store owner
        if (roles.containsKey(storeID)) {
            roles.remove(storeID);
        } else {
            throw new IllegalArgumentException("Role not found for the given store ID.");
        }
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
    public int getUserID() {
        return userID;
    }
    public String getEmail() {
        return email;
    }

    @Override
    public IRegisteredRole getRoleByStoreID(int storeID) {
        return roles.get(storeID); // system admin (storeID = -1)or store owner
    }

    @Override
    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        return roles; // system admin (storeID = -1)or store owner
    }

    @Override
    public HashMap<Integer, Order> getOrders() {
        return orders; // userID -> Order
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }


}
