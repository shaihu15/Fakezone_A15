package DomainLayer.Model;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
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

    public void addToBasket(int storeId, StoreProductDTO product) {
        cart.addProduct(storeId, product);
    }

    public Basket getBasket(int storeId) {
        return cart.getBasket(storeId);
    }

    public List<StoreProductDTO> viewCart() {
        List<StoreProductDTO> products = cart.getAllProducts();
        return products;

    }

    public void saveCartOrder() {
        List<StoreProductDTO> products = cart.getAllProducts();
        for (StoreProductDTO product : products) {
            int storeId = product.getStoreId();
            if (!productsPurchase.containsKey(storeId)) {
                productsPurchase.put(storeId, new ArrayList<>());
            }
            productsPurchase.get(storeId).add(product.getProductId());
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

}
