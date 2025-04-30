package ApplicationLayer.Services;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import DomainLayer.Enums.StoreManagerPermission;
import ApplicationLayer.DTO.AuctionProductDTO;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Interfaces.IStoreService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreProduct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import ApplicationLayer.DTO.StoreRolesDTO;

public class StoreService implements IStoreService {
    private final IStoreRepository storeRepository;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    private final ApplicationEventPublisher publisher;

    public StoreService(IStoreRepository storeRepository, ApplicationEventPublisher publisher) {
        this.storeRepository = storeRepository;
        this.publisher = publisher;
    }

    // should store service catch the errors? who's printing to console??
    @Override
    public void addStoreOwner(int storeId, int requesterId, int newOwnerId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addStoreOwner - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreOwner(requesterId, newOwnerId);
    }

    @Override
    public List<Integer> getStoreOwners(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            throw new IllegalArgumentException("Store not found");
        }
        return store.getStoreOwners(requesterId);
    }

    @Override
    public HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getStoreManagers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.getStoreManagers(requesterId);
    }

    // --- Store-related DTO Conversions ---
    private StoreDTO toStoreDTO(Store store) {
        Collection<StoreProductDTO> storeProductDTOs = store.getStoreProducts().values().stream()
                .map(sp -> new StoreProductDTO(sp)) // using the constructor directly
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
                store.getAverageRating());
    }

    public StoreProductDTO toStoreProductDTO(StoreProduct storeProduct) {
        return new StoreProductDTO(storeProduct);
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
        // String lowerKeyword = keyword.toLowerCase();

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
    public void deleteStore(int storeId, int requesterId) {
    }

    @Override
    public void openStore(int storeId, int requesterId) {
    }

    @Override
    public void closeStore(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("closeStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.closeStore(requesterId);
        logger.info("Store closed: " + storeId + " by user: " + requesterId);
    }

    @Override
    public void updateStoreName(int storeId, String newName, int requesterId) {
    }

    @Override
    public void addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity) {
        try{
            logger.info("Store Service - User " + requesterId + " trying to add store product " + productId + " to store "+ storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null){
                logger.error("Store Service - addProductToStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addStoreProduct(requesterId, productId, name, basePrice, quantity);
        }
        catch (Exception e){
            logger.error("StoreService - failed to add store product " + e.getMessage());
            throw e;
        }
    }


    @Override
    public void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice, int quantity) {
        try{
            logger.info("Store Service - User " + requesterId + " trying to update store product " + productId + " in store "+ storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null){
                logger.error("Store Service - updateProductToStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.editStoreProduct(requesterId, productId, name, basePrice, quantity);
        }
        catch (Exception e){
            logger.error("StoreService - failed to update store product " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeProductFromStore(int storeId, int requesterId, int productId) {
        try{
            logger.info("Store Service - User " + requesterId + " trying to remove store product " + productId + " from store "+ storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null){
                logger.error("Store Service - removeProductFromStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.removeStoreProduct(requesterId, productId);
        }
        catch (Exception e){
            logger.error("StoreService - failed to remove store product " + e.getMessage());
            throw e;
        }
    }

    
    @Override
    public void addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd){
        try {
            logger.info("Store Service - User "+ requesterId + " trying to add " + daysToAdd + " days to auction product " + productId + " in store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null){
                logger.error("Store Service - removeProductFromStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addAuctionProductDays(requesterId, productId, daysToAdd);
        } catch (Exception e) {
            logger.error("StoreService - failed to add store auction product days " + e.getMessage());
            throw e;
        }
    }

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
    public void removeStoreRating(int storeId, int userId) {
    }

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
    public void removeStoreProductRating(int storeId, int productId, int userId) {
    }

    @Override
    public double getStoreProductAverageRating(int storeId, int productId) {
        return 0;
    }

    @Override
    public void addPurchasePolicy(int storeId, int requesterId, int policyId, String name, String description) {
    }

    @Override
    public void removePurchasePolicy(int storeId, int requesterId, int policyId) {
    }

    @Override
    public void addDiscountPolicy(int storeId, int requesterId, int policyId, String name, String description) {
    }

    @Override
    public void removeDiscountPolicy(int storeId, int requesterId, int policyId) {
    }

    @Override
    public void removeStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to remove store owner " + ownerId
                    + " from store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - removeStoreOwner - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.removeStoreOwner(requesterId, ownerId);
        } catch (Exception e) {
            logger.error("StoreService - failed to remove store owner " + e.getMessage());
            throw e;
        }

    }

    @Override
    public StoreRolesDTO getStoreRoles(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getStoreRoles - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return new StoreRolesDTO(store, requesterId);
    }

    @Override
    public void addStoreManager(int storeId, int requesterId, int newManagerId, List<StoreManagerPermission> perms) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add store manager " + newManagerId
                    + " to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addStoreManager - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addStoreManager(requesterId, newManagerId, perms);
        } catch (Exception e) {
            logger.error("Store Service - failed to add store manager " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addStoreManagerPermissions(int storeId, int requesterId, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add store manager permissions to "
                    + managerId + " in store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addStoreManagerPermissions - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addManagerPermissions(requesterId, managerId, perms);
        } catch (Exception e) {
            logger.error("Store Service - failed to add manager permissions: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeStoreManagerPermissions(int storeId, int requesterId, int managerId,
            List<StoreManagerPermission> toRemove) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to remove store manager permissions from "
                    + managerId + " in store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - removeStoreManagerPermissions - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.removeManagerPermissions(requesterId, managerId, toRemove);
        } catch (Exception e) {
            logger.error("Store Service - failed to remove  manager permissions: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeStoreManager(int storeId, int requesterId, int managerId) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to remove store manager " + managerId
                    + " from store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - removeStoreManager - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.removeStoreManager(requesterId, managerId);
        } catch (Exception e) {
            logger.error("Store Service - failed to remove store manager");
            throw e;
        }
    }

    @Override
    public int addStore(int userId, String storeName) {
        if (storeRepository.findByName(storeName) != null) {
            logger.error("openStore - Store name already exists: " + storeName);
            throw new IllegalArgumentException("Store name already exists");
        }
        Store store = new Store(storeName, userId, publisher);
        int storeId = store.getId();
        logger.info("openStore - New store ID: " + storeId);

        storeRepository.addStore(store);
        logger.info("Store opened: " + storeName + " by user: " + userId);
        return storeId;
    }

    @Override
    public void receivingMessage(int storeId, int userId, String message) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("receivingMessage - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.receivingMessage(userId, message);
    }

    @Override
    public void sendMessageToUser(int managerId, int storeId, int userId, String message) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("sendMessage - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.sendMessage(managerId, userId, message);
    }

    @Override
    public boolean isStoreOpen(int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("isStoreOpen - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.isOpen();
    }

    @Override
    public Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId, int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getMessagesFromUsers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            return store.getMessagesFromUsers(managerId);
        } catch (IllegalArgumentException e) {
            logger.error("getMessagesFromUsers - Manager not found: " + managerId);
            throw new IllegalArgumentException("Manager not found");
        }
    }

    @Override
    public Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId, int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getMessagesFromStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            return store.getMessagesFromStore(managerId);
        } catch (IllegalArgumentException e) {
            logger.error("getMessagesFromStore - Manager not found: " + managerId);
            throw new IllegalArgumentException("Manager not found");
        }
    }

    @Override
    public StoreProductDTO getProductFromStore(int productId, int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getProductFromStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        StoreProduct product = store.getStoreProduct(productId);
        if (product == null) {
            logger.error("getProductFromStore - Product not found: " + productId);
            throw new IllegalArgumentException("Product not found");
        }
        return toStoreProductDTO(product);
    }

    public void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addAuctionProductToStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.addAuctionProduct(requesterId, productID, basePrice, daysToEnd);
            logger.info("Auction product added to store: " + storeId + " by user: " + requesterId + " with product ID: "
                    + productID);
        } catch (IllegalArgumentException e) {
            logger.error("addAuctionProductToStore - Product not found: " + productID);
            throw new IllegalArgumentException("Product not found");
        }
    }

    public void addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addBidOnAuctionProductInStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.addBidOnAuctionProduct(requesterId, productID, bid);
            logger.info("Bid added to auction product in store: " + storeId + " by user: " + requesterId
                    + " with product ID: " + productID + " and bid: " + bid);
        } catch (IllegalArgumentException e) {
            logger.error("addBidOnAuctionProductInStore - Product not found: " + productID);
            throw new IllegalArgumentException("Product not found");
        }
    }

    private void isValidPurchaseActionForUserInStore(int storeId, int requesterId, int productId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("isValidPurchaseActionForUserInStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.isValidPurchaseAction(requesterId, productId);
            logger.info("Purchase action is valid for user: " + requesterId + " in store: " + storeId + " for product: "
                    + productId);

        } catch (IllegalArgumentException e) {
            logger.error("isValidPurchaseActionForUserInStore - Product not found: " + productId);
            throw new IllegalArgumentException("Product not found");
        }
    }

    public List<AuctionProductDTO> getAuctionProductsFromStore(int storeId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getAuctionProductsFromStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        List<AuctionProductDTO> auctionProducts = store.getAuctionProducts().stream()
                .map(AuctionProductDTO::new)
                .collect(Collectors.toList());
        logger.info("Auction products retrieved from store: " + storeId);
        return auctionProducts;
    }

    @Override
    public StoreProductDTO decrementProductQuantity(int productId, int storeId, int quantity) {
        Store store = storeRepository.findById(storeId);
        StoreProductDTO prod;
        if (store == null) {
            logger.error("decrementProductQuantity - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            prod = store.decrementProductQuantity(productId, quantity);

        } catch (Exception e) {
            logger.error("decrementProductQuantity - Product not found: " + productId);
            throw new IllegalArgumentException("Product not found");
        }
        logger.info("Product quantity decremented: " + productId + " in store: " + storeId);
        return prod;
    }

    @Override
    public void sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("sendResponseForAuctionByOwner - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.receivedResponseForAuctionByOwner(requesterId, productId, accept);
            logger.info("Response for auction sent by owner: " + requesterId + " for product: " + productId
                    + " in store: " + storeId);
        } catch (IllegalArgumentException e) {
            logger.error("sendResponseForAuctionByOwner - Product not found: " + productId);
            throw new IllegalArgumentException("Product not found");
        }
}

    public void acceptAssignment(int storeId, int userId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("acceptAssignment - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try{
            store.acceptAssignment(userId);
            logger.info("User " + userId + " accepted assignment to store " + storeId);
        }
        catch(Exception e){
            logger.error("acceptAssignment failed for user " + userId + " store " + storeId + " error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void declineAssignment(int storeId, int userId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("declineAssignment - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try{
            store.declineAssignment(userId);
            logger.info("User " + userId + " declined assignment to store " + storeId);
        }
        catch(Exception e){
            logger.error("declineAssignment failed for user " + userId + " store " + storeId + " error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Integer> getPendingOwners(int storeId, int requesterId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getPendingOwners - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try{
            List<Integer> pending = store.getPendingOwners(requesterId);
            logger.info("getPendingOwners success store " + storeId + " user " + requesterId);
            return pending;
        }
        catch (Exception e){
            logger.error("getPendingOwners failure - " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Integer> getPendingManagers(int storeId, int requesterId){
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getPendingManagers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try{
            List<Integer> pending = store.getPendingManagers(requesterId);
            logger.info("getPendingManagers success store " + storeId + " user " + requesterId);
            return pending;
        }
        catch (Exception e){
            logger.error("getPendingManagers failure - " + e.getMessage());
            throw e;
        }
    }

}
