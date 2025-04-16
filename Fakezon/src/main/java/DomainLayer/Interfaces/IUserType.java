package DomainLayer.Interfaces;

import DomainLayer.Model.User;

public interface IUserType {
    User getUser();
    void addToCart();
    boolean logout();
    boolean isRegistered();

}
