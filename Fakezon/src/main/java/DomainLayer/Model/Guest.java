package DomainLayer.Model;

public class Guest extends UserType{
    public Guest(User user){
        super(user);
    }

    public boolean isRegistered(){
        return false;
    }

    public void addToCart(Product product){
        // same logic as Registered
    }

    @Override
    public boolean logout() {
        return false; // cannot logout as a guest
    }
}
