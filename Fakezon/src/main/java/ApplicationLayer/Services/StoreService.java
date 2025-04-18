package ApplicationLayer.Services;

import DomainLayer.IRepository.IStoreRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import DomainLayer.Enums.StoreManagerPermission;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Interfaces.IStoreService;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreProduct;

public class StoreService implements IStoreService {
    private final IStoreRepository storeRepository;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    //should store service catch the errors? who's printing to console??
    @Override
    public void addStoreOwner(int storeId, int requesterId, int newOwnerId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addStoreOwner - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreOwner(requesterId, newOwnerId);
    }

    @Override
    public List<Integer> getStoreOwners(int storeId, int requesterId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
        return store.getStoreOwners(requesterId);
    }

    @Override
    public HashMap<Integer,List<StoreManagerPermission>> getStoreManagers(int storeId, int requesterId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
        return store.getStoreManagers(requesterId);
    }
    // --- Store-related DTO Conversions ---
    private StoreDTO toStoreDTO(Store store) {
        Collection<StoreProductDTO> storeProductDTOs = store.getStoreProducts().values().stream()
            .map(sp->new StoreProductDTO(sp)) // using the constructor directly
            .collect(Collectors.toList());

    Map<Integer, Double> ratings = store.getRatings().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getRating()));

        return new StoreDTO(
                store.getId(),
                store.getName(),
                store.getStoreFounderID(),
                store.isOpen(),
                storeProductDTOs,
                // store.getStoreOwners(),
                // store.getStoreManagers(),
                ratings,
                store.getAverageRating()
        );
    }

    // --- Store Info Methods ---

    @Override
    public StoreDTO viewStore(int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("viewStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return toStoreDTO(store);
    }

    @Override
    public List<StoreDTO> getAllStores() {
        return storeRepository.getAllStores().stream()
                .map(this::toStoreDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoreDTO> searchStores(String keyword) {
        //String lowerKeyword = keyword.toLowerCase();

        return storeRepository.getAllStores().stream()
                .filter(store -> store.getName().contains(keyword))
                .map(this::toStoreDTO)
                .collect(Collectors.toList());
    }

    // --- Skeleton for remaining interface methods (still to be implemented) ---



    @Override
    public int updateStore(int storeId, int requesterId, String name) {
        return 0;
    }

    @Override
    public void deleteStore(int storeId, int requesterId) {}

    @Override
    public void openStore(int storeId, int requesterId) {}

    @Override
    public void closeStore(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if(store == null){
            logger.error("closeStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.closeStore(requesterId);
        logger.info("Store closed: " + storeId + " by user: " + requesterId);
    }

    @Override
    public void updateStoreName(int storeId, String newName, int requesterId) {}

    @Override
    public void addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity, String productType) {}

    @Override
    public void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity, String productType) {}

    @Override
    public void removeProductFromStore(int storeId, int requesterId, int productId) {}

    @Override
    public void addStoreRating(int storeId, int userId, double rating, String comment) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addStoreRating - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.addRating(userId, rating, comment);
        logger.info("Store rating added: " + storeId + " by user: " + userId + " with rating: " + rating);
    }

    @Override
    public void removeStoreRating(int storeId, int userId) {}

    @Override
    public double getStoreAverageRating(int storeId) {
        return 0;
    }

    @Override
    public void addStoreProductRating(int storeId, int productId, int userId, double rating, String comment) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addStoreProductRating - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreProductRating(userId, productId, rating, comment);
        logger.info("Store product rating added: " + productId + " by user: " + userId + " with rating: " + rating);
    }

    @Override
    public void removeStoreProductRating(int storeId, int productId, int userId) {}

    @Override
    public double getStoreProductAverageRating(int storeId, int productId) {
        return 0;
    }

    @Override
    public void addPurchasePolicy(int storeId, int requesterId, int policyId, String name, String description) {}

    @Override
    public void removePurchasePolicy(int storeId, int requesterId, int policyId) {}

    @Override
    public void addDiscountPolicy(int storeId, int requesterId, int policyId, String name, String description) {}

    @Override
    public void removeDiscountPolicy(int storeId, int requesterId, int policyId) {}

    @Override
    public void removeStoreOwner(int storeId, int requesterId, int ownerId) {}

    @Override
    public void addStoreManager(int storeId, int requesterId, int newManagerId, List<StoreManagerPermission> perms) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreManager(requesterId, newManagerId, perms);
    }

    @Override
    public void removeStoreManager(int storeId, int requesterId, int managerId) {}
    @Override
    public int addStore(int userId, String storeName) {
        if(storeRepository.findByName(storeName) != null){
            logger.error("openStore - Store name already exists: " + storeName);
            throw new IllegalArgumentException("Store name already exists");
        }
        Store store = new Store(storeName, userId);
        int storeId = store.getId();
        logger.info("openStore - New store ID: " + storeId);

        storeRepository.addStore(store);
        logger.info("Store opened: " + storeName + " by user: " + userId);
        return storeId;
    }

}


