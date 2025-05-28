package ApplicationLayer.Interfaces;

import java.util.AbstractMap.SimpleEntry;

import org.springframework.security.access.method.P;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Cart;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.User;
import java.time.LocalDate;

public interface IStoreService {

  void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int MinutesToEnd);

  void addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid);
    List<AuctionProductDTO> getAuctionProductsFromStore(int storeId);

  int addStore(int userId, String storeName);

  StoreDTO viewStore(int storeId);

  List<StoreDTO> searchStores(String keyword);

  List<StoreDTO> getAllStores();

  void closeStore(int storeId, int requesterId);

  boolean isStoreOpen(int storeId);

  void updateStoreName(int storeId, String newName, int requesterId);

  StoreProductDTO getProductFromStore(int productId, int storeId);

  // --- Product Management ---
  StoreProductDTO addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity, PCategory category);

  void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice,
                  int quantity);

  // --- Ratings ---
  void addStoreRating(int storeId, int userId, double rating, String comment);

  void addStoreProductRating(int storeId, int productId, int userId, double rating, String comment);

  void removeStoreProductRating(int storeId, int productId, int userId);

  double getStoreProductAverageRating(int storeId, int productId);

  // --- Purchase Policies ---
  void addPurchasePolicy(int storeId, int requesterId, int policyId, String name, String description);

  void removePurchasePolicy(int storeId, int requesterId, int policyId);

  // --- Discount Policies ---

  // --- Specific Discount Policy Methods ---
  void addSimpleDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, double percentage);

  void addSimpleDiscountWithStoreScope(int storeId, int requesterId, double percentage);

  void addConditionDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addConditionDiscountWithStoreScope(int storeId, int requesterId, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addAndDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addAndDiscountWithStoreScope(int storeId, int requesterId, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addOrDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addOrDiscountWithStoreScope(int storeId, int requesterId, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addXorDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  void addXorDiscountWithStoreScope(int storeId, int requesterId, List<java.util.function.Predicate<Cart>> conditions, double percentage);

  // --- Ownership & Management ---
  void addStoreOwner(int storeId, int requesterId, int newOwnerId);

  void removeStoreOwner(int storeId, int requesterId, int ownerId);

  List<Integer> getStoreOwners(int storeId, int requesterId);

  void addStoreManagerPermissions(int storeId, int requesterId, int managerId,
                  List<StoreManagerPermission> perms);

  void removeStoreManagerPermissions(int storeId, int requesterId, int managerId,
                  List<StoreManagerPermission> toRemove);

  void addStoreManager(int storeId, int requesterId, int newManagerId, List<StoreManagerPermission> perms);

  void removeStoreManager(int storeId, int requesterId, int managerId);

  HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int storeId, int requesterId);

  void receivingMessage(int storeId, int userId, String message);

  void sendMessageToUser(int managerId, int storeId, int userId, String message);

  Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId, int storeId);

  Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId, int storeId);

  Map<StoreDTO, Map<StoreProductDTO, Boolean>> decrementProductsInStores(int userId, Map<Integer,Map<Integer,Integer>> productsToBuy);
  
  void removeProductFromStore(int storeId, int requesterId, int productId);

  StoreRolesDTO getStoreRoles(int storeId, int requesterId);

  Map<Integer,Double> calcAmount(int user,Cart cart, LocalDate dobInput);

  void sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept);

  boolean canViewOrders(int storeId, int userId);

  boolean acceptAssignment(int storeId, int userId);

  void declineAssignment(int storeId, int userId);

  List<Integer> getPendingOwners(int storeId, int requesterId);

  List<Integer> getPendingManagers(int storeId, int requesterId);

  Map<StoreDTO, Map<StoreProductDTO, Boolean>> checkIfProductsInStores(int userId, Map<Integer, Map<Integer, Integer>> cart);

  void returnProductsToStores(int userId, Map<Integer,Map<Integer,Integer>> products);

  List<ProductRating> getStoreProductRatings(int storeId, int productID);

  Response<HashMap<Integer, String>> getAllStoreMessages(int storeId);

  void clearAllData();

}
