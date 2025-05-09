package ApplicationLayer.Interfaces;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ApplicationLayer.DTO.BasketDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public interface ISystemService {

    Response<AbstractMap.SimpleEntry<UserDTO, String>> login(String email, String password); // login to the system
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

    Response<Integer> addStore(int userId, String storeName);

    Response<StoreProductDTO> getProductFromStore(int productId, int storeId);

    Response<ProductDTO> getProduct(int productId);

    Response<Boolean> updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds);

    Response<Boolean> deleteProduct(int productId);

    Response<String> guestRegister(String email, String password,String dobInput, String country);
    
    Response<List<ProductDTO>> searchByKeyword(String token, String keyword);

    Response<List<ProductDTO>> searchByCategory(String category);
    
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

    Response<Map<StoreDTO,Map<StoreProductDTO,Integer>>> viewCart(int userId); // returns a list of products in the cart

    Response<String> closeStoreByFounder(int storeId, int userId);

    Response<HashMap<Integer, String>> getAllMessages(int userID); // get all the messages of the user
  
    Response<HashMap<Integer, String>> getAssignmentMessages(int userID); // get all the messages of the user

    Response<HashMap<Integer, String>> getAuctionEndedMessages(int userID); // get all the messages of the user
  
    Response<String> sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept);
  
    Response<StoreProductDTO> addProductToStore(int storeId, int requesterId, String productName, String description, double basePrice, int quantity,String category); // add product to store

    Response<String> purchaseCart(int userId, String country, LocalDate dob, PaymentMethod paymentMethod, String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails); // purchase the cart

    Response<Void> updateProductInStore(int storeId, int requesterId, int productId, double basePrice, int quantity);

    Response<Void> removeProductFromStore(int storeId, int requesterId, int productId);
    
    Response<List<OrderDTO>> getAllStoreOrders(int storeId, int userId);

    Response<String> acceptAssignment(int storeId, int userId);

    Response<String> declineAssignment(int storeId, int userId);
    
    Response<List<Integer>> getPendingOwners(int storeId, int requesterId);

    Response<List<Integer>> getPendingManagers(int storeId, int requesterId);

    Response<List<StoreProductDTO>> getTopRatedProducts(int limit);

    Response<Boolean> deleteOrder(int orderId, String token);

    Response<OrderDTO> viewOrder(int orderId, String token);

    Response<List<OrderDTO>> searchOrders(String keyword, String token);

    Response<List<OrderDTO>> getOrdersByStoreId(int storeId, String token);

    Response<Void> userLogout(int userID);

    // User suspension management (admin only)
    Response<Void> suspendUser(int requesterId, int userId, LocalDate endOfSuspension);
    
    Response<Boolean> unsuspendUser(int requesterId, int userId);
    
    Response<Boolean> isUserSuspended(int userId);
    
    Response<LocalDate> getSuspensionEndDate(int requesterId, int userId);
    
    Response<List<Registered>> getAllSuspendedUsers(int requesterId);
    
    Response<Integer> cleanupExpiredSuspensions(int requesterId);
    
    // System admin management
    Response<Void> addSystemAdmin(int requesterId, int userId);
    
    Response<Boolean> removeSystemAdmin(int requesterId, int userId);
    
    Response<Boolean> isSystemAdmin(int userId);
    
    Response<List<Registered>> getAllSystemAdmins(int requesterId);
    
    Response<Integer> getSystemAdminCount(int requesterId);

    // Unsigned (guest) user management
    Response<Void> addUnsignedUser(User user);
    
    Response<User> getUnsignedUserById(int userId);
    
    Response<List<User>> getAllUnsignedUsers(int adminId);
    
    Response<Void> updateUnsignedUser(User user);
    
    Response<Boolean> removeUnsignedUser(int userId);
    
    Response<Boolean> isUnsignedUser(int userId);
    
    Response<Integer> getUnsignedUserCount(int adminId);
}