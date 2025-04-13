package DomainLayer.Model;

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
}
