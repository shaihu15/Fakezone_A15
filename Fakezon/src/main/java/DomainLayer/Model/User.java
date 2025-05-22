package DomainLayer.Model;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;

import org.apache.commons.lang3.ObjectUtils.Null;

public class User {
    protected boolean isLoggedIn;
    protected int userId;
    protected HashMap<Integer, OrderDTO> orders; // orderId -> Order
    protected HashMap<Integer, List<Integer>> productsPurchase; // storeId -> List of productIDs
    protected Cart cart;
    protected static final AtomicInteger idCounter = new AtomicInteger(0);

    public User() {
        this.userId = idCounter.incrementAndGet(); // auto-increment userID
        this.cart = new Cart();
        this.isLoggedIn = false;
        this.orders = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public User(int userId){
        this.userId = userId;
        this.cart = new Cart();
        this.isLoggedIn = false;
        this.orders = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }
    
    public boolean isRegistered() {
        return false;
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    public Cart getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
    }

    public boolean logout() {
        cart.clear();
        return true;
    }

    public void addToBasket(int storeId, int productId, int quantity) {
        cart.addProduct(storeId, productId, quantity);
    }

    public Basket getBasket(int storeId) {
        return cart.getBasket(storeId);
    }

    public Map<Integer,Map<Integer,Integer>> viewCart() {
        Map<Integer,Map<Integer,Integer>> allProducts = cart.getAllProducts();
        return allProducts;

    }

    public void saveCartOrder() {
        Map<Integer,Map<Integer,Integer>> products = cart.getAllProducts();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : products.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> productQuantities = entry.getValue();
            for (Map.Entry<Integer, Integer> productEntry : productQuantities.entrySet()) {
                int productId = productEntry.getKey();
                if (!productsPurchase.containsKey(storeId)) {
                    productsPurchase.put(storeId, new ArrayList<>());
                }
                productsPurchase.get(storeId).add(productId);
            }
        }
    }

    public int getUserId() {
        return userId;
    }

    public UserDTO toDTO() {
        return new UserDTO(userId, null, -1);
    }

    public void setUserId(int userId) { ///this one is only for testing purposes, will 
        this.userId = userId;
    }

    public void setCart(Map<Integer,Map<Integer,Integer>> validCart) {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : validCart.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> productQuantities = entry.getValue();
            for (Map.Entry<Integer, Integer> productEntry : productQuantities.entrySet()) {
                int productId = productEntry.getKey();
                int quantity = productEntry.getValue();
                cart.addProduct(storeId, productId, quantity);
            }
        }
    }

}
