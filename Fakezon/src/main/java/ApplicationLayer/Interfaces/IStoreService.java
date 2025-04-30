package ApplicationLayer.Interfaces;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import DomainLayer.Enums.StoreManagerPermission;

import DomainLayer.Model.DiscountPolicy;
import DomainLayer.Model.Product;
import DomainLayer.Model.PurchasePolicy;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import ApplicationLayer.DTO.StoreRolesDTO;

public interface IStoreService {

  void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd);

  void addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid);
    List<AuctionProductDTO> getAuctionProductsFromStore(int storeId);

  int addStore(int userId, String storeName);

  int updateStore(int storeId, int requesterId, String name);

        void deleteStore(int storeId, int requesterId);

        StoreDTO viewStore(int storeId);

        List<StoreDTO> searchStores(String keyword);

        List<StoreDTO> getAllStores();

        void openStore(int storeId, int requesterId);

        void closeStore(int storeId, int requesterId);

        boolean isStoreOpen(int storeId);

        void updateStoreName(int storeId, String newName, int requesterId);

        StoreProductDTO getProductFromStore(int productId, int storeId);

        // --- Product Management ---
        void addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity);

        void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice,
                        int quantity);

        // --- Ratings ---
        void addStoreRating(int storeId, int userId, double rating, String comment);

        void removeStoreRating(int storeId, int userId);

        double getStoreAverageRating(int storeId);

        void addStoreProductRating(int storeId, int productId, int userId, double rating, String comment);

        void removeStoreProductRating(int storeId, int productId, int userId);

        double getStoreProductAverageRating(int storeId, int productId);

        // --- Purchase Policies ---
        void addPurchasePolicy(int storeId, int requesterId, int policyId, String name, String description);

        void removePurchasePolicy(int storeId, int requesterId, int policyId);

        // --- Discount Policies ---
        void addDiscountPolicy(int storeId, int requesterId, int policyId, String name, String description);

        void removeDiscountPolicy(int storeId, int requesterId, int policyId);

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

        StoreProductDTO decrementProductQuantity(int productId, int storeId, int quantity);

        void removeProductFromStore(int storeId, int requesterId, int productId);

        StoreRolesDTO getStoreRoles(int storeId, int requesterId);

        void addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd);

        boolean canViewOrders(int storeId, int userId);

        void acceptAssignment(int storeId, int userId);

        void declineAssignment(int storeId, int userId);
        
        List<Integer> getPendingOwners(int storeId, int requesterId);

        List<Integer> getPendingManagers(int storeId, int requesterId);

}
