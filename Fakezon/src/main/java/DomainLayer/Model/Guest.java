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
        throw new UnsupportedOperationException("Guests cannot log out because they are not logged in.");
    }
}
