package DomainLayer.Model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.RoleName;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IStore;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.Node;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;
import DomainLayer.Model.helpers.Tree;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Store implements IStore {

    private String name;
    private int storeID;
    private boolean isOpen = true;
    private int storeFounderID; // store founder ID
    private HashMap<Integer, StoreRating> Sratings; // HASH userID to store rating
    private HashMap<Integer, StoreProduct> storeProducts; // HASH productID to store product
    private HashMap<Integer, AuctionProduct> auctionProducts; // HASH productID to auction product
    private HashMap<Integer, PurchasePolicy> purchasePolicies; // HASH policyID to purchase policy
    private HashMap<Integer, DiscountPolicy> discountPolicies; // HASH policyID to discount policy
    private List<Integer> storeOwners;
    private HashMap<Integer, Integer> pendingOwners; // appointee : appointor
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers; // HASH userID to store manager perms
    private Tree rolesTree;
    private Queue<SimpleEntry<Integer, String>> messagesFromUsers; // HASH userID to message
    private Stack<SimpleEntry<Integer, String>> messagesFromStore; // HASH userID to message
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final ApplicationEventPublisher publisher;
    private static final Logger logger = LoggerFactory.getLogger(Store.class);
    private final ReentrantLock rolesLock = new ReentrantLock();    // ALWAYS ~LOCK~ ROLES BEFORE PRODUCTS IF YOU NEED BOTH!
    private final ReentrantLock productsLock = new ReentrantLock(); // ALWAYS *UNLOCK* PRODS BEFORE LOCK IF YOU NEED BOTH
    private HashMap<Integer, List<StoreManagerPermission>> pendingManagersPerms; // HASH userID to PENDING store manager perms
    private HashMap<Integer, Integer> pendingManagers; // appointee : appointor
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Store(String name, int founderID, ApplicationEventPublisher publisher) {
        this.storeFounderID = founderID;
        this.storeOwners = new ArrayList<>();
        // storeOwners.put(founderID, new StoreOwner(founderID, name));
        this.storeManagers = new HashMap<>();
        this.name = name;
        this.storeID = idCounter.incrementAndGet();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.auctionProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.rolesTree = new Tree(founderID); // founder = root
        this.storeOwners.add(founderID);
        this.pendingOwners = new HashMap<>(); // appointee : appointor
        this.storeManagers = new HashMap<>(); // HASH userID to store manager
        this.messagesFromUsers = new LinkedList<>();
        this.messagesFromStore = new Stack<>();
        this.publisher = publisher;
        this.pendingManagersPerms = new HashMap<>();
        this.pendingManagers = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return storeID;
    }

    // precondition: user is logged in and previously made a purchase from the store
    // - cheaked by service layer
    @Override
    public synchronized void addRating(int userID, double rating, String comment) {
        if (Sratings.containsKey(userID)) {
            Sratings.get(userID).updateRating(rating, comment);
        } else {
            Sratings.put(userID, new StoreRating(userID, rating, comment));
        }
    }

    @Override
    public void addStoreProductRating(int userID, int productID, double rating, String comment) {
        productsLock.lock();
        if (storeProducts.containsKey(productID)) {
            storeProducts.get(productID).addRating(userID, rating, comment);
            productsLock.unlock();
        } else {
            productsLock.unlock();
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public boolean addBidOnAuctionProduct(int requesterId, int productID, double bidAmount) {
        productsLock.lock();
        if (auctionProducts.containsKey(productID)) {
            boolean ans = auctionProducts.get(productID).addBid(requesterId, bidAmount);
            productsLock.unlock();
            return ans;
        } else {
            productsLock.unlock();
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    

    @Override
    public StoreProductDTO addStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity, PCategory category) {
        rolesLock.lock();
        productsLock.lock();
        try{
            if(!hasInventoryPermissions(requesterId)){
                throw new IllegalArgumentException("User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if(storeProducts.containsKey(productID)){
                throw new IllegalArgumentException("Product " + productID + " is already in store " + storeID);
            }
            if(quantity <= 0){
                throw new IllegalArgumentException("Product's quantity must be greater than 0");
            }
            if(basePrice <= 0){
                throw new IllegalArgumentException("Product's base price must be greater than 0");
            }
            if(name == null || name.length() <= 0){
                throw new IllegalArgumentException("Product's name can not be empty");
            }
        }
        catch(Exception e){
            productsLock.unlock();
            rolesLock.unlock();
            throw e;
        }
        StoreProduct storeProduct = new StoreProduct(productID,storeID, name, basePrice, quantity, category);
        storeProducts.put(productID, storeProduct); //overrides old product
        productsLock.unlock();
        rolesLock.unlock();
        return new StoreProductDTO(storeProduct); //returns the productDTO
    }
    @Override
    public void editStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity) {
        rolesLock.lock();
        productsLock.lock();
        try{
            if(!hasInventoryPermissions(requesterId)){
                throw new IllegalArgumentException("User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if(!storeProducts.containsKey(productID)){
                throw new IllegalArgumentException("Product " + productID + " is not in store " + storeID);
            }
            if(quantity <= 0){
                throw new IllegalArgumentException("Product's quantity must be greater than 0");
            }
            if(basePrice <= 0){
                throw new IllegalArgumentException("Product's base price must be greater than 0");
            }
            if(name == null || name.length() <= 0){
                throw new IllegalArgumentException("Product's name can not be empty");
            }
        }
        catch(Exception e){
            productsLock.unlock();
            rolesLock.unlock();
            throw e;
        }
        StoreProduct storeProduct = storeProducts.get(productID);
        storeProducts.put(productID, new StoreProduct(productID,storeID, name, basePrice, quantity, storeProduct.getCategory())); //overrides old product
        productsLock.unlock();
        rolesLock.unlock();
    }
    public void removeStoreProduct(int requesterId, int productID){
        rolesLock.lock();
        productsLock.lock();
        try{
            if(!hasInventoryPermissions(requesterId)){
                throw new IllegalArgumentException("User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if(!storeProducts.containsKey(productID)){
                throw new IllegalArgumentException("Product " + productID + " is not in store " + storeID);
            }
        }
        catch(Exception e){
            productsLock.unlock();
            rolesLock.unlock();
            throw e;
        }
        if(auctionProducts.containsKey(productID)){
            auctionProducts.remove(productID);
        }
        storeProducts.remove(productID);
        productsLock.unlock();
        rolesLock.unlock();
    }


    // To Do: change the paramers of the function and decide on the structure of
    // purchase policy and discount policy
    @Override
    public void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy) {
        rolesLock.lock();
        if (isOwner(userID)
                || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.PURCHASE_POLICY))) {
            purchasePolicies.put(purchasePolicy.getPolicyID(), purchasePolicy);
        } else {
            rolesLock.unlock();
            throw new IllegalArgumentException(
                    "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }
        rolesLock.unlock();
    }

    // To Do: change the paramers of the function and decide on the structure of
    // purchase policy and discount policy
    @Override
    public void addDiscountPolicy(int userID, DiscountPolicy discountPolicy) {
        rolesLock.lock();
        if (isOwner(userID)
                || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
            discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
        } else {
            rolesLock.unlock();
            throw new IllegalArgumentException(
                    "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }
        rolesLock.unlock();
    }

    @Override
    public void addAuctionProduct(int requesterId, int productID, double basePrice, int daysToEnd) {
        rolesLock.lock();
        productsLock.lock();
        try{
            if(!hasInventoryPermissions(requesterId)){
                throw new IllegalArgumentException("User with ID: " + requesterId + " has insufficient permissions for store ID: " + storeID);
            }
            if (storeProducts.containsKey(productID)) {
                StoreProduct storeProduct = storeProducts.get(productID);
                if (storeProduct.getQuantity() <= 0) {
                    throw new IllegalArgumentException(
                            "Product with ID: " + productID + " is out of stock in store ID: " + storeID);
                }
                if (auctionProducts.containsKey(productID)) {
                    throw new IllegalArgumentException(
                            "Product with ID: " + productID + " is already an auction product in store ID: " + storeID);
                }
                if (basePrice <= 0) {
                    throw new IllegalArgumentException("Base price must be greater than 0 for auction product with ID: "
                            + productID + " in store ID: " + storeID);
                }
                if (daysToEnd <= 0) {
                    throw new IllegalArgumentException("Days to end must be greater than 0 for auction product with ID: "
                            + productID + " in store ID: " + storeID);
                }
                auctionProducts.put(productID, new AuctionProduct(storeProduct, basePrice, daysToEnd));
                scheduler.schedule(() -> {
                    handleAuctionEnd(productID);
                    //auctionProducts.remove(productID);
                }, daysToEnd, TimeUnit.DAYS);
        
                
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        catch(Exception e){
            productsLock.unlock();
            rolesLock.unlock();
            throw e;
        }
        productsLock.unlock();
        rolesLock.unlock();
    }

    @Override
    public boolean addBidToAuctionProduct(int requesterId, int productID, double bidAmount) {
        productsLock.lock();
        if (auctionProducts.containsKey(productID)) {
            boolean ans = auctionProducts.get(productID).addBid(requesterId, bidAmount);
            productsLock.unlock();
            return ans;
        } else {
            productsLock.unlock();
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }

    @Override
    public void isValidPurchaseAction(int requesterId, int productID) {
        productsLock.lock();
        try{
            if (auctionProducts.containsKey(productID)) {
                AuctionProduct auctionProduct = auctionProducts.get(productID);
                if (auctionProduct.getDaysToEnd() <= 0) {
                    throw new IllegalArgumentException("Auction for product with ID: " + productID + " has ended.");
                }
                if (auctionProduct.getUserIDHighestBid() != requesterId) {
                    throw new IllegalArgumentException("User with ID: " + requesterId
                            + " is not the highest bidder for product with ID: " + productID);
                }
            } else {
                throw new IllegalArgumentException("The product with ID: " + productID + " is not an auction product.");
            }
        }
        catch(Exception e){
            productsLock.unlock();
            throw e;
        }
        productsLock.unlock();
    }

    public List<AuctionProduct> getAuctionProducts() {
        productsLock.lock();
        List<AuctionProduct> prods = new ArrayList<>(auctionProducts.values());
        productsLock.unlock();
        return prods;
    }

    public void handleAuctionEnd(int productID) {
        if (auctionProducts.containsKey(productID)) {
            AuctionProduct auctionProduct = auctionProducts.get(productID);
            if (auctionProduct.getDaysToEnd() <= 0) {
                if(auctionProduct.getUserIDHighestBid() != -1) {
                    auctionProduct.setOwnersToApprove(storeOwners);
                    this.publisher.publishEvent(new AuctionEndedToOwnersEvent(this.storeID, productID, auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid()));
                }
                else {
                    this.publisher.publishEvent(new AuctionFailedToOwnersEvent(this.storeID, productID, auctionProduct.getBasePrice(),"Auction failed, no bids were placed"));
                }
            } else {
                throw new IllegalArgumentException("Auction for product with ID: " + productID + " has not ended yet.");
            }
        } else {
            throw new IllegalArgumentException(
                    "Product with ID: " + productID + " is not an auction product in store ID: " + storeID);
        }
    }

    @Override
    public void receivingMessage(int userID, String message) {
        messagesFromUsers.add(new SimpleEntry<>(userID, message));
    }

    public void receivedResponseForAuctionByOwner(int ownerId,int productID, boolean approved) {
        if(!isOwner(ownerId))
            throw new IllegalArgumentException("User with ID: " + ownerId + " is not a store owner");
        if (auctionProducts.containsKey(productID)) {
            AuctionProduct auctionProduct = auctionProducts.get(productID);
            if (approved) {
                auctionProduct.setBidApprovedByOwners(ownerId, true);
                handeleIfApprovedAuction(auctionProduct);
            } else {
                auctionProduct.setBidApprovedByOwners(ownerId, false);
                handeleIfDeclinedAuction(auctionProduct);
            }
        } else {
            throw new IllegalArgumentException("The product with ID: " + productID + " is not an auction product.");
        }
    }

    private void handeleIfApprovedAuction(AuctionProduct auctionProduct){
        if(auctionProduct.isApprovedByAllOwners()) {
            if(auctionProduct.getQuantity() <= 0) {
                this.publisher.publishEvent(new AuctionDeclinedBidEvent(this.storeID, auctionProduct.getProductID(), auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid()));
                this.publisher.publishEvent(new AuctionFailedToOwnersEvent(this.storeID, auctionProduct.getProductID(), auctionProduct.getBasePrice(),"Auction failed, out of stock"));
                throw new IllegalArgumentException("Product with ID: " + auctionProduct.getProductID() + " is out of stock in store ID: " + storeID);
            }
            auctionProduct.setQuantity(auctionProduct.getQuantity()-1);
            this.publisher.publishEvent(new AuctionApprovedBidEvent(this.storeID, auctionProduct.getProductID(), auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid(), auctionProduct.toDTO(storeID)));
        }
    }
    private void handeleIfDeclinedAuction(AuctionProduct auctionProduct){
        this.publisher.publishEvent(new AuctionDeclinedBidEvent(this.storeID, auctionProduct.getProductID(), auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid()));
        this.publisher.publishEvent(new AuctionFailedToOwnersEvent(this.storeID, auctionProduct.getProductID(), auctionProduct.getBasePrice(),"Auction failed, declined by owners"));

        auctionProducts.remove(auctionProduct.getProductID());

    }
    @Override
    public void sendMessage(int managerId, int userID, String message) {
        rolesLock.lock();
        try{
            if (isOwner(managerId) || (isManager(managerId)
                    && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
                messagesFromStore.push(new SimpleEntry<>(userID, message));
                this.publisher.publishEvent(new ResponseFromStoreEvent(this.storeID, userID, message));

            } else {
                throw new IllegalArgumentException(
                        "User with ID: " + managerId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId) {
        rolesLock.lock();
        try{
            if (isOwner(managerId) || (isManager(managerId)
                    && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
                        
                rolesLock.unlock();
                return messagesFromUsers;
            } else {
                throw new IllegalArgumentException(
                        "User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId) {
        rolesLock.lock();
        try{
            if (isOwner(managerId) || (isManager(managerId)
                    && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
                rolesLock.unlock();
                return messagesFromStore;
            } else {
                throw new IllegalArgumentException(
                        "User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public HashMap<Integer, StoreProduct> getStoreProducts() {
        return storeProducts;
    }

    @Override
    public HashMap<Integer, StoreRating> getRatings() {
        return Sratings;
    }

    @Override
    public HashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }

    @Override
    public HashMap<Integer, DiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }

    @Override
    public List<Integer> getStoreOwners(int requesterId) {
        rolesLock.lock();
        try{
            if (isOwner(requesterId) || (isManager(requesterId)
                    && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
                List<Integer> ownersCopy = new ArrayList<>(storeOwners); // copy of store owners
                rolesLock.unlock();
                return ownersCopy;
            } else {
                logger.warn("User {} tried to access store roles without permission for store {}", requesterId, storeID);
                throw new IllegalArgumentException(
                        "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    // so the list won't change outside of Store and mess with the hashmap
    // DO NOT ACCESS WITHOUT ROLESLOCK ACQUIRED
    private HashMap<Integer, List<StoreManagerPermission>> copyStoreManagersMap() {
        HashMap<Integer, List<StoreManagerPermission>> copy = new HashMap<>();
        for (Integer id : storeManagers.keySet()) {
            copy.put(id, new ArrayList<>(storeManagers.get(id)));
        }
        return copy;
    }

    @Override
    public HashMap<Integer, List<StoreManagerPermission>> getStoreManagers(int requesterId) {
        rolesLock.lock();
        try{
            if (isOwner(requesterId) || (isManager(requesterId)
                    && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
                HashMap<Integer, List<StoreManagerPermission>> managersCopy = copyStoreManagersMap();
                rolesLock.unlock();
                return managersCopy;
            } else {
                logger.warn("User {} tried to access store roles without permission for store {}", requesterId, storeID);
                throw new IllegalArgumentException(
                        "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public void addStoreOwner(int appointor, int appointee) {
        rolesLock.lock();
        try{
            if (!isOwner(appointor)) {
                throw new IllegalArgumentException(
                        "Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
            }
            if (isOwner(appointee)) {
                throw new IllegalArgumentException(
                        "User with ID: " + appointee + " is already a store owner for store with ID: " + storeID);
            }
            if(pendingManagers.containsKey(appointee)){
                throw new IllegalArgumentException("Already pending user " + appointee + " approval for managment");
            }
            if(pendingOwners.containsKey(appointee)){
                throw new IllegalArgumentException("Already pending user " + appointee + " approval for ownership");
            }
            if (isManager(appointee)) {
                Node appointeeNode = rolesTree.getNode(appointee);
                Node appointorNode = rolesTree.getNode(appointor);
                if (!appointorNode.isChild(appointeeNode)) {
                    throw new IllegalArgumentException(
                            "Only the manager with id: " + appointee + "'s appointor can reassign them as Owner");
                }
            }
            pendingOwners.put(appointee, appointor);
            rolesLock.unlock();
            this.publisher.publishEvent(new AssignmentEvent(storeID, appointee, RoleName.STORE_OWNER));
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    private void acceptStoreOwner(int appointor, int appointee){
        if (!isOwner(appointor)) { // could happen - removing assignment
            pendingOwners.remove(appointee);
            throw new IllegalArgumentException(
                    "Appointor ID: " + appointor + " is no longer a valid store owner for store ID: " + storeID);
        }
        if (isOwner(appointee)) { // shouldn't happen theoratically
            pendingOwners.remove(appointee); 
            throw new IllegalArgumentException(
                    "User with ID: " + appointee + " is already a store owner for store with ID: " + storeID);
        }
        pendingOwners.remove(appointee);
        storeOwners.add(appointee);
        if(!isManager(appointee))
            rolesTree.addNode(appointor, appointee);
        storeManagers.remove(appointee); // does nothing if they're not a manager
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor, "User " + appointee + " approved your ownership assignment"));
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointee, "Successfully added ownership permissions for store " + storeID));

    }

    private void declineStoreOwner(int appointor, int appointee){
        pendingOwners.remove(appointee);
        if(storeOwners.contains(appointor))
            this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor, "User " + appointee + " declined your ownership assignment"));
    }

    @Override
    public boolean isOwner(int userId) {
        return storeOwners.contains(userId);
    }

    @Override
    public boolean isManager(int userId) {
        return storeManagers.containsKey(userId);
    }

    @Override
    public void addStoreManager(int appointor, int appointee, List<StoreManagerPermission> perms) {
        rolesLock.lock();
        try{
            if (!isOwner(appointor)) {
                throw new IllegalArgumentException(
                        "Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
            }
            if (isManager(appointee) || isOwner(appointee)) {
                throw new IllegalArgumentException(
                        "User with ID: " + appointee + " is already a store manager/owner for store with ID: " + storeID);
            }
            if(pendingManagers.containsKey(appointee)){
                throw new IllegalArgumentException("Already pending user " + appointee + " approval for managment");
            }
            if(pendingOwners.containsKey(appointee)){
                throw new IllegalArgumentException("Already pending user " + appointee + " approval for ownership");
            }
            if (perms == null || perms.isEmpty()) {
                throw new IllegalArgumentException("Permissions list is empty");
            }
            pendingManagersPerms.put(appointee, new ArrayList<>(perms));
            pendingManagers.put(appointee, appointor);
            rolesLock.unlock();
            this.publisher.publishEvent(new AssignmentEvent(storeID, appointee, RoleName.STORE_MANAGER));
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    private void acceptStoreManager(int appointor, int appointee){
        if (!isOwner(appointor)) { // could happen - removing assignment
            pendingManagers.remove(appointee);
            pendingManagersPerms.remove(appointee);
            throw new IllegalArgumentException(
                    "Appointor ID: " + appointor + " is no longer a valid store owner for store ID: " + storeID);
        }
        if (isOwner(appointee) || isManager(appointee)) { // shouldn't happen theoratically
            pendingManagers.remove(appointee);
            pendingManagersPerms.remove(appointee);
            throw new IllegalArgumentException(
                    "User with ID: " + appointee + " is already a store owner/manager for store with ID: " + storeID);
        }
        List<StoreManagerPermission> perms = pendingManagersPerms.remove(appointee);
        pendingManagers.remove(appointee);
        if(perms.isEmpty())
            throw new IllegalArgumentException("Permissions can not be empty"); // shouldn't happen
        storeManagers.put(appointee, perms);
        rolesTree.addNode(appointor, appointee);
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor, "User " + appointee + " approved your managment assignment for store " + storeID));
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointee, "Successfully added managment permissions for store " + storeID));
    }

    private void declineStoreManager(int appointor, int appointee){
        pendingManagers.remove(appointee);
        pendingManagersPerms.remove(appointee);
        if(storeOwners.contains(appointor))
            this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor, "User " + appointee + " declined your ownership assignment"));
    }

    @Override
    public void acceptAssignment(int userId){
        rolesLock.lock();
        try{
            boolean ownership = pendingOwners.containsKey(userId);
            boolean managment = pendingManagers.containsKey(userId);
            if(ownership && managment){ // shouldn't happen
                throw new IllegalArgumentException("User " + userId + " pending for both ownership and managment"); 
            }
            if(!(ownership || managment)){
                throw new IllegalArgumentException("User " + userId + " has no pending assignments");
            }
            if(ownership)
                acceptStoreOwner(pendingOwners.get(userId), userId);
            else
                acceptStoreManager(pendingManagers.get(userId), userId);
            rolesLock.unlock();
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public void declineAssignment(int userId){
        rolesLock.lock();
        try{
            boolean ownership = pendingOwners.containsKey(userId);
            boolean managment = pendingManagers.containsKey(userId);
            if(ownership && managment){ // shouldn't happen
                throw new IllegalArgumentException("User " + userId + " pending for both ownership and managment"); 
            }
            if(!(ownership || managment)){
                throw new IllegalArgumentException("User " + userId + " has no pending assignments");
            }
            if(ownership)
                declineStoreOwner(pendingOwners.get(userId), userId);
            else
                declineStoreManager(pendingManagers.get(userId), userId);
            rolesLock.unlock();
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
    }

    @Override
    public void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms) {
        rolesLock.lock();
        try{
            if (!isOwner(requesterId)) {
                throw new IllegalArgumentException(
                        "Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
            }
            if (!isManager(managerId)) {
                throw new IllegalArgumentException(
                        "Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
            }
            checkNodesValidity(requesterId, managerId); // no need to hold the nodes here
            List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
            for (StoreManagerPermission perm : perms) {
                if (!currentPerms.contains(perm))
                    currentPerms.add(perm);
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
        rolesLock.unlock();
    }

    @Override
    public void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove) {
        rolesLock.lock();
        try{
            if (!isOwner(requesterId)) {
                throw new IllegalArgumentException(
                        "Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
            }
            if (!isManager(managerId)) {
                throw new IllegalArgumentException(
                        "Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
            }
            checkNodesValidity(requesterId, managerId); // no need to hold the nodes here
            List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
            List<StoreManagerPermission> copyCurrentPerms = new ArrayList<>(currentPerms);
            for (StoreManagerPermission perm : toRemove) {
                if (!currentPerms.contains(perm)) {
                    storeManagers.put(managerId, copyCurrentPerms); // restore initial perms because method failed
                    throw new IllegalArgumentException("can not remove permission: " + perm + " because manager: "
                            + managerId + " does not have it. permissions reseted to original");
                }
                currentPerms.remove(perm);
            }
            if (currentPerms.isEmpty()) {
                storeManagers.put(managerId, copyCurrentPerms); // restore initial perms because method failed
                throw new IllegalArgumentException(
                        "permissions can not be empty. reseting manager: " + managerId + " permissions to original");
            }
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
        rolesLock.unlock();
    }

    @Override
    public int getStoreFounderID() {
        return storeFounderID;
    }

    @Override
    public void closeStore(int requesterId) {
        if (requesterId == this.storeFounderID) {
            if (!this.isOpen) {
                throw new IllegalArgumentException("Store: " + storeID + " is already closed");
            }
            this.isOpen = false;
            //this.publisher.publishEvent(new ClosingStoreEvent(this.storeID));

        } else {
            throw new IllegalArgumentException(
                    "Requester ID: " + requesterId + " is not a Store Founder of store: " + storeID);
        }
    }

    @Override
    public StoreRating getStoreRatingByUser(int userID) {
        if (Sratings.containsKey(userID)) {
            return Sratings.get(userID);
        } else {
            throw new IllegalArgumentException("User with ID: " + userID + " has not rated the store yet.");
        }
    }

    @Override
    public ProductRating getStoreProductRating(int userID, int productID) {
        productsLock.lock();
        try{
            if (storeProducts.containsKey(productID)) {
                ProductRating rating = storeProducts.get(productID).getRatingByUser(userID);
                productsLock.unlock();
                return rating;
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        catch(Exception e){
            productsLock.unlock();
            throw e;
        }
    }

    @Override
    public void openStore() {
        this.isOpen = true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public double getAverageRating() {
        if (Sratings == null || Sratings.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (StoreRating rating : Sratings.values()) {
            sum += rating.getRating();
        }
        return sum / Sratings.size();
    }

    @Override
    public StoreProduct getStoreProduct(int productID) {
        productsLock.lock();
        try{
            if (storeProducts.containsKey(productID)) {
                StoreProduct prod =  storeProducts.get(productID);
                productsLock.unlock();
                return prod;
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        catch(Exception e){
            productsLock.unlock();
            throw e;
        }
    }

    public void removeStoreOwner(int requesterId, int toRemoveId) {
        rolesLock.lock();
        try{
            if (!isOwner(requesterId)) {
                throw new IllegalArgumentException("User with id: " + requesterId + " is not a valid store owner");
            }
            if (!isOwner(toRemoveId)) {
                throw new IllegalArgumentException("User with id: " + toRemoveId + " is not a valid store owner");
            }
            if(toRemoveId == storeFounderID){
                throw new IllegalArgumentException("Can not remove Store Founder");
            }
            Node[] nodesArr = checkNodesValidity(requesterId, toRemoveId);
            Node fatherNode = nodesArr[0];
            Node childNode = nodesArr[1];
            storeOwners.remove(Integer.valueOf(childNode.getId())); // WRAPPED AS INTEGER BECAUSE OTHERWISE JAVA WANTS TO
                                                                    // REMOVE AS INDEX - DO NOT CHANGE!!
            removeAllChildrenRoles(childNode); // remove all children from their respective roles list/hashmap
            if (requesterId != toRemoveId)
                fatherNode.removeChild(childNode); // remove child & all descendants from the actual tree
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
        rolesLock.unlock();
    }

    @Override
    public void removeStoreManager(int requesterId, int toRemoveId) {
        rolesLock.lock();
        try{
            if (!isOwner(requesterId)) {
                throw new IllegalArgumentException("User with id: " + requesterId + " is not a valid store owner");
            }
            if (!isManager(toRemoveId)) {
                throw new IllegalArgumentException("User with id: " + toRemoveId + " is not a valid store manager");
            }
            Node[] nodesArr = checkNodesValidity(requesterId, toRemoveId);
            Node fatherNode = nodesArr[0];
            Node childNode = nodesArr[1];
            if (!childNode.getChildren().isEmpty()) {
                throw new IllegalArgumentException("Manager with id " + toRemoveId + " has children in rolesTree");
            }
            storeManagers.remove(toRemoveId);
            fatherNode.removeChild(childNode);// remove child from the actual tree
        }
        catch(Exception e){
            rolesLock.unlock();
            throw e;
        }
        rolesLock.unlock();
    }
    private void removeAllChildrenRoles(Node toRemove) {
        List<Node> children = toRemove.getAllDescendants();
        for (Node child : children) {
            int childId = child.getId();
            if (storeOwners.contains(childId)) {
                storeOwners.remove(Integer.valueOf(childId));// WRAPPED AS INTEGER BECAUSE OTHERWISE JAVA WANTS TO
                                                             // REMOVE AS INDEX - DO NOT CHANGE!!
            } else {
                if (storeManagers.containsKey(childId)) {
                    storeManagers.remove(childId);
                } else {
                    throw new IllegalArgumentException("Node with id " + toRemove.getId() + " has descendant with id "
                            + childId + " with no roles on owners list/managers hashmap"); // should not happen - just
                                                                                           // for debugging purposes
                }
            }
        }
    }

    // returns [father,child]
    // allows requesterId = childId!!!
    private Node[] checkNodesValidity(int requesterId, int childId) {
        Node fatherNode = rolesTree.getNode(requesterId);
        Node childNode = rolesTree.getNode(childId);
        if (fatherNode == null) {
            throw new IllegalArgumentException("Could Not Find fatherNode in rolesTree (id: " + requesterId + ")"); // should not happen - just for debugging purposes
        }
        if (childNode == null) {
            throw new IllegalArgumentException("Could Not Find childNode in rolesTree (id: " + requesterId + ")"); // should not happen - just for debugging purposes
        }
        if (requesterId != childId && !fatherNode.isChild(childNode)) {
            throw new IllegalArgumentException("Only " + childId + "'s appointor can change/remove their permissions");
        }
        return new Node[] {fatherNode, childNode};
    }

    @Override
    public synchronized List<StoreProductDTO> decrementProductsQuantity(Map<Integer, Integer> productsToBuy, int userId) {
        List<StoreProductDTO> products = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : productsToBuy.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            StoreProduct storeProduct = storeProducts.get(productId);
            if (storeProduct == null) {
                throw new IllegalArgumentException("Product with ID: " + productId + " does not exist in store ID: "
                        + storeID);
            }
            if(auctionProducts.containsKey(productId)){
                AuctionProduct auctionProduct = auctionProducts.get(productId);
                if(auctionProduct.getUserIDHighestBid() != userId  && !auctionProduct.isApprovedByAllOwners()){
                    throw new IllegalArgumentException("User with ID: " + userId + " is not the highest bidder for product with ID: " + productId);
                }
                if(auctionProduct.getQuantity() < quantity) {
                    throw new IllegalArgumentException("Not enough quantity for product with ID: " + productId);
                }
                auctionProduct.setQuantity(auctionProduct.getQuantity() - quantity);
                auctionProducts.remove(productId);
            }
            else if (storeProduct.getQuantity() < quantity) {
                throw new IllegalArgumentException("Not enough quantity for product with ID: " + productId);
            }
            else{
                storeProduct.setQuantity(storeProduct.getQuantity() - quantity);
            }
            products.add(new StoreProductDTO(storeProduct, quantity));
        }
        
        return products;
    }

    private boolean hasInventoryPermissions(int id){
        return (isOwner(id) || (isManager(id) && storeManagers.get(id).contains(StoreManagerPermission.INVENTORY)));
    }



    @Override
    public double calcAmount(int userId,Map<Integer,Integer> productToBuy, LocalDate dob) {
        if(!isOpen) {
            throw new IllegalArgumentException("Store is closed, can not purchase products");
        }
        if(productToBuy == null || productToBuy.isEmpty()){
            throw new IllegalArgumentException("Product list is empty or null");
        }
        double amount = 0;
        Map<StoreProductDTO, Boolean> products = checkIfProductsInStore(productToBuy);
        for (Map.Entry<StoreProductDTO, Boolean> entry : products.entrySet()) {
            StoreProductDTO product = entry.getKey();
            int productId = product.getProductId();
            if(!storeProducts.containsKey(productId)) {
                throw new IllegalArgumentException(
                        "Product with ID: " + productId + " does not exist in store ID: " + storeID);
            }
            if (this.purchasePolicies.containsKey(productId)) {
                PurchasePolicy policy = this.purchasePolicies.get(productId);
                if (!policy.canPurchase(dob, productId, product.getQuantity())) {
                    throw new IllegalArgumentException(
                            "Purchase policy for product with ID: " + productId + " is not valid for the current basket.");
                }
            }
            if(auctionProducts.containsKey(productId)){
                AuctionProduct auctionProduct = auctionProducts.get(productId);
                if(auctionProduct.getUserIDHighestBid() == userId  && auctionProduct.isApprovedByAllOwners()){
                    amount += auctionProduct.getCurrentHighestBid();
                }
            }
            else
            {
                boolean isDiscountApplicable = true;
                DiscountPolicy discountPolicy = this.discountPolicies.get(productId);
                if(discountPolicy == null) {
                    isDiscountApplicable = false;
                }
                if (discountPolicy!= null) {
                    DiscountPolicy policy = this.discountPolicies.get(productId);
                    List<DiscountCondition> conditions = policy.getConditions();
                    for(DiscountCondition condition : conditions) {
                        boolean con = products.entrySet().stream().anyMatch(e ->
                        e.getKey().getProductId() == condition.getTriggerProductId() &&
                        e.getKey().getQuantity() < condition.getTriggerQuantity());
                        if (con){
                            isDiscountApplicable = false;
                            break;
                        }
                    }
                }
                if(isDiscountApplicable && discountPolicy != null) {
                    discountPolicy.calculateNewPrice(product.getBasePrice(), product.getQuantity());
                    amount += discountPolicy.calculateNewPrice(product.getBasePrice(), product.getQuantity());
                } else {
                    amount += product.getBasePrice() * product.getQuantity();
                }
            }
    }
        return amount;
    }

    @Override
    public boolean canViewOrders(int userId){
        rolesLock.lock();
        boolean ans = storeOwners.contains(userId) || (storeManagers.containsKey(userId) && storeManagers.get(userId).contains(StoreManagerPermission.VIEW_PURCHASES));
        rolesLock.unlock();
        return ans;
    }    @Override
    public List<Integer> getPendingOwners(int requesterId){
        rolesLock.lock();
        if(!isOwner(requesterId)){
            rolesLock.unlock();
            throw new IllegalArgumentException("User " + requesterId + " has insufficient permissions to view roles");
        }
        List<Integer> pending = new ArrayList<>(pendingOwners.keySet());
        rolesLock.unlock();
        return pending;
    }

    @Override
    public List<Integer> getPendingManagers(int requesterId){
        rolesLock.lock();
        if(!isOwner(requesterId)){
            rolesLock.unlock();
            throw new IllegalArgumentException("User " + requesterId + " has insufficient permissions to view roles");
        }
        List<Integer> pending = new ArrayList<>(pendingManagers.keySet());
        rolesLock.unlock();
        return pending;
    }

    @Override
    public Map<StoreProductDTO, Boolean> checkIfProductsInStore(Map<Integer,Integer> products) {
        Map<StoreProductDTO, Boolean> productsInStore = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : products.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            if (storeProducts.containsKey(productId)) {
                StoreProduct storeProduct = storeProducts.get(productId);
                if (storeProduct.getQuantity() >= quantity) {
                    productsInStore.put(new StoreProductDTO(storeProduct, quantity), true);
                } else {
                    productsInStore.put(new StoreProductDTO(storeProduct, quantity), false);
                }
            }
        }
        return productsInStore;
    }

}
