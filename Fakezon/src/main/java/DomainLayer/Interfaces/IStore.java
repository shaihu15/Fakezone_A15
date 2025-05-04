package DomainLayer.Interfaces;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Basket;
import DomainLayer.Model.DiscountPolicy;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.PurchasePolicy;
import DomainLayer.Model.StoreProduct;
import  DomainLayer.Model.StoreRating;
import DomainLayer.Model.User;

public interface IStore {
    String getName();

    int getId();

    void addRating(int userID, double rating, String comment);

    void addStoreProductRating(int userID, int productID, double rating, String comment);

    void addStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity);

    void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy);

    void addDiscountPolicy(int userID, DiscountPolicy discountPolicy);

    void addAuctionProduct(int requesterId, int productID, double basePrice, int daysToEnd);

    boolean addBidToAuctionProduct(int requesterId, int productID, double bidAmount);

    void isValidPurchaseAction(int requesterId, int productID);

    void receivingMessage(int userID, String message);

    void sendMessage(int managerId, int userID, String message);

    Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId);

    Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId);

    HashMap<Integer, StoreProduct> getStoreProducts();

    HashMap<Integer, StoreRating> getRatings();

    HashMap<Integer, PurchasePolicy> getPurchasePolicies();

    HashMap<Integer, DiscountPolicy> getDiscountPolicies();

    List<Integer> getStoreOwners(int requesterId);

    HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int requesterId);

    void addStoreOwner(int appointor, int appointee);

    void addStoreManager(int appointor, int appointee, List<StoreManagerPermission> perms);

    void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms);

    void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove);

    int getStoreFounderID();

    void closeStore(int requesterId);

    StoreRating getStoreRatingByUser(int userID);

    void openStore();

    boolean isOpen();

    double getAverageRating();

    StoreProduct getStoreProduct(int productID);

    void removeStoreOwner(int requesterId, int toRemoveId);

    void removeStoreManager(int requesterId, int toRemoveId);

    StoreProductDTO decrementProductQuantity(int productId, int quantity);

    boolean isOwner(int userId);

    boolean isManager(int userId);

    ProductRating getStoreProductRating(int userID, int productID);

    boolean addBidOnAuctionProduct(int requesterId, int productID, double bidAmount);

    double calcAmount(Basket basket, LocalDate dob,String country);

    boolean canViewOrders(int userId);
  
    void acceptAssignment(int userId);

    void declineAssignment(int userId);

    List<Integer> getPendingOwners(int requesterId);

    List<Integer> getPendingManagers(int requesterId);
    
}