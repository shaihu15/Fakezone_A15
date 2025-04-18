package DomainLayer.Interfaces;

import java.util.HashMap;

import org.springframework.security.access.method.P;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Order;

public interface IUserType {
    //cart
    boolean addToCart(int storeID, ProductDTO product);
    void clearCart();
    Cart getCart();
    
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
