package ApplicationLayer.Interfaces;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Order;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;

public interface IUserService {
    Optional<Registered> getUserByUserName(String userName);

    Optional<Registered> getUserById(int userID);

    List<Registered> getAllUsers();

    UserDTO registerUser(String email, String password, LocalDate dateOfBirth);

    void deleteUser(String userName);// need?

    void updateUser(Registered user);

    void logout(String email);

    void login(String email, String password);

    boolean isUserLoggedIn(int userID);

    void addRole(int userID, int storeID, IRegisteredRole role);

    void removeRole(int userID, int storeID);

    IRegisteredRole getRoleByStoreID(int userID, int storeID);

    HashMap<Integer, IRegisteredRole> getAllRoles(int userID);

    boolean didPurchaseStore(int userID, int storeID); // check if the user purchased from the store

    boolean didPurchaseProduct(int userID, int storeID, int productID); // check if the user purchased from the product

    Response<List<OrderDTO>> getOrdersByUser(int userID); // get all the orders of the user
    
    void sendMessageToStore(int userID, int storeID, String message); // send message to store


    UserDTO addUser(String password, String email, LocalDate dateOfBirth); // add a new user

    void addToBasket(int userId, int storeId, StoreProductDTO product); // add a product to the user's basket

    List<StoreProductDTO> viewCart(int userId);

    UserDTO convertUserToDTO(User user);

}
