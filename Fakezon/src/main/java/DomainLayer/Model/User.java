package DomainLayer.Model;

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
}
