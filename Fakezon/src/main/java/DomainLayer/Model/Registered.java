package DomainLayer.Model;

public class Registered extends UserType{
    private RegisteredRole role;
    private boolean isLoggedIn;

    public Registered(User user){
        super(user);
        this.role = new UnassignedRole(this);
        this.isLoggedIn = true;
    }

    public boolean isRegistered(){
        return true;
    }

    public void addToCart(Product product){
        // same logic as Guest
    }

    public boolean logout(){
        this.isLoggedIn = false;
        this.user.setUserType(new Guest(this.user));
        return true;
    }

}
