package DomainLayer.Model;

import java.util.HashMap;

import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Interfaces.IUserType;

abstract class UserType implements IUserType {
    protected User user;

    public UserType(User user){
        this.user = user;
    }
    public void addToCart(){
        //@TODO
    }

    public User getUser(){
        return user;
    }
    
    abstract public boolean logout();
    abstract public boolean isRegistered();
    abstract public void addRole(int storeID, IRegisteredRole role); // system admin (storeID = -1)or store owner
    abstract public void removeRole(int storeID);
    abstract public IRegisteredRole getRoleByStoreID(int storeID); // system admin (storeID = -1)or store owner
    abstract public HashMap<Integer, IRegisteredRole> getAllRoles(); // system admin (storeID = -1)or store owner
    abstract public boolean isLoggedIn();

}
