package ApplicationLayer.Services;

import DomainLayer.IRepository.IStoreRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Override
    public void addStoreOwner(int storeId, int requesterId, int newOwnerId){
        try{
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found");
            }
            store.addStoreOwner(requesterId, newOwnerId);
        }catch (Exception e){
            System.out.println("Error doring add store owner: " + e.getMessage());
        }
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
    public int addStore(String name, int founderId) {
        return 0;
    }

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
            throw new IllegalArgumentException("Store not found");
        }
        store.closeStore(requesterId);
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
            throw new IllegalArgumentException("Store not found");
        }
        store.addRating(userId, rating, comment);
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
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreProductRating(userId, productId, rating, comment);
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
    public void addStoreManager(int storeId, int requesterId, int newManagerId) {}

    @Override
    public void removeStoreManager(int storeId, int requesterId, int managerId) {}

}


