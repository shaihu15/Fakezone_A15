package ApplicationLayer.Interfaces;

import java.util.List;

import ApplicationLayer.DTO.StoreDTO;
import DomainLayer.Model.DiscountPolicy;
import DomainLayer.Model.Product;
import DomainLayer.Model.PurchasePolicy;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;

public interface IStoreService {
    int addStore(String name, int founderId);
    int updateStore(int storeId, int requesterId, String name);
    void deleteStore(int storeId, int requesterId);

    StoreDTO viewStore(int storeId);
    List<StoreDTO> searchStores(String keyword);
    List<StoreDTO> getAllStores();

    void openStore(int storeId, int requesterId);
    void closeStore(int storeId, int requesterId);
    void updateStoreName(int storeId, String newName, int requesterId);

    // --- Product Management ---
    void addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity, String productType);
    void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity, String productType);
    void removeProductFromStore(int storeId, int requesterId, int productId);
    //List<IProductDTO> getProductsInStore(int storeId);

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

    void addStoreManager(int storeId, int requesterId, int newManagerId);
    void removeStoreManager(int storeId, int requesterId, int managerId);
    int openStore(int userId, String storeName);//return storeId

}
