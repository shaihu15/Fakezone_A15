package ApplicationLayer.Interfaces;

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

    StoreDTO userAccessStore(String token, int storeId);

    void ratingStore(int storeId, int userId, double rating, String comment);

    void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment);

    Response<List<OrderDTO>> getOrdersByUser(int userId); // userID -> OrderDTO

    void sendMessageToStore(int userId, int storeId, String message); // send message to store

    void sendMessageToUser(int managerId, int storeId, int userToAnswer, String message); // send message to user

    void addStore(int userId, String storeName);

    StoreProductDTO getProductFromStore(int productId, int storeId);

    Response<ProductDTO> getProduct(int productId);

    Response<Boolean> updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds);

    Response<Boolean> deleteProduct(int productId);

   
    String guestRegister(String email, String password,String dobInput);
    
    Response<List<ProductDTO>> searchByKeyword(String token, String keyword);
    
    void addStoreManagerPermissions(int storeId, String sessionToken, int managerId, List<StoreManagerPermission> perms);
    
    void removeStoreManagerPermissions(int storeId, String sessionToken, int managerId, List<StoreManagerPermission> toRemove);

    void removeStoreManager(int storeId, int requesterId, int managerId);

    void removeStoreOwner(int storeId, int requesterId, int ownerId);

    void addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> perms);

    void addStoreOwner(int storeId, int requesterId, int ownerId);

    void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd);

    void addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid);

    StoreRolesDTO getStoreRoles(int storeId, int userId); // owner gets store roles information

    void addToBasket(int userId, int productId, int storeId, int quantity);

    List<StoreProductDTO> viewCart(int userId); // returns a list of products in the cart

    Response<String> closeStoreByFounder(int storeId, int userId);


    Response<String> purchaseCart(int userId, PaymentMethod paymentMethod, String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails); // purchase the cart

    void addProductToStore(int storeId, int requesterId, int productId, double basePrice, int quantity);

    void updateProductInStore(int storeId, int requesterId, int productId, double basePrice, int quantity);

    void removeProductFromStore(int storeId, int requesterId, int productId);
    
    void addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd);

}