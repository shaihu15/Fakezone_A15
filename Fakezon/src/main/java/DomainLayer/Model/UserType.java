package DomainLayer.Model;

import java.util.HashMap;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Interfaces.IUserType;

abstract class UserType implements IUserType {
    Cart cart;
    public UserType(){
        this.cart = new Cart();
    }
    abstract public boolean addToCart(int storeID,ProductDTO product);

    public void clearCart(){
        cart.clear();
    }
    public Cart getCart(){
        return cart;
    }
    abstract public boolean logout();
    abstract public boolean isRegistered();
    abstract public void addRole(int storeID, IRegisteredRole role); // system admin (storeID = -1)or store owner
    abstract public void removeRole(int storeID);
    abstract public IRegisteredRole getRoleByStoreID(int storeID); // system admin (storeID = -1)or store owner
    abstract public HashMap<Integer, IRegisteredRole> getAllRoles(); // system admin (storeID = -1)or store owner
    abstract public boolean isLoggedIn();

}
