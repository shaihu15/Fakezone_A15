package DomainLayer.Model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.RoleName;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Interfaces.IDiscountPolicy;
import DomainLayer.Interfaces.IStore;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionGotHigherBidEvent;
import DomainLayer.Model.helpers.OfferEvents.CounterOfferDeclineEvent;
import DomainLayer.Model.helpers.OfferEvents.CounterOfferEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferAcceptedByAll;
import DomainLayer.Model.helpers.OfferEvents.OfferAcceptedSingleOwnerEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferDeclinedEvent;
import DomainLayer.Model.helpers.OfferEvents.OfferReceivedEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.Node;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Model.helpers.Tree;
import DomainLayer.Model.helpers.UserMsg;

import jakarta.persistence.*;


import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "stores")
public class Store implements IStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storeid")
    private int storeID;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private boolean isOpen = true;
    
    @Column(nullable = false)
    private int storeFounderID; // store founder ID
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @MapKeyColumn(name = "user_id")
    private Map<Integer, StoreRating> Sratings; // HASH userID to store rating
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)//LAZY
    @JoinColumn(name = "owner_store_id")
    @MapKeyColumn(name = "product_id")
    private Map<Integer, StoreProduct> storeProducts; // HASH productID to store product
    
    // For now, let's skip AuctionProduct and complex policies for initial implementation
    @Transient
    private HashMap<Integer, AuctionProduct> auctionProducts; // HASH productID to auction product
    
    @Transient
    private HashMap<Integer, PurchasePolicy> purchasePolicies; // HASH policyID to purchase policy
    
    @Transient
    private HashMap<Integer, IDiscountPolicy> discountPolicies; // HASH policyID to discount policy
    
    //@ElementCollection
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "store_owners", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "owner_id")
    private List<Integer> storeOwners;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pending_owners", joinColumns = @JoinColumn(name = "store_id"))
    @MapKeyColumn(name = "appointee_id")
    @Column(name = "appointor_id")
    private Map<Integer, Integer> pendingOwners; // appointee : appointor
    
    // Store managers permissions - simplified for now
    @Transient
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers; // HASH userID to store manager perms
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id")
    private List<UserMsg> messagesFromUsers; // List for persistence
     
    @Transient
    private static final AtomicInteger policyIDCounter = new AtomicInteger(0);
    
    @Transient
    private ApplicationEventPublisher publisher;
    
    @Transient
    private static final Logger logger = LoggerFactory.getLogger(Store.class);
    
    @Transient
    private final ReentrantLock rolesLock = new ReentrantLock(); // ALWAYS ~LOCK~ ROLES BEFORE PRODUCTS IF YOU NEED BOTH!
    
    @Transient                                                                 
    private final ReentrantLock productsLock = new ReentrantLock(); // ALWAYS *UNLOCK* PRODS BEFORE LOCK IF YOU NEED BOTH
    
    @Transient
    private final ReentrantLock ratingLock = new ReentrantLock();
    
    @Transient
    private HashMap<Integer, List<StoreManagerPermission>> pendingManagersPerms; // HASH userID to PENDING store manager perms
    
    @Transient
    private HashMap<Integer, Integer> pendingManagers; // appointee : appointor
    
    @Transient
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Transient
    private HashMap<Integer, List<Offer>> offersOnProducts; // userId -> List of offers they submited 
    @Transient
    private final ReentrantLock offersLock = new ReentrantLock();
    @Transient
    private HashMap<Integer, List<Offer>> pendingOffers; // userId -> List of COUNTER offers waiting for the user to accept

    @Transient
    private Tree rolesTree;
    
    // Default constructor for JPA
    public Store() {
        initializeTransientFields();
    }

    public Store(String name, int founderID, ApplicationEventPublisher publisher) {
        this.storeFounderID = founderID;
        this.name = name;
        this.publisher = publisher;
        initializeCollections();
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public Store(String name, int founderID, ApplicationEventPublisher publisher, int storeId) {
        this.storeFounderID = founderID;
        this.name = name;
        this.storeID = storeId;
        this.publisher = publisher;
        initializeCollections();
    }

    private void initializeCollections() {
        this.storeOwners = new ArrayList<>();
        this.storeManagers = new HashMap<>();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.auctionProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.rolesTree = new Tree(storeFounderID); // founder = root
        this.storeOwners.add(storeFounderID);
        this.pendingOwners = new HashMap<>(); // appointee : appointor
        this.storeManagers = new HashMap<>(); // HASH userID to store manager
        this.messagesFromUsers = new ArrayList<>();
        this.pendingManagersPerms = new HashMap<>();
        this.pendingManagers = new HashMap<>();
        this.offersOnProducts = new HashMap<>();
        this.pendingOffers = new HashMap<>();
    }

    private void initializeTransientFields() {
        this.auctionProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.storeManagers = new HashMap<>();
        this.messagesFromUsers = new ArrayList<>();
        this.pendingManagersPerms = new HashMap<>();
        this.pendingManagers = new HashMap<>();
        this.offersOnProducts = new HashMap<>();
        this.pendingOffers = new HashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.publisher = null; // Will be injected/set when needed
        if (storeFounderID != 0) {
            this.rolesTree = new Tree(storeFounderID);
        }
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
    public void addRating(int userID, double rating, String comment) {
        ratingLock.lock();
        if (Sratings.containsKey(userID)) {
            Sratings.get(userID).updateRating(rating, comment);
        } else {
            Sratings.put(userID, new StoreRating(userID, rating, comment));
        }
        ratingLock.unlock();
    }

    @Override
    public void addStoreProductRating(int userID, int productID, double rating, String comment) {
        productsLock.lock();
        try{
            if (storeProducts.containsKey(productID)) {
                storeProducts.get(productID).addRating(userID, rating, comment);
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        finally{
            productsLock.unlock();
        }
    }

    public boolean addBidOnAuctionProduct(int requesterId, int productID, double bidAmount) {
        productsLock.lock();

        try{
            if (auctionProducts.containsKey(productID)) {
                AuctionProduct ap = auctionProducts.get(productID);
                if(ap.isDone()){
                    throw new IllegalArgumentException("Auction is Done");
                }
                int prevId = ap.addBid(requesterId, bidAmount);
                if(prevId == requesterId) return false;
                if(prevId != -1) handleRecivedHigherBid(prevId, productID);
                return true;
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        finally{
            productsLock.unlock();
        }
    }

    @Override
    public StoreProductDTO addStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity,
            PCategory category) {
        rolesLock.lock();
        productsLock.lock();
        try {
            if (!hasInventoryPermissions(requesterId)) {
                throw new IllegalArgumentException(
                        "User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if (storeProducts.containsKey(productID)) {
                throw new IllegalArgumentException("Product " + productID + " is already in store " + storeID);
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Product's quantity must be greater than 0");
            }
            if (basePrice <= 0) {
                throw new IllegalArgumentException("Product's base price must be greater than 0");
            }
            if (name == null || name.length() <= 0) {
                throw new IllegalArgumentException("Product's name can not be empty");
            }
           
            StoreProduct storeProduct = new StoreProduct(productID,storeID, name, basePrice, quantity, category);
            storeProducts.put(productID, storeProduct); //overrides old product
            return new StoreProductDTO(storeProduct); //returns the productDTO
        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
            rolesLock.unlock();
        }
    }

    @Override
    public void editStoreProduct(int requesterId, int productID, String name, double basePrice, int quantity) {
        rolesLock.lock();
        productsLock.lock();
        try {
            if (!hasInventoryPermissions(requesterId)) {
                throw new IllegalArgumentException(
                        "User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if (!storeProducts.containsKey(productID)) {
                throw new IllegalArgumentException("Product " + productID + " is not in store " + storeID);
            }
            if (basePrice <= 0) {
                throw new IllegalArgumentException("Product's base price must be greater than 0");
            }
            if (name == null || name.length() <= 0) {
                throw new IllegalArgumentException("Product's name can not be empty");
            }
            StoreProduct storeProduct = storeProducts.get(productID);
            storeProduct.setQuantity(quantity);
            storeProduct.setBasePrice(basePrice);
        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
            rolesLock.unlock();
        }

    }

    public void removeStoreProduct(int requesterId, int productID) {
        rolesLock.lock();
        productsLock.lock();
        try {
            if (!hasInventoryPermissions(requesterId)) {
                throw new IllegalArgumentException(
                        "User " + requesterId + " has insufficient inventory permissions for store " + storeID);
            }
            if (!storeProducts.containsKey(productID)) {
                throw new IllegalArgumentException("Product " + productID + " is not in store " + storeID);
            }

            if (auctionProducts.containsKey(productID)) {
                auctionProducts.remove(productID);
            }
            storeProducts.remove(productID);
        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
            rolesLock.unlock();
        }
    }

    // To Do: change the paramers of the function and decide on the structure of
    // purchase policy and discount policy
    @Override
    public void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.PURCHASE_POLICY))) {
                purchasePolicies.put(purchasePolicy.getPolicyID(), purchasePolicy);
            } else {
                throw new IllegalArgumentException(
                        "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addSimpleDiscountWithProductsScope(int userID, List<Integer> productIDs, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new SimpleDiscount(policyIDCounter.incrementAndGet(), percentage,
                     new ProductsDiscountScope(productIDs, storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            } else {
                throw new IllegalArgumentException(
                        "User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addSimpleDiscountWithStoreScope(int userID, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new SimpleDiscount(policyIDCounter.incrementAndGet(), percentage,
                     new StoreDiscountScope(storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addConditionDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new AndDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new ProductsDiscountScope(productIDs, storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addConditionDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new AndDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new StoreDiscountScope(storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addAndDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new AndDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new ProductsDiscountScope(productIDs, storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addAndDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new AndDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new StoreDiscountScope(storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addOrDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new OrDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new ProductsDiscountScope(productIDs, storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addOrDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new OrDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new StoreDiscountScope(storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addXorDiscountWithProductsScope(int userID, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new XorDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new ProductsDiscountScope(productIDs, storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }   

    @Override
    public void addXorDiscountWithStoreScope(int userID, List<Predicate<Cart>> conditions, double percentage) { 
        rolesLock.lock();
        try{
            if (isOwner(userID)
                    || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))) {
                IDiscountPolicy discountPolicy = new XorDiscount(policyIDCounter.incrementAndGet(), conditions, percentage,
                     new StoreDiscountScope(storeID, storeProducts));
                discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
            }
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public void addAuctionProduct(int requesterId, int productID, double basePrice, int MinutesToEnd) {
        rolesLock.lock();
        productsLock.lock();
        try {
            if (!hasInventoryPermissions(requesterId)) {
                throw new IllegalArgumentException(
                        "User with ID: " + requesterId + " has insufficient permissions for store ID: " + storeID);
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
                if (MinutesToEnd <= 0) {
                    throw new IllegalArgumentException(
                            "Minutes to end must be greater than 0 for auction product with ID: "
                                    + productID + " in store ID: " + storeID);
                }
                auctionProducts.put(productID, new AuctionProduct(storeProduct, basePrice, MinutesToEnd));
                scheduler.schedule(() -> {
                    handleAuctionEnd(productID);
                }, MinutesToEnd, TimeUnit.MINUTES);

            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }
        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
            rolesLock.unlock();
        }
    }

    @Override
    public void isValidPurchaseAction(int requesterId, int productID) {
        productsLock.lock();
        try {
            if (auctionProducts.containsKey(productID)) {
                AuctionProduct auctionProduct = auctionProducts.get(productID);
                if (auctionProduct.getMinutesToEnd() <= 0) {
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

            throw e;
        }
        finally{
            productsLock.unlock();
        }
    }

    public List<AuctionProduct> getAuctionProducts() {
        productsLock.lock();
        List<AuctionProduct> prods = new ArrayList<>(auctionProducts.values());
        productsLock.unlock();
        return prods;
    }

    private void handleRecivedHigherBid(int prevHigherBid, int productID) {
        if (auctionProducts.containsKey(productID)) {
            AuctionProduct auctionProduct = auctionProducts.get(productID);
            this.publisher.publishEvent(new AuctionGotHigherBidEvent(this.storeID, productID, prevHigherBid,
                    auctionProduct.getCurrentHighestBid()));

        }
    }

    private void handleAuctionEnd(int productID) {
        productsLock.lock();
        try{

            if (auctionProducts.containsKey(productID)) {
                AuctionProduct auctionProduct = auctionProducts.get(productID);
                    if (auctionProduct.getUserIDHighestBid() != -1) // if there was a bid
                    {
                        if(auctionProduct.getQuantity() > 0){ //if there is stock
                            //update owner
                            this.publisher.publishEvent(new AuctionEndedToOwnersEvent(this.storeID, productID,
                                auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid()));
                            //update winner
                            this.publisher.publishEvent(new AuctionApprovedBidEvent(this.storeID, auctionProduct.getProductID(),
                                auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid(),
                                auctionProduct.toDTO(storeID)));
                        }
                        else
                        {
                            //update winner for fail
                            this.publisher.publishEvent(new AuctionDeclinedBidEvent(this.storeID, auctionProduct.getProductID(),
                                auctionProduct.getUserIDHighestBid(), auctionProduct.getCurrentHighestBid()));
                            this.publisher.publishEvent(new AuctionFailedToOwnersEvent(this.storeID, productID,
                                auctionProduct.getCurrentHighestBid(), "Auction failed, product no longer available in store"));
                        }
                    } else {
                        //update owner that no bid were placed
                        this.publisher.publishEvent(new AuctionFailedToOwnersEvent(this.storeID, productID,
                                auctionProduct.getCurrentHighestBid(), "Auction failed, no bids were placed"));
                    }
                    auctionProduct.setIsDone(true);
                
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " is not an auction product in store ID: " + storeID);
            }
        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
        }
    }

    @Override
    public void receivingMessage(int userID, String message) {
        UserMsg userMsg = new UserMsg(userID, message);
        messagesFromUsers.add(userMsg);
    }

    @Override
    public void sendMessage(int managerId, int userID, String message) {
        rolesLock.lock();
        try {
            if (isOwner(managerId) || (isManager(managerId)
                    && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
                this.publisher.publishEvent(new ResponseFromStoreEvent(this.storeID, userID, message));

            } else {
                throw new IllegalArgumentException(
                        "User with ID: " + managerId + " has insufficient permissions for store ID: " + storeID);
            }

        }
        catch(Exception e){
            throw e;
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public Map<Integer,UserMsg> getMessagesFromUsers(int managerId) {
        rolesLock.lock();
        try {
            if (isOwner(managerId) || (isManager(managerId)
                    && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))) {
                Map<Integer, UserMsg> map = new HashMap<>();
                for (UserMsg msg : messagesFromUsers) {
                    map.put(msg.getMsgId(), msg);
                }
                return map;
            } else {
                throw new IllegalArgumentException(
                        "User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
            }

        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public Map<Integer, StoreProduct> getStoreProducts() {
        return storeProducts;
    }

    @Override
    public Map<Integer, StoreRating> getRatings() {
        return Sratings;
    }

    @Override
    public HashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }

    @Override
    public HashMap<Integer, IDiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }

    @Override
    public List<Integer> getStoreOwners(int requesterId) {
        rolesLock.lock();
        try {
            if (isOwner(requesterId) || (isManager(requesterId)
                    && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
                List<Integer> ownersCopy = new ArrayList<>(storeOwners); // copy of store owners
                return ownersCopy;
            } else {
                logger.warn("User {} tried to access store roles without permission for store {}", requesterId,
                        storeID);
                throw new IllegalArgumentException(
                        "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
            }

        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
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
        try {
            if (isOwner(requesterId) || (isManager(requesterId)
                    && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))) {
                HashMap<Integer, List<StoreManagerPermission>> managersCopy = copyStoreManagersMap();
                return managersCopy;
            } else {
                logger.warn("User {} tried to access store roles without permission for store {}", requesterId,
                        storeID);
                throw new IllegalArgumentException(
                        "User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
            }
        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
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
            this.publisher.publishEvent(new AssignmentEvent(storeID, appointee, RoleName.STORE_OWNER));
        }
        catch(Exception e){
            throw e;
        }

        finally {
            rolesLock.unlock();
        }
    }

    private void acceptStoreOwner(int appointor, int appointee) {
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
        if (!isManager(appointee))
            rolesTree.addNode(appointor, appointee);
        storeManagers.remove(appointee); // does nothing if they're not a manager
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor,
                "User " + appointee + " approved your ownership assignment"));
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointee,
                "Successfully added ownership permissions for store " + storeID));

    }

    private void declineStoreOwner(int appointor, int appointee) {
        pendingOwners.remove(appointee);
        if (storeOwners.contains(appointor))
            this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor,
                    "User " + appointee + " declined your ownership assignment"));
    }

    @Override
    public boolean isOwner(int userId) {
        rolesLock.lock();
        try{
            return storeOwners.contains(userId);
        }
        finally{
            rolesLock.unlock();
        }
    }

    @Override
    public boolean isManager(int userId) {
        rolesLock.lock();
        try{
            return storeManagers.containsKey(userId);
        }
        finally{
            rolesLock.unlock();
        }
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
            this.publisher.publishEvent(new AssignmentEvent(storeID, appointee, RoleName.STORE_MANAGER));
        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    private void acceptStoreManager(int appointor, int appointee) {
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
        if (perms.isEmpty())
            throw new IllegalArgumentException("Permissions can not be empty"); // shouldn't happen
        storeManagers.put(appointee, perms);
        rolesTree.addNode(appointor, appointee);
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor,
                "User " + appointee + " approved your managment assignment for store " + storeID));
        this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointee,
                "Successfully added managment permissions for store " + storeID));
    }

    private void declineStoreManager(int appointor, int appointee) {
        pendingManagers.remove(appointee);
        pendingManagersPerms.remove(appointee);
        if (storeOwners.contains(appointor))
            this.publisher.publishEvent(new ResponseFromStoreEvent(storeID, appointor,
                    "User " + appointee + " declined your ownership assignment"));
    }

    @Override
    public boolean acceptAssignment(int userId) {
        rolesLock.lock();
        try {
            boolean ownership = pendingOwners.containsKey(userId);
            boolean managment = pendingManagers.containsKey(userId);
            if (ownership && managment) { // shouldn't happen
                throw new IllegalArgumentException("User " + userId + " pending for both ownership and managment");
            }
            if (!(ownership || managment)) {
                throw new IllegalArgumentException("User " + userId + " has no pending assignments");
            }
            if (ownership){
                acceptStoreOwner(pendingOwners.get(userId), userId);
                return ownership;
            }
            else{
                acceptStoreManager(pendingManagers.get(userId), userId);
                return ownership;
            }

        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public void declineAssignment(int userId) {
        rolesLock.lock();
        try {
            boolean ownership = pendingOwners.containsKey(userId);
            boolean managment = pendingManagers.containsKey(userId);
            if (ownership && managment) { // shouldn't happen
                throw new IllegalArgumentException("User " + userId + " pending for both ownership and managment");
            }
            if (!(ownership || managment)) {
                throw new IllegalArgumentException("User " + userId + " has no pending assignments");
            }
            if (ownership)
                declineStoreOwner(pendingOwners.get(userId), userId);
            else
                declineStoreManager(pendingManagers.get(userId), userId);

        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms) {
        rolesLock.lock();
        try {
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
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove) {
        rolesLock.lock();
        try {
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
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
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
            this.publisher.publishEvent(new ClosingStoreEvent(this.storeID));

        } else {
            throw new IllegalArgumentException(
                    "Requester ID: " + requesterId + " is not a Store Founder of store: " + storeID);
        }
    }

    public void closeStoreByAdmin(int adminId) {
        if (!this.isOpen) {
            throw new IllegalArgumentException("Store: " + storeID + " is already closed");
        }
        this.isOpen = false;
        // this.publisher.publishEvent(new ClosingStoreEvent(this.storeID));
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
        try {
            if (storeProducts.containsKey(productID)) {
                ProductRating rating = storeProducts.get(productID).getRatingByUser(userID);
                return rating;
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }

        }
        catch(Exception e){
            throw e;
        }
        finally{
            productsLock.unlock();
        }
    }

    @Override
    public void openStore(int userId) {
        if(userId == this.storeFounderID){
            if(!this.isOpen){
                this.isOpen = true;
            }
            else{
                throw new IllegalArgumentException("Store is Already Open");
            }
        }
        else{
            throw new IllegalArgumentException("Not Store Founder");
        }
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
        try {
            if (storeProducts.containsKey(productID)) {
                StoreProduct prod =  storeProducts.get(productID);
                return prod;
            } else {
                throw new IllegalArgumentException(
                        "Product with ID: " + productID + " does not exist in store ID: " + storeID);
            }

        }
        catch(Exception e){
            throw e;
        }
        finally {
            productsLock.unlock();
        }
    }

    public void removeStoreOwner(int requesterId, int toRemoveId) {
        rolesLock.lock();
        try {
            if (!isOwner(requesterId)) {
                throw new IllegalArgumentException("User with id: " + requesterId + " is not a valid store owner");
            }
            if (!isOwner(toRemoveId)) {
                throw new IllegalArgumentException("User with id: " + toRemoveId + " is not a valid store owner");
            }
            if (toRemoveId == storeFounderID) {
                throw new IllegalArgumentException("Can not remove Store Founder");
            }
            Node[] nodesArr = checkNodesValidity(requesterId, toRemoveId);
            Node fatherNode = nodesArr[0];
            Node childNode = nodesArr[1];
            storeOwners.remove(Integer.valueOf(childNode.getId())); // WRAPPED AS INTEGER BECAUSE OTHERWISE JAVA WANTS
                                                                    // TO
                                                                    // REMOVE AS INDEX - DO NOT CHANGE!!
            removeAllChildrenRoles(childNode); // remove all children from their respective roles list/hashmap
            if (requesterId != toRemoveId)
                fatherNode.removeChild(childNode); // remove child & all descendants from the actual tree

            removeOwnerFromAllOffers(toRemoveId);
        }
        catch(Exception e){
            throw e;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public void removeStoreManager(int requesterId, int toRemoveId) {
        rolesLock.lock();
        try {
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
            throw e;
        } 
        finally {
            rolesLock.unlock();
        }
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
            throw new IllegalArgumentException("Could Not Find fatherNode in rolesTree (id: " + requesterId + ")"); // should
                                                                                                                    // not
                                                                                                                    // happen
                                                                                                                    // -
                                                                                                                    // just
                                                                                                                    // for
                                                                                                                    // debugging
                                                                                                                    // purposes
        }
        if (childNode == null) {
            throw new IllegalArgumentException("Could Not Find childNode in rolesTree (id: " + requesterId + ")"); // should
                                                                                                                   // not
                                                                                                                   // happen
                                                                                                                   // -
                                                                                                                   // just
                                                                                                                   // for
                                                                                                                   // debugging
                                                                                                                   // purposes
        }
        if (requesterId != childId && !fatherNode.isChild(childNode)) {
            throw new IllegalArgumentException("Only " + childId + "'s appointor can change/remove their permissions");
        }
        return new Node[] { fatherNode, childNode };
    }

    private boolean hasInventoryPermissions(int id) {
        return (isOwner(id) || (isManager(id) && storeManagers.get(id).contains(StoreManagerPermission.INVENTORY)));
    }

    @Override
    public double calcAmount(int userId, Map<Integer, Integer> productToBuy, LocalDate dob, Cart cart) {
        if (!isOpen) {
            throw new IllegalArgumentException("Store is closed, can not purchase products");
        }
        if (productToBuy == null || productToBuy.isEmpty()) {
            throw new IllegalArgumentException("Product list is empty or null");
        }
        double amount = 0;

        Map<StoreProduct, Integer> products = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : productToBuy.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            if (!storeProducts.containsKey(productId)) {
                throw new IllegalArgumentException(
                        "Product with ID: " + productId + " does not exist in store ID: " + storeID);
            }
            StoreProduct product = storeProducts.get(productId);
            products.put(product, quantity);
            if (this.purchasePolicies.containsKey(productId)) {
                PurchasePolicy policy = this.purchasePolicies.get(productId);
                if (!policy.canPurchase(dob, productId, quantity)) {
                    throw new IllegalArgumentException(
                            "Purchase policy for product with ID: " + productId
                                    + " is not valid for the current basket.");
                }
            }
            if (auctionProducts.containsKey(productId)) {
                AuctionProduct auctionProduct = auctionProducts.get(productId);
                if (auctionProduct.getUserIDHighestBid() == userId && auctionProduct.isDone()) {
                    amount += auctionProduct.getCurrentHighestBid();
                    amount += auctionProduct.getBasePrice() * (quantity - 1); 
                }
                else{
                    amount += auctionProduct.getBasePrice() * quantity;
                }
                
            } else {
                Offer offer = getUserOfferOnStoreProduct(userId, productId);
                if(offer != null && offer.getUserId() == userId && offer.isApproved() && offer.isHandled()){
                    amount += offer.getOfferAmount();
                    if(quantity > 1){
                        amount += product.getBasePrice() * (quantity - 1);
                    }
                }
                else{
                    amount += product.getBasePrice() * quantity;
                }
            }
        }
        double totalDiscount = discountPolicies.values().stream()
        .mapToDouble(d -> d.apply(cart)).sum();
    
        amount -= totalDiscount;
        return amount;
    }


    @Override
    public boolean canViewOrders(int userId) {
        rolesLock.lock();
        boolean ans = storeOwners.contains(userId) || (storeManagers.containsKey(userId)
                && storeManagers.get(userId).contains(StoreManagerPermission.VIEW_PURCHASES));
        rolesLock.unlock();
        return ans;

    }   
    @Override
    public List<Integer> getPendingOwners(int requesterId){
        rolesLock.lock();
        try{
            if(!isOwner(requesterId) && !(isManager(requesterId) && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))){
                throw new IllegalArgumentException("User " + requesterId + " has insufficient permissions to view roles");
            }
            List<Integer> pending = new ArrayList<>(pendingOwners.keySet());
            return pending;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public List<Integer> getPendingManagers(int requesterId) {
        rolesLock.lock();

        try{
            if(!isOwner(requesterId) && !(isManager(requesterId) && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES))){
                throw new IllegalArgumentException("User " + requesterId + " has insufficient permissions to view roles");
            }
            List<Integer> pending = new ArrayList<>(pendingManagers.keySet());
            return pending;
        }
        finally {
            rolesLock.unlock();
        }
    }

    @Override
    public Map<StoreProductDTO, Boolean> checkIfProductsInStore(int userID, Map<Integer, Integer> products) {
        productsLock.lock();
        try{
            Map<StoreProductDTO, Boolean> productsInStore = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : products.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                if (storeProducts.containsKey(productId)) {
                    StoreProduct storeProduct = storeProducts.get(productId);
                    int newQuantity = Math.min(quantity, storeProduct.getQuantity());
                    if (quantity == newQuantity) {
                        productsInStore.put(new StoreProductDTO(storeProduct, newQuantity), true);
                    } else {
                        productsInStore.put(new StoreProductDTO(storeProduct, newQuantity), false);
                    }
                }
            }
            return productsInStore;
        }
        finally{
            productsLock.unlock();
        }
    }

    public Map<StoreProductDTO, Boolean> decrementProductsInStore(int userId, Map<Integer,Integer> productsToBuy)
    {
        productsLock.lock();
        try{
            Map<StoreProductDTO, Boolean> products = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : productsToBuy.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                StoreProduct storeProduct = storeProducts.get(productId);
                if (storeProduct == null) {
                    throw new IllegalArgumentException("Product with ID: " + productId + " does not exist in store ID: "
                            + storeID);
                }
                int newQuantity = Math.min(quantity, storeProduct.getQuantity());
                if(auctionProducts.containsKey(productId)){
                    AuctionProduct auctionProduct = auctionProducts.get(productId);
                    if(auctionProduct.getUserIDHighestBid() == userId  && auctionProduct.isDone()){
                        if(auctionProduct.getQuantity() < quantity) {
                            throw new IllegalArgumentException("Not enough quantity for product with ID: " + productId);
                        }
                        auctionProduct.setQuantity(auctionProduct.getQuantity() - quantity);
                        auctionProducts.remove(productId);
                    }
                }
                Offer offer = getUserOfferOnStoreProduct(userId, productId);
                if(offer != null){
                    removeOffer(offer);
                }
                if (newQuantity == quantity) { //this if was else-if, it might cause problems now?
                    products.put(new StoreProductDTO(storeProduct, quantity),true);
                    storeProduct.decrementProductQuantity(newQuantity);
                }
                else{
                    //storeProduct.decrementProductQuantity(newQuantity);
                    products.put(new StoreProductDTO(storeProduct, newQuantity),false);

                }
            }

            return products;
        }
        finally{
            productsLock.unlock();
        }

    }

    @Override
    public void returnProductsToStore(int userId, Map<Integer, Integer> products) {
        productsLock.lock();
        try{
            for (Map.Entry<Integer, Integer> entry : products.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                if (storeProducts.containsKey(productId)) {
                    StoreProduct storeProduct = storeProducts.get(productId);
                    storeProduct.incrementProductQuantity(quantity);
                }
            }
        }
        finally{
            productsLock.unlock();  
        }
    }

    @Override
    public List<ProductRating> getStoreProductAllRatings(int productId){
        //locks already happen in both sub methods
        try{
            StoreProduct prod = getStoreProduct(productId);
            return prod.getAllRatings();
        }
        catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<StoreManagerPermission> isManagerAndGetPerms(int userId){
        rolesLock.lock();
        try{
            if(storeManagers.containsKey(userId)){
                return new ArrayList<StoreManagerPermission>(storeManagers.get(userId));
            }
            else{
                return null;
            }
        }
        catch(Exception e){
            throw e;
        }
        finally{
            rolesLock.unlock();
        }

    }

    @Override
    public void placeOfferOnStoreProduct(int userId, int productId, double offerAmount){
        productsLock.lock();
        offersLock.lock();
        try{
            if(!storeProducts.containsKey(productId)){
                throw new IllegalArgumentException("Store Product " + productId + " Does Not Exist in Store " + storeID);
            }
            if(storeProducts.get(productId).getQuantity() < 1){
                throw new IllegalArgumentException("Product " + productId + " is out of stock");
            }
            if(offerAmount < 1){
                throw new IllegalArgumentException("Offer must be at least $1");
            }
            List<Offer> userOffers = offersOnProducts.get(userId);
            if (userOffers == null){
                userOffers = new ArrayList<>();
            }
            Offer offer = getUserOfferOnStoreProduct(userId, productId);
            if(offer != null){
                throw new IllegalArgumentException("Can not Offer on the Same Product Twice");
            }
            offer = getUserPendingOfferOnStoreProduct(userId, productId);
            if(offer != null){
                throw new IllegalArgumentException("User already has pending counter offer on the same product");
            }

            userOffers.add(new Offer(userId, this.storeID, productId, offerAmount, new ArrayList<>(this.storeOwners)));
            offersOnProducts.put(userId, userOffers);
            this.publisher.publishEvent(new OfferReceivedEvent(this.storeID, productId, userId, offerAmount));
        }
        finally{
            offersLock.unlock();
            productsLock.unlock();
        }
    }

    @Override
    public void acceptOfferOnStoreProduct(int ownerId, int userId, int productId){
        productsLock.lock();
        offersLock.lock();
        rolesLock.lock();
        try{
            if(!isOwner(ownerId)){
                throw new IllegalArgumentException("Only Store Owners can Accept Offers");
            }
            Offer offer = getUserOfferOnStoreProduct(userId, productId);
            if(offer == null){
                throw new IllegalArgumentException("User " + userId + " Did not place an Offer on Product " + productId);
            }
            offer.approve(ownerId);
            this.publisher.publishEvent(new OfferAcceptedSingleOwnerEvent(this.storeID, productId, userId, offer.getOfferAmount(), ownerId));
            if(offer.isApproved()){
                handleOfferDone(offer);
            }
        }
        finally{
            rolesLock.lock();
            offersLock.unlock();
            productsLock.unlock();
        }
    }

    @Override
    public void declineOfferOnStoreProduct(int ownerId, int userId, int productId){
        productsLock.lock();
        offersLock.lock();
        rolesLock.lock();
        try{
            if(!isOwner(ownerId)){
                throw new IllegalArgumentException("Only Store Owners can Decline Offers");
            }
            Offer offer = getUserOfferOnStoreProduct(userId, productId);
            if(offer == null){
                throw new IllegalArgumentException("User " + userId + " Did not place an Offer on Product " + productId);
            }
            offer.decline(ownerId);
            handleOfferDone(offer);
        }
        finally{
            rolesLock.lock();
            offersLock.unlock();
            productsLock.unlock();
        }
    }

    private Offer getUserOfferOnStoreProduct(int userId, int productId){
        offersLock.lock();
        try{
            if(offersOnProducts.containsKey(userId)){
                for(Offer offer : offersOnProducts.get(userId)){
                    if(offer.getProductId() == productId){
                        return offer;
                    }
                }
            }
            return null;
        }
        finally{
            offersLock.unlock();
        }
    }

    private void removeOwnerFromAllOffers(int ownerId){
        offersLock.lock();
        try{
            for(List<Offer> offersList : offersOnProducts.values()){
                for(Offer offer : offersList){
                    offer.removeOwner(ownerId);
                    if(offer.isApproved() && !offer.isHandled()){
                        handleOfferDone(offer);
                    }
                }
            }
        }
        finally{
            offersLock.unlock();
        }
    }

    private void handleOfferDone(Offer offer){
        offersLock.lock();
        try{
            if(offer.isApproved() && !offer.isHandled()){
                this.publisher.publishEvent(new OfferAcceptedByAll(storeID, offer.getProductId(), offer.getUserId(), offer.getOfferAmount()));
                offer.setHandled();
            }
            else{
                if(offer.isDeclined() && !offer.isHandled()){
                    this.publisher.publishEvent(new OfferDeclinedEvent(storeID, offer.getProductId(), offer.getUserId(), offer.getOfferAmount(), offer.getDeclinedBy()));
                    removeOffer(offer);
                    offer.setHandled();
                }
            }
        }
        finally{
            offersLock.unlock();
        }
    }

    private void removeOffer(Offer offer){
        offersLock.lock();
        try{
            List<Offer> offers = offersOnProducts.get(offer.getUserId());
            if(offers != null){
                offers.remove(offer);
            }
            if(offers == null || offers.isEmpty()){
                offersOnProducts.remove(offer.getUserId());
            }
        }
        finally{
            offersLock.unlock();
        }
    }

    @Override
    public void counterOffer(int ownerId, int userId, int productId, double offerAmount){
        offersLock.lock();
        rolesLock.lock();
        try{
            if(!isOwner(ownerId)){
                throw new IllegalArgumentException("User " + ownerId + " is not a valid Store Owner in store " + storeID);
            }
            Offer offer = getUserOfferOnStoreProduct(userId, productId);
            if(offer == null){
                throw new IllegalArgumentException("User " + userId + " Did not place an Offer on Product " + productId);
            }
            if(offerAmount < 1){
                throw new IllegalArgumentException("Counter offer must be more than $1");
            }
            offer = getUserPendingOfferOnStoreProduct(userId, productId);
            if(offer != null){
                throw new IllegalArgumentException("User already has a counter offer pending");
            }
            declineOfferOnStoreProduct(ownerId, userId, productId); // handles the decline too (msgs & all)
            Offer counterOffer = new Offer(userId, storeID, productId, offerAmount, List.copyOf(storeOwners));
            List<Offer> pendingUserOffers = pendingOffers.get(userId);
            if(pendingUserOffers == null){
                pendingUserOffers = new ArrayList<>();
            }
            pendingUserOffers.add(counterOffer);
            pendingOffers.put(userId, pendingUserOffers);
            this.publisher.publishEvent(new CounterOfferEvent(storeID, productId, userId, offerAmount));
        }
        finally{
            rolesLock.unlock();
            offersLock.unlock();
        }

    }

    private Offer getUserPendingOfferOnStoreProduct(int userId, int productId){
        offersLock.lock();
        try{
            if(pendingOffers.containsKey(userId)){
                for(Offer offer : pendingOffers.get(userId)){
                    if(offer.getProductId() == productId){
                        return offer;
                    }
                }
            }
            return null;
        }
        finally{
            offersLock.unlock();
        }
    }

    @Override
    public void acceptCounterOffer(int userId, int productId){
        offersLock.lock();
        try{
            Offer pendingOffer = getUserPendingOfferOnStoreProduct(userId, productId);
            if(pendingOffer == null){
                throw new IllegalArgumentException("User has no Pending Counter Offers");
            }
            removePendingOffer(pendingOffer);
            placeOfferOnStoreProduct(userId, productId, pendingOffer.getOfferAmount());
        }
        finally{
            offersLock.unlock();
        }
    }

    @Override
    public void declineCounterOffer(int userId, int productId){
        offersLock.lock();
        try{
            Offer pendingOffer = getUserPendingOfferOnStoreProduct(userId, productId);
            if(pendingOffer == null){
                throw new IllegalArgumentException("User has no Pending Counter Offers");
            }
            this.publisher.publishEvent(new CounterOfferDeclineEvent(storeID, productId, userId, pendingOffer.getOfferAmount()));
            removePendingOffer(pendingOffer);
        }
        finally{
            offersLock.unlock();
        }
    }

    private void removePendingOffer(Offer offer){
        offersLock.lock();
        try{
            List<Offer> offers = pendingOffers.get(offer.getUserId());
            if(offers != null){
                offers.remove(offer);
            }
            if(offers == null || offers.isEmpty()){
                pendingOffers.remove(offer.getUserId());
            }
        }
        finally{
            offersLock.unlock();
        }
    }

    public List<Offer> getUserOffers(int userId){
        List<Offer> offers = offersOnProducts.get(userId);
        if(offers == null){
            return new ArrayList<>();
        }
        return offers;
    }

    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
}