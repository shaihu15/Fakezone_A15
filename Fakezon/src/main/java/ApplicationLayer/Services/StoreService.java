package ApplicationLayer.Services;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.method.P;
import org.springframework.transaction.annotation.Transactional;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IStoreService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Offer;
import DomainLayer.Model.ProductRating;

import DomainLayer.Model.Registered;

import DomainLayer.Model.Store;
import DomainLayer.Model.StoreProduct;
import DomainLayer.Model.User;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Model.helpers.UserMsg;

import org.springframework.stereotype.Service;

@Service
public class StoreService implements IStoreService {
    private final IStoreRepository storeRepository;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    private final ApplicationEventPublisher publisher;

    public StoreService(IStoreRepository storeRepository, ApplicationEventPublisher publisher) {
        this.storeRepository = storeRepository;
        this.publisher = publisher;

        // FOR UI PUT IN COMMENT IF NOT NEEDED!

        // init();
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
        storeRepository.save(store);
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
    public StoreDTO toStoreDTO(Store store) {
        int storeId = store.getId();
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

    @Transactional
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
    public void closeStore(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("closeStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.closeStore(requesterId);
        storeRepository.save(store);
        logger.info("Store closed: " + storeId + " by user: " + requesterId);
    }

    @Override
    public void closeStoreByAdmin(int storeId, int adminId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("closeStoreByAdmin - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.closeStoreByAdmin(adminId);
        storeRepository.save(store);
        logger.info("Store closed by admin: " + storeId + " by admin: " + adminId);
    }

    @Override
    public void updateStoreName(int storeId, String newName, int requesterId) {
    }

    @Override
    public StoreProductDTO addProductToStore(int storeId, int requesterId, int productId, String name, double basePrice,
            int quantity, PCategory category) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add store product " + productId
                    + " to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addProductToStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            logger.info("Store product added: " + productId + " to store: " + storeId + " by user: " + requesterId);
            StoreProductDTO result = store.addStoreProduct(requesterId, productId, name, basePrice, quantity, category);
            storeRepository.save(store);
            return result;
        } catch (Exception e) {
            logger.error("StoreService - failed to add store product " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateProductInStore(int storeId, int requesterId, int productId, String name, double basePrice,
            int quantity) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to update store product " + productId
                    + " in store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - updateProductToStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.editStoreProduct(requesterId, productId, name, basePrice, quantity);
            storeRepository.save(store);
        } catch (Exception e) {
            logger.error("StoreService - failed to update store product " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeProductFromStore(int storeId, int requesterId, int productId) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to remove store product " + productId
                    + " from store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - removeProductFromStore - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.removeStoreProduct(requesterId, productId);
            storeRepository.save(store);
        } catch (Exception e) {
            logger.error("StoreService - failed to remove store product " + e.getMessage());
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
        storeRepository.save(store);
        logger.info("Store rating added: " + storeId + " by user: " + userId + " with rating: " + rating);
    }

    @Override
    public void addStoreProductRating(int storeId, int productId, int userId, double rating, String comment) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addStoreProductRating - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.addStoreProductRating(userId, productId, rating, comment);
        storeRepository.save(store);
        logger.info("Store product rating added: " + productId + " by user: " + userId + " with rating: " + rating);
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
            storeRepository.save(store);
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
            storeRepository.save(store);
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
            storeRepository.save(store);
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
            storeRepository.save(store);
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
            storeRepository.save(store);
        } catch (Exception e) {
            logger.error("Store Service - failed to remove store manager");
            throw e;
        }
    }

    @Override
    public int addStore(int userId, String storeName) {
        if (storeName == null || storeName.isEmpty()) {
            logger.error("openStore - Store name is empty: " + storeName);
            throw new IllegalArgumentException("Store name is empty");
        }
        if (storeRepository.findByName(storeName) != null) {
            logger.error("openStore - Store name already exists: " + storeName);
            throw new IllegalArgumentException("Store name already exists");
        }
        Store store = new Store(storeName, userId, publisher);
        
        // Save the store first to get the database-generated ID
        storeRepository.addStore(store);
        
        // Now get the ID after it's been persisted
        int storeId = store.getId();
        logger.info("openStore - New store ID: " + storeId);
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
        storeRepository.save(store);
    }

    @Override
    public void sendMessageToUser(int managerId, int storeId, int userId, String message) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("sendMessage - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.sendMessage(managerId, userId, message);
        storeRepository.save(store);
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

    public void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice,
            int MinutesToEnd) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("addAuctionProductToStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.addAuctionProduct(requesterId, productID, basePrice, MinutesToEnd);
            storeRepository.save(store);
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
            if (store.addBidOnAuctionProduct(requesterId, productID, bid)) {
                storeRepository.save(store);
                logger.info("Bid added to auction product in store: " + storeId + " by user: " + requesterId
                        + " with product ID: " + productID + " and bid: " + bid);
            } else {
                logger.error("addBidOnAuctionProductInStore - Bid not valid: " + bid);
                throw new IllegalArgumentException("Bid not valid");
            }
        } catch (IllegalArgumentException e) {
            logger.error("addBidOnAuctionProductInStore  " + productID + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void isValidPurchaseActionForUserInStore(int storeId, int requesterId, int productId) {
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
    public Map<Integer, Double> calcAmount(int userId, Cart cart, LocalDate dob) {
        Map<Integer, Double> prices = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : cart.getAllProducts().entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> basket = entry.getValue();
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("calcAmount - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            double storeAmount = store.calcAmount(userId, basket, dob, cart);
            prices.put(storeId, storeAmount);
        }
        return prices;
    }

    @Override
    public boolean canViewOrders(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("canViewOrders - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.canViewOrders(userId);
    }

    public boolean acceptAssignment(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        boolean isowner;
        if (store == null) {
            logger.error("acceptAssignment - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            isowner = store.acceptAssignment(userId);
            storeRepository.save(store);
            logger.info("User " + userId + " accepted assignment to store " + storeId);
            return isowner;
        } catch (Exception e) {
            logger.error(
                    "acceptAssignment failed for user " + userId + " store " + storeId + " error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void declineAssignment(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("declineAssignment - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            store.declineAssignment(userId);
            storeRepository.save(store);
            logger.info("User " + userId + " declined assignment to store " + storeId);
        } catch (Exception e) {
            logger.error(
                    "declineAssignment failed for user " + userId + " store " + storeId + " error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Integer> getPendingOwners(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getPendingOwners - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            List<Integer> pending = store.getPendingOwners(requesterId);
            logger.info("getPendingOwners success store " + storeId + " user " + requesterId);
            return pending;
        } catch (Exception e) {
            logger.error("getPendingOwners failure - " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Integer> getPendingManagers(int storeId, int requesterId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getPendingManagers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            List<Integer> pending = store.getPendingManagers(requesterId);
            logger.info("getPendingManagers success store " + storeId + " user " + requesterId);
            return pending;
        } catch (Exception e) {
            logger.error("getPendingManagers failure - " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<StoreDTO, Map<StoreProductDTO, Boolean>> checkIfProductsInStores(
            int userID, Map<Integer, Map<Integer, Integer>> cart) {
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> result = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : cart.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> products = entry.getValue();
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("checkIfProductsInStores - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            Map<StoreProductDTO, Boolean> storeProducts = store.checkIfProductsInStore(userID, products);
            result.put(toStoreDTO(store), storeProducts);
        }
        return result;
    }

    @Override
    public Response<Map<Integer, UserMsg>> getMessagesFromUsers(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getMessagesFromUsers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        try {
            Map<Integer, UserMsg> messages = store.getMessagesFromUsers(userId);
            if (messages.isEmpty()) {
                logger.info("No messages found for store: " + storeId);
                return new Response<>(null, "No messages found", false, ErrorType.INVALID_INPUT, null);
            }
            logger.info("Messages retrieved for store: " + storeId);
            return new Response<Map<Integer, UserMsg>>(messages, "Messages retrieved successfully", true, null, null);
        } catch (Exception e) {
            System.out.println("Error during get messages: " + e.getMessage());
            logger.error("Error during get messages: " + e.getMessage());
            return new Response<>(null, "Error during get messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }

    }

    public void init() {
        logger.info("store service init");
        storeRepository.addStore(new Store("store1001", 1001, publisher, 1001));
        Store uiStore = storeRepository.findById(1001);
        uiStore.addStoreOwner(1001, 1002);
        uiStore.acceptAssignment(1002);
        uiStore.addStoreManager(1002, 1003, new ArrayList<>(List.of(StoreManagerPermission.INVENTORY)));
        uiStore.acceptAssignment(1003);
        uiStore.addStoreProduct(1001, 1001, "Product1001", 100.0, 10, PCategory.BOOKS);
        uiStore.addStoreProduct(1001, 1002, "Product1002", 200.0, 20, PCategory.MUSIC);
        storeRepository.save(uiStore);
    }

    @Override
    public Map<StoreDTO, Map<StoreProductDTO, Boolean>> decrementProductsInStores(
            int userID, Map<Integer, Map<Integer, Integer>> cart) {
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> result = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : cart.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> products = entry.getValue();
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("decrementProductsInStores - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            Map<StoreProductDTO, Boolean> storeProducts = store.decrementProductsInStore(userID, products);
            storeRepository.save(store);
            result.put(toStoreDTO(store), storeProducts);
        }
        return result;
    }

    public void returnProductsToStores(int userId, Map<Integer, Map<Integer, Integer>> products) {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : products.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> productsInStore = entry.getValue();
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("returnProductsToStores - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.returnProductsToStore(userId, productsInStore);
            storeRepository.save(store);
        }
    }

    @Override
    public void clearAllData() {
        storeRepository.clearAllData();
    }

    @Override
    public List<ProductRating> getStoreProductRatings(int storeId, int productID) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("returnProductsToStores - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.getStoreProductAllRatings(productID);
    }

    // Discount Policy Methods

    @Override
    public void addSimpleDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs,
            double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add simple discount with products scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addSimpleDiscountWithProductsScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addSimpleDiscountWithProductsScope(requesterId, productIDs, percentage);
            storeRepository.save(store);
            logger.info("Simple discount with products scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add simple discount with products scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addSimpleDiscountWithStoreScope(int storeId, int requesterId, double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add simple discount with store scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addSimpleDiscountWithStoreScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addSimpleDiscountWithStoreScope(requesterId, percentage);
            storeRepository.save(store);
            logger.info("Simple discount with store scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add simple discount with store scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addConditionDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs,
            List<Predicate<Cart>> conditions, double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add condition discount with products scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addConditionDiscountWithProductsScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addConditionDiscountWithProductsScope(requesterId, productIDs, conditions, percentage);
            storeRepository.save(store);
            logger.info(
                    "Condition discount with products scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add condition discount with products scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addConditionDiscountWithStoreScope(int storeId, int requesterId, List<Predicate<Cart>> conditions,
            double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add condition discount with store scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addConditionDiscountWithStoreScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addConditionDiscountWithStoreScope(requesterId, conditions, percentage);
            storeRepository.save(store);
            logger.info("Condition discount with store scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add condition discount with store scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addAndDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs,
            List<Predicate<Cart>> conditions, double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add AND discount with products scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addAndDiscountWithProductsScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addAndDiscountWithProductsScope(requesterId, productIDs, conditions, percentage);
            storeRepository.save(store);
            logger.info("AND discount with products scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add AND discount with products scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addAndDiscountWithStoreScope(int storeId, int requesterId, List<Predicate<Cart>> conditions,
            double percentage) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add AND discount with store scope to store "
                    + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addAndDiscountWithStoreScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addAndDiscountWithStoreScope(requesterId, conditions, percentage);
            storeRepository.save(store);
            logger.info("AND discount with store scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add AND discount with store scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addOrDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs,
            List<Predicate<Cart>> conditions, double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add OR discount with products scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addOrDiscountWithProductsScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addOrDiscountWithProductsScope(requesterId, productIDs, conditions, percentage);
            storeRepository.save(store);
            logger.info("OR discount with products scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add OR discount with products scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addOrDiscountWithStoreScope(int storeId, int requesterId, List<Predicate<Cart>> conditions,
            double percentage) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add OR discount with store scope to store "
                    + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addOrDiscountWithStoreScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addOrDiscountWithStoreScope(requesterId, conditions, percentage);
            storeRepository.save(store);
            logger.info("OR discount with store scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add OR discount with store scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addXorDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs,
            List<Predicate<Cart>> conditions, double percentage) {
        try {
            logger.info("Store Service - User " + requesterId
                    + " trying to add XOR discount with products scope to store " + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addXorDiscountWithProductsScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addXorDiscountWithProductsScope(requesterId, productIDs, conditions, percentage);
            storeRepository.save(store);
            logger.info("XOR discount with products scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add XOR discount with products scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addXorDiscountWithStoreScope(int storeId, int requesterId, List<Predicate<Cart>> conditions,
            double percentage) {
        try {
            logger.info("Store Service - User " + requesterId + " trying to add XOR discount with store scope to store "
                    + storeId);
            Store store = storeRepository.findById(storeId);
            if (store == null) {
                logger.error("Store Service - addXorDiscountWithStoreScope - Store not found: " + storeId);
                throw new IllegalArgumentException("Store not found");
            }
            store.addXorDiscountWithStoreScope(requesterId, conditions, percentage);
            storeRepository.save(store);
            logger.info("XOR discount with store scope added to store: " + storeId + " by user: " + requesterId);
        } catch (Exception e) {
            logger.error("StoreService - failed to add XOR discount with store scope: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isStoreOwner(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("isStoreOwner - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.isOwner(userId);
    }

    @Override // returns null if not manager
    public List<StoreManagerPermission> isStoreManager(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("isStoreOwner - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.isManagerAndGetPerms(userId);
    }

    @Override
    public void openStore(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("openStore - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.openStore(userId);
        storeRepository.save(store);
    }

    @Override
    public void placeOfferOnStoreProduct(int storeId, int userId, int productId, double offerAmount) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("placeOfferOnStoreProduct - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.placeOfferOnStoreProduct(userId, productId, offerAmount);
        storeRepository.save(store);
    }

    @Override
    public void acceptOfferOnStoreProduct(int storeId, int ownerId, int userId, int productId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("acceptOfferOnStoreProduct - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.acceptOfferOnStoreProduct(ownerId, userId, productId);
        storeRepository.save(store);
    }

    @Override
    public void declineOfferOnStoreProduct(int storeId, int ownerId, int userId, int productId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("declineOfferOnStoreProduct - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.declineOfferOnStoreProduct(ownerId, userId, productId);
        storeRepository.save(store);
    }

    @Override
    public void counterOffer(int storeId, int ownerId, int userId, int productId, double offerAmount) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("counterOffer - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.counterOffer(ownerId, userId, productId, offerAmount);
        storeRepository.save(store);
    }

    @Override
    public void acceptCounterOffer(int storeId, int userId, int productId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("acceptCounterOffer - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.acceptCounterOffer(userId, productId);
        storeRepository.save(store);
    }

    @Override
    public void declineCounterOffer(int storeId, int userId, int productId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("declineCounterOffer - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        store.declineCounterOffer(userId, productId);
        storeRepository.save(store);
    }

    @Override
    public List<Offer> getUserOffers(int storeId, int userId) {
        Store store = storeRepository.findById(storeId);
        if (store == null) {
            logger.error("getUserOffers - Store not found: " + storeId);
            throw new IllegalArgumentException("Store not found");
        }
        return store.getUserOffers(userId);
    }

}
