package DomainLayer.Model;
import java.util.HashMap;

import DomainLayer.IRepository.IRegisteredRole;


public class Registered extends UserType{
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role
    private boolean isLoggedIn;

    public Registered(User user){
        super(user);
        this.roles = new HashMap<>();
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
    public void addRole(int storeID, IRegisteredRole role){ // sytem admin (storeID = -1)or store owner
        roles.put(storeID, role);
    }
    public void removeRole(int storeID){ // sytem admin (storeID = -1)or store owner
        if (roles.containsKey(storeID)) {
            roles.remove(storeID);
        } else {
            throw new IllegalArgumentException("Role not found for the given store ID.");
        }
    }

    @Override
    public IRegisteredRole getRoleByStoreID(int storeID) {
        return roles.get(storeID); // system admin (storeID = -1)or store owner
    }

    @Override
    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        return roles; // system admin (storeID = -1)or store owner
    }


}
