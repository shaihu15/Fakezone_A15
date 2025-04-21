package ApplicationLayer.Interfaces;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Order;
import DomainLayer.Model.Registered;

public interface IUserService {
    Optional<Registered> getUserByUserName(String userName);

    Optional<Registered> getUserById(int userID);

    List<Registered> getAllUsers();

    UserDTO registerUser(Registered user);

    void deleteUser(String userName);// need?

    void updateUser(Registered user);

    void logout(int userID);

    void login(int userID, String password);

    boolean isUserLoggedIn(int userID);

    void addRole(int userID, int storeID, IRegisteredRole role);

    void removeRole(int userID, int storeID);

    IRegisteredRole getRoleByStoreID(int userID, int storeID);

    HashMap<Integer, IRegisteredRole> getAllRoles(int userID);

    boolean didPurchaseStore(int userID, int storeID); // check if the user purchased from the store

    boolean didPurchaseProduct(int userID, int storeID, int productID); // check if the user purchased from the product

    HashMap<Integer, Order> getOrdersByUser(int userID); // userID -> Order

    void sendMessageToStore(int userID, int storeID, String message); // send message to store

    void receivingMessageFromStore(int userID, int storeID, String message); // receive message from store

    UserDTO addUser(String password, String email, LocalDate dateOfBirth); // add a new user

    void guestRegister(String userName, String password, String email, int UserId, LocalDate dateOfBirth); // authanticate
                                                                                                           // a guest
                                                                                                           // user

}
