package ApplicationLayer.Interfaces;

import java.util.HashMap;
import java.util.Set;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Order;

public interface ISystemService {
    // Access to core services
    IDelivery getDeliveryService();

    IAuthenticator getAuthenticatorService();

    IPayment getPaymentService();

    StoreDTO userAccessStore(String token , int storeId);

    void ratingStore(int storeId, int userId, double rating, String comment);

    void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment);

    HashMap<Integer, Order> getOrdersByUser(int userId); // userID -> OrderDTO

    void sendMessageToStore(int userId, int storeId, String message); // send message to store

    void sendMessageToUser(int managerId, int storeId, int userToAnswer, String message); // send message to user

    void addStore(int userId, String storeName);

    StoreProductDTO getProductFromStore(int productId, int storeId);

    ProductDTO getProduct(int productId);

    void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds);

    void deleteProduct(int productId);

    void guestRegister(String email, String password,String dobInput);

    StoreRolesDTO getStoreRoles(int storeId, int userId); // owner gets store roles information
}