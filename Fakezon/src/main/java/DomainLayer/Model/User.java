package DomainLayer.Model;

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

}
