package DomainLayer.Model;

public class Registered extends UserType{
    public Registered(User user){
        super(user);
    }

    public boolean isRegistered(){
        return true;
    }

    public void addToCart(Product product){
        // same logic as Guest
    }

}
