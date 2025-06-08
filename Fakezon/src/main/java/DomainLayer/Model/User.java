package DomainLayer.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.UserDTO;

public class User {
    protected boolean isLoggedIn;
    protected int userId;

    protected Cart cart;
    protected static final AtomicInteger idCounter = new AtomicInteger(0);

    public User() {
        this.userId = idCounter.incrementAndGet(); // auto-increment userID
        this.cart = new Cart();
        this.isLoggedIn = false;
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public User(int userId){
        this.userId = userId;
        this.cart = new Cart();
        this.isLoggedIn = false;
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

    public void saveCartOrderAndDeleteIt() {
        //user cant save orders if not logged in
        this.cart.clear();
        return;
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
                cart.setProduct(storeId, productId, quantity);
            }
        }
    }

    public void removeFromBasket(int storeId, int productId){
        cart.removeItem(storeId, productId);
    }

    public void addToBasketQuantity(int storeId, int productId, int quantity) {
        cart.addProductQuantity(storeId, productId, quantity);
    }


}
