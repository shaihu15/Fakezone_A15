package DomainLayer.Interfaces;

import java.util.HashMap;

import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Order;
import DomainLayer.Model.User;

public interface IUserType {
    User getUser();
    void addToCart();
    boolean logout();
    boolean isRegistered();
    void addRole(int storeID, IRegisteredRole role);
    void removeRole(int storeID);
    int getUserID();
    String getEmail();
    boolean didPurchaseStore(int storeID);
    boolean didPurchaseProduct(int storeID, int productID);
    IRegisteredRole getRoleByStoreID(int storeID);
    HashMap<Integer, IRegisteredRole> getAllRoles();
    HashMap<Integer, Order> getOrders(); // userID -> Order
    boolean isLoggedIn();
}
