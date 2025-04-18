package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;

import DomainLayer.IRepository.IRegisteredRole;

public class User {
    private Cart cart;
    private UserType userType; // Guest or Registered


    public User() {
        this.cart = new Cart();
        this.userType = new Guest(this); // default state is Guest
    }

    public Cart getCart() {
        return cart;
    }
    public boolean didPurchaseStore(int storeID) {
        return userType.didPurchaseStore(storeID);
    }
    public boolean didPurchaseProduct(int storeID,int productID) {
        return userType.didPurchaseProduct(storeID, productID);
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
        return userType.getUserID();
    }
    public String getEmail() {
        return userType.getEmail();
    }
    public HashMap<Integer, Order> getOrders() {
        return userType.getOrders();
    }
}
