package DomainLayer.Model;

import java.util.HashMap;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.IRepository.IRegisteredRole;

public class Guest extends UserType{
    public Guest(){
        super();
    }

    public boolean isRegistered(){
        return false;
    }

    public void addToCart(Product product){
        // same logic as Registered
    }

    @Override
    public boolean logout() {
        throw new UnsupportedOperationException("Guest cannot be logged out");
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

    @Override
    public boolean addToCart(int storeID, ProductDTO product) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addToCart'");
    }

}
