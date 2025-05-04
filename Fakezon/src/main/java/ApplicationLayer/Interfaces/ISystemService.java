package ApplicationLayer.Interfaces;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Response;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;

public interface ISystemService {
    // Access to core services
    IDelivery getDeliveryService();

    IAuthenticator getAuthenticatorService();

    IPayment getPaymentService();

    Response<StoreDTO> userAccessStore(String token , int storeId);

    Response<Void> ratingStore(int storeId, int userId, double rating, String comment);

    Response<Void> ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment);

    Response<List<OrderDTO>> getOrdersByUser(int userId); // userID -> OrderDTO

    Response<Void> sendMessageToStore(int userId, int storeId, String message); // send message to store

    Response<Void> sendMessageToUser(int managerId, int storeId, int userToAnswer, String message); // send message to user

    Response<Void> addStore(int userId, String storeName);

    Response<StoreProductDTO> getProductFromStore(int productId, int storeId);

    Response<ProductDTO> getProduct(int productId);

    Response<Boolean> updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds);

    Response<Boolean> deleteProduct(int productId);

    Response<String> guestRegister(String email, String password,String dobInput, String country);
    
    Response<List<ProductDTO>> searchByKeyword(String token, String keyword);
    
    Response<Void> addStoreManagerPermissions(int storeId, String sessionToken, int managerId, List<StoreManagerPermission> perms);
    
    Response<Void> removeStoreManagerPermissions(int storeId, String sessionToken, int managerId, List<StoreManagerPermission> toRemove);

    Response<Void> removeStoreManager(int storeId, int requesterId, int managerId);
    
    Response<Void> removeStoreOwner(int storeId, int requesterId, int ownerId);

    Response<Void> addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> perms);

    Response<Void> addStoreOwner(int storeId, int requesterId, int ownerId);
    
    Response<Void> addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd);
    
    Response<Void> addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid);
    
    Response<StoreRolesDTO> getStoreRoles(int storeId, int userId); // owner gets store roles information

    Response<Void> addToBasket(int userId, int productId, int storeId, int quantity);

    Response<List<StoreProductDTO>> viewCart(int userId); // returns a list of products in the cart

    Response<String> closeStoreByFounder(int storeId, int userId);

    Response<HashMap<Integer, String>> getAllMessages(int userID); // get all the messages of the user
    Response<HashMap<Integer, String>> getAssignmentMessages(int userID); // get all the messages of the user
    Response<HashMap<Integer, String>> getAuctionEndedtMessages(int userID); // get all the messages of the user
    Response<String> sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept);
    Response<Void> addProductToStore(int storeId, int requesterId, int productId, double basePrice, int quantity,String category); // add product to store

    Response<String> purchaseCart(int userId, String country, LocalDate dob, PaymentMethod paymentMethod, String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails); // purchase the cart

    Response<Void> updateProductInStore(int storeId, int requesterId, int productId, double basePrice, int quantity);

    Response<Void> removeProductFromStore(int storeId, int requesterId, int productId);
    
    Response<Void> addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd);

    Response<List<OrderDTO>> getAllStoreOrders(int storeId, int userId);
  
    Response<String> acceptAssignment(int storeId, int userId);

    Response<String> declineAssignment(int storeId, int userId);
    
    Response<List<Integer>> getPendingOwners(int storeId, int requesterId);

    Response<List<Integer>> getPendingManagers(int storeId, int requesterId);

}