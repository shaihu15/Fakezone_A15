package DomainLayer.Model;

import java.util.HashMap;
import java.util.List;

import DomainLayer.IRepository.IRegisteredRole;

public class User {
    private UserType userType; // Guest or Registered


    public User() {
        this.userType = new Guest(); // default state is Guest
    }

    public Cart getCart() {
        return userType.getCart();
    }
    public void clearCart() {
        userType.clearCart();
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
    public boolean isLoggedIn() {
        return userType.isLoggedIn();
    }

    public String getPassword(){
        return userType.getPassword();
    }
    public void sendMessageToStore(int storeID, String message) {
        userType.sendMessageToStore(storeID, message);
    }
    public void receivingMessageFromStore(int storeID, String message) {
        userType.receivingMessageFromStore(storeID, message);
    }
}
