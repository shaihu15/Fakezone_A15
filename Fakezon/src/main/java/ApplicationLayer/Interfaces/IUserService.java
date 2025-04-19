package ApplicationLayer.Interfaces;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Order;
import DomainLayer.Model.User;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
public interface IUserService {
    Optional<User> getUserByUserName(String userName);
    Optional<User> getUserById(int userID);
    List<User> getAllUsers();
    User registerUser(User user);
    void deleteUser(String userName);//need?
    User updateUser(User user);
    void logout(int userID);
    void login(int userID, String password);
    boolean isUserLoggedIn(int userID);
    void addRole(int userID, int storeID, IRegisteredRole role);
    void removeRole(int userID, int storeID);
    IRegisteredRole getRoleByStoreID(int userID, int storeID);
    HashMap<Integer, IRegisteredRole> getAllRoles(int userID);
    boolean didPurchaseStore(int userID, int storeID); //check if the user purchased from the store
    boolean didPurchaseProduct(int userID, int storeID, int productID); //check if the user purchased from the product
    HashMap<Integer, Order> getOrdersByUser(int userID); // userID -> Order
}
