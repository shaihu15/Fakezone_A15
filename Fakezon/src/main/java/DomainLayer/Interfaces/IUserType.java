package DomainLayer.Interfaces;

import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.User;

public interface IUserType {
    User getUser();
    void addToCart();
    boolean logout();
    boolean isRegistered();
    void addRole(int storeID, IRegisteredRole role);
    void removeRole(int storeID);
}
