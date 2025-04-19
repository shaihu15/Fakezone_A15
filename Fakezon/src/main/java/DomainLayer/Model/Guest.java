package DomainLayer.Model;

import java.util.HashMap;

import DomainLayer.IRepository.IRegisteredRole;

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
        user.getCart().clear(); // Clear the cart when logging out
        return true; // Assuming logout is successful
    }

    @Override
    public void addRole(int storeID, IRegisteredRole role) {
        throw new UnsupportedOperationException("Guest cannot have role");
    }

    @Override
    public void removeRole(int storeID) {
        throw new UnsupportedOperationException("Guest cannot have role");
    }

    @Override
    public IRegisteredRole getRoleByStoreID(int storeID) {
        throw new UnsupportedOperationException("Guest cannot have role");
    }

    @Override
    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        throw new UnsupportedOperationException("Guest cannot have role");
    }

    @Override
    public int getUserID() {
        throw new UnsupportedOperationException("Guest cannot have user ID");
    }

    @Override
    public String getEmail() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Guest cannot have email");
    }

    @Override
    public boolean didPurchaseStore(int storeID) {
        throw new UnsupportedOperationException("Guest dones't have purchase history");
    }

    @Override
    public boolean didPurchaseProduct(int storeID, int productID) {
        throw new UnsupportedOperationException("Guest dones't have purchase history");
    }

    @Override
    public HashMap<Integer, Order> getOrders() {
        throw new UnsupportedOperationException("Guest dones't have purchase history");
    }
    public boolean isLoggedIn() {
        throw new UnsupportedOperationException("Guest cannot be logged in");
    }
}
