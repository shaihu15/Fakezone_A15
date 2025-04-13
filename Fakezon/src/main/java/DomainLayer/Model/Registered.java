package DomainLayer.Model;

public class Registered extends UserType{
    private RegisteredRole role;

    public Registered(User user){
        super(user);
        this.role = new UnassignedRole(this);
    }

    public boolean isRegistered(){
        return true;
    }

    public void addToCart(Product product){
        // same logic as Guest
    }

}
