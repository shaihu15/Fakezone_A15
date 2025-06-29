package DomainLayer.Interfaces;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;

import org.springframework.security.access.method.P;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Predicate;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Offer;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.PurchasePolicy;
import DomainLayer.Model.StoreProduct;
import  DomainLayer.Model.StoreRating;
import DomainLayer.Model.User;
import DomainLayer.Model.helpers.UserMsg;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Model.StoreProductKey;
public interface IStore {
    String getName();

    int getId();

    void addRating(int userID, double rating, String comment);

    void addStoreProductRating(int userID, int productID, double rating, String comment);

    StoreProductDTO addStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity, PCategory category);

    void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy);


    void addAuctionProduct(int requesterId, int productID, double basePrice, int MinutesToEnd);


    void isValidPurchaseAction(int requesterId, int productID);

    void receivingMessage(int userID, String message);

    void sendMessage(int managerId, int userID, String message);

    Map<StoreProductKey, StoreProduct> getStoreProducts();

    Map<Integer, StoreRating> getRatings();

    HashMap<Integer, PurchasePolicy> getPurchasePolicies();

    HashMap<Integer, IDiscountPolicy> getDiscountPolicies();

    List<Integer> getStoreOwners(int requesterId);

    HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int requesterId);

    void addStoreOwner(int appointor, int appointee);

    void addStoreManager(int appointor, int appointee, List<StoreManagerPermission> perms);

    void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms);

    void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove);

    int getStoreFounderID();

    void closeStore(int requesterId);

    void closeStoreByAdmin(int adminId);

    StoreRating getStoreRatingByUser(int userID);

    void openStore(int userId);

    boolean isOpen();

    double getAverageRating();

    StoreProduct getStoreProduct(int productID);

    void removeStoreOwner(int requesterId, int toRemoveId);

    void removeStoreManager(int requesterId, int toRemoveId);

    boolean isOwner(int userId);

    boolean isManager(int userId);

    ProductRating getStoreProductRating(int userID, int productID);

    boolean addBidOnAuctionProduct(int requesterId, int productID, double bidAmount);

    boolean canViewOrders(int userId);
  
    boolean acceptAssignment(int userId);

    void declineAssignment(int userId);

    List<Integer> getPendingOwners(int requesterId);

    List<Integer> getPendingManagers(int requesterId);
    
    void editStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity);
    
    double calcAmount(int userId,Map<Integer,Integer> productToBuy, LocalDate dob, Cart cart);

    Map<Integer,UserMsg> getMessagesFromUsers(int userId);

    Map<StoreProductDTO, Boolean> checkIfProductsInStore(int userID, Map<Integer,Integer> products);

    Map<StoreProductDTO, Boolean> decrementProductsInStore(int userId, Map<Integer,Integer> productsToBuy);

    void returnProductsToStore(int userId, Map<Integer,Integer> products);

    List<ProductRating> getStoreProductAllRatings(int productId);

    void addSimpleDiscountWithProductsScope(int userID, List<Integer> productIDs, double percentage);

    void addSimpleDiscountWithStoreScope(int userID, double percentage);

    void addConditionDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage);

    void addConditionDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage);

    void addAndDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage);

    void addAndDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage);

    void addOrDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage);

    void addOrDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage);

    void addXorDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage);

    void addXorDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage);

    List<StoreManagerPermission> isManagerAndGetPerms(int userId);

    void placeOfferOnStoreProduct(int userId, int productId, double offerAmount);

    void acceptOfferOnStoreProduct(int ownerId, int userId, int productId);

    void declineOfferOnStoreProduct(int ownerId, int userId, int productId);

    void counterOffer(int ownerId, int userId, int productId, double offerAmount);

    void acceptCounterOffer(int userId, int productId);

    void declineCounterOffer(int userId, int productId);

    List<Offer> getUserOffers(int userId);

}