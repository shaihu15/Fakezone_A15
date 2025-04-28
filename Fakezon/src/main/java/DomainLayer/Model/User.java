package DomainLayer.Model;

import ApplicationLayer.DTO.StoreProductDTO;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class User {
    protected boolean isLoggedIn;
    protected int userID;
    Cart cart;
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public User() {
        this.userID = idCounter.incrementAndGet(); // auto-increment userID
        this.cart = new Cart();
        this.isLoggedIn = false;

    }

    public boolean isRegistered() {
        return false;
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

}
