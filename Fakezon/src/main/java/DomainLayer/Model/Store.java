package DomainLayer.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;



import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.helpers.*;
import java.util.AbstractMap.SimpleEntry;
public class Store {

    private String name;
    private int storeID;
    private boolean isOpen = true;
    private int storeFounderID; //store founder ID
    private HashMap<Integer, StoreRating> Sratings; //HASH userID to store rating
    private HashMap<Integer, StoreProduct> storeProducts; //HASH productID to store product
    private HashMap<Integer, AuctionProduct> auctionProducts; //HASH productID to auction product
    private HashMap<Integer, PurchasePolicy> purchasePolicies; //HASH policyID to purchase policy
    private HashMap<Integer, DiscountPolicy> discountPolicies; //HASH policyID to discount policy
    private List<Integer> storeOwners;
    private HashMap<Integer,Integer> pendingOwners; //appointee : appointor
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers; //HASH userID to store manager perms
    private Tree rolesTree;
    private Queue<SimpleEntry<Integer, String>> messagesFromUsers; //HASH userID to message
    private Stack<SimpleEntry<Integer, String>> messagesFromStore; //HASH userID to message
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    
    public Store(String name, int founderID) {
        this.storeFounderID = founderID;
        this.storeOwners = new ArrayList<>();
        //storeOwners.put(founderID, new StoreOwner(founderID, name));
        this.storeManagers = new HashMap<>();
        this.name = name;
        this.storeID = idCounter.incrementAndGet();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.auctionProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.rolesTree = new Tree(founderID);
        this.storeOwners.add(founderID);
        this.pendingOwners = new HashMap<>(); //appointee : appointor
        this.storeManagers = new HashMap<>(); //HASH userID to store manager
        this.messagesFromUsers = new LinkedList<>();
        this.messagesFromStore = new Stack<>();
    }

    public String getName() {
        return name;
    }
    
    public int getId(){
        return storeID;
    }
    //precondition: user is logged in and  previously made a purchase from the store - cheaked by service layer
    public void addRating(int userID, double rating, String comment) {
        if (Sratings.containsKey(userID)) {
            Sratings.get(userID).updateRating(rating, comment);
        } else {
            Sratings.put(userID, new StoreRating(userID, rating, comment));
        }
    }
    public void addStoreProductRating(int userID, int productID, double rating, String comment) {
        if (storeProducts.containsKey(productID)) {
            storeProducts.get(productID).addRating(userID, rating, comment);
        } else {
            throw new IllegalArgumentException("Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }
    public void addStoreProduct(int productID, String name, double basePrice, int quantity, ProductState state) {
        storeProducts.put(productID, new StoreProduct(productID, name, basePrice, quantity, state));
    }
    //To Do: change the paramers of the function and decide on the structure of purchase policy and discount policy
    public void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy) {
        if(isOwner(userID) || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.PURCHASE_POLICY))){
            purchasePolicies.put(purchasePolicy.getPolicyID(), purchasePolicy);
        }
        else{
            throw new IllegalArgumentException("User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }    }

    
    //To Do: change the paramers of the function and decide on the structure of purchase policy and discount policy
    public void addDiscountPolicy(int userID, DiscountPolicy discountPolicy) {
        if(isOwner(userID) || (isManager(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))){
            discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
        }
        else{
            throw new IllegalArgumentException("User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }
    }
    public void addAuctionProduct(int requesterId, int productID, double basePrice, int daysToEnd) {
        if(!isOwner(requesterId) && !(isManager(requesterId) && storeManagers.get(requesterId).contains(StoreManagerPermission.INVENTORY))){
            throw new IllegalArgumentException("User with ID: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
        if(storeProducts.containsKey(productID)){
            StoreProduct storeProduct = storeProducts.get(productID);
            auctionProducts.put(productID, new AuctionProduct(storeProduct, basePrice, daysToEnd));
        }
        else{
            throw new IllegalArgumentException("Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }
    public boolean addBidToAuctionProduct(int requesterId, int productID, double bidAmount) {
        if(auctionProducts.containsKey(productID)){
            return auctionProducts.get(productID).addBid(requesterId, bidAmount);
        }
        else{
            throw new IllegalArgumentException("Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }
    public void isValidPurchaseAction(int requesterId, int productID) {
        if(auctionProducts.containsKey(productID)){
            AuctionProduct auctionProduct = auctionProducts.get(productID);
            if(auctionProduct.getDaysToEnd() <= 0){
                throw new IllegalArgumentException("Auction for product with ID: " + productID + " has ended.");
            }
            if(auctionProduct.getUserIDHighestBid() != requesterId){
                throw new IllegalArgumentException("User with ID: " + requesterId + " is not the highest bidder for product with ID: " + productID);
            }
        }
        else{
            throw new IllegalArgumentException("The product with ID: " + productID + " is not an auction product.");
        }
    }
    

    public void receivingMessage(int userID, String message) {
        messagesFromUsers.add(new SimpleEntry<>(userID, message));
    }
    public void sendMessage(int managerId, int userID, String message) {
        if(isOwner(managerId) || (isManager(managerId) && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))){
            messagesFromStore.push(new SimpleEntry<>(userID, message));
        }
        else{
            throw new IllegalArgumentException("User with ID: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }
    public Queue<SimpleEntry<Integer, String>> getMessagesFromUsers(int managerId) {
        if(isOwner(managerId) || (isManager(managerId) && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))){
            return messagesFromUsers;
        }
        else{
            throw new IllegalArgumentException("User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }
    public Stack<SimpleEntry<Integer, String>> getMessagesFromStore(int managerId) {
        if (isOwner(managerId) || (isManager(managerId) && storeManagers.get(managerId).contains(StoreManagerPermission.REQUESTS_REPLY))){
            return messagesFromStore;
        } else {
            throw new IllegalArgumentException("User with id: " + managerId + " has insufficient permissions for store ID: " + storeID);
        }
    }

    
    public HashMap<Integer, StoreProduct> getStoreProducts() {
        return storeProducts;
    }
    public HashMap<Integer, StoreRating> getRatings() {
        return Sratings;
    }
    public HashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }
    public HashMap<Integer, DiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }
    public List<Integer> getStoreOwners(int requesterId) {
        if(isOwner(requesterId) || (isManager(requesterId) && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES) )){
            return new ArrayList<>(storeOwners); // copy of store owners
        }
        else{
            throw new IllegalArgumentException("User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
    }
    //so the list won't change outside of Store and mess with the hashmap
    private HashMap<Integer, List<StoreManagerPermission>> copyStoreManagersMap(){
        HashMap<Integer, List<StoreManagerPermission>> copy = new HashMap<>();
        for(Integer id : storeManagers.keySet()){
            copy.put(id, new ArrayList<>(storeManagers.get(id)));
        }
        return copy;
    }

    public HashMap<Integer,List<StoreManagerPermission>> getStoreManagers(int requesterId){
        if(isOwner(requesterId) || (isManager(requesterId) && storeManagers.get(requesterId).contains(StoreManagerPermission.VIEW_ROLES) )){
            return copyStoreManagersMap();
        }
        else{
            throw new IllegalArgumentException("User with id: " + requesterId + " has insufficient permissions for store ID: " + storeID);
        }
    }
    //TO DO: Send Approval Request
    public void addStoreOwner(int appointor, int appointee){
        if(!isOwner(appointor)){
            throw new IllegalArgumentException("Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
        }
        if(isOwner(appointee)){
            throw new IllegalArgumentException("User with ID: " + appointee + " is already a store owner for store with ID: " + storeID);
        }
        // relevant after notifs
        /*if(pendingOwners.containsKey(appointee)){
            throw new IllegalArgumentException("Already waiting for User with ID: " + appointee + "'s approval");
        }*/
        //pendingOwners.put(appointee, appointor); TO DO WHEN OBSERVER/ABLE IS IMPLEMENTED
        if(isManager(appointee)){
            Node appointeeNode = rolesTree.getNode(appointee);
            Node appointorNode = rolesTree.getNode(appointor);
            if(!appointorNode.getChildren().contains(appointeeNode)){
                throw new IllegalArgumentException("Only the manager with id: " + appointee + "'s appointor can reassign them as Owner");
            }
            storeManagers.remove(appointee); // how should reappointing a manager affect the tree?? right now - only their father can re-assign them
        }
        storeOwners.add(appointee);
        rolesTree.addNode(appointor, appointee);
    }
    //***will be relevant when observer/able is implemented***
    // public void approvalStoreOwner(int appointee){
    //     int appointor = pendingOwners.get(appointee);
    //     pendingOwners.remove(appointee);
    //     storeOwners.add(appointee);
    //     rolesTree.addNode(appointor, appointee);
    // }
    // public void declineStoreOwner(int appointee){
    //     pendingOwners.remove(appointee);
    // }

    public boolean isOwner(int userId){
        return storeOwners.contains(userId);
    }

    public boolean isManager(int userId){
        return storeManagers.containsKey(userId);
    }

    public void addStoreManager(int appointor, int appointee, List<StoreManagerPermission> perms){
        if(!isOwner(appointor)){
            throw new IllegalArgumentException("Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
        }
        if(isManager(appointee)){
            throw new IllegalArgumentException("User with ID: " + appointee + " is already a store manager for store with ID: " + storeID);
        }
        if(perms == null || perms.isEmpty()){
            throw new IllegalArgumentException("Permissions list is empty");
        }
        storeManagers.put(appointee, perms);
        rolesTree.addNode(appointor, appointee);
    }

    public void addManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> perms){
        if(!isOwner(requesterId)){
            throw new IllegalArgumentException("Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
        }
        if(!isManager(managerId)){
            throw new IllegalArgumentException("Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
        }
        List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
        for (StoreManagerPermission perm : perms) {
            if(!currentPerms.contains(perm))
                currentPerms.add(perm);
        }
    }

    public void removeManagerPermissions(int requesterId, int managerId, List<StoreManagerPermission> toRemove){
        if(!isOwner(requesterId)){
            throw new IllegalArgumentException("Requester ID: " + requesterId + " is not a valid store owner for store ID: " + storeID);
        }
        if(!isManager(managerId)){
            throw new IllegalArgumentException("Manager ID: " + requesterId + " is not a valid store manager for store ID: " + storeID);
        }
        List<StoreManagerPermission> currentPerms = storeManagers.get(managerId);
        List<StoreManagerPermission> copyCurrentPerms = new ArrayList<>(currentPerms);
        for(StoreManagerPermission perm : toRemove){
            if(!currentPerms.contains(perm)){
                storeManagers.put(managerId, copyCurrentPerms); //restore initial perms because method failed
                throw new IllegalArgumentException("can not remove permission: " + perm + " because manager: " + managerId + " does not have it. permissions reseted to original");
            }
            currentPerms.remove(perm);
        }
        if(currentPerms.isEmpty()){
            storeManagers.put(managerId, copyCurrentPerms); //restore initial perms because method failed
            throw new IllegalArgumentException("permissions can not be empty. reseting manager: " + managerId + " permissions to original");
        }
    }

    public int getStoreFounderID() {
        return storeFounderID;
    }
    public void closeStore(int requesterId) {
        if(requesterId == this.storeFounderID){
            if(!this.isOpen){
                throw new IllegalArgumentException("Store: " + storeID + " is already closed");
            }
            this.isOpen = false;
            //TODO: ADD NOTIFICATIONS SENDING
        }
        else{
            throw new IllegalArgumentException("Requester ID: " + requesterId + " is not a Store Founder of store: " + storeID);
        }
    }
    public StoreRating getStoreRatingByUser(int userID) {
        if (Sratings.containsKey(userID)) {
            return Sratings.get(userID);
        } else {
            throw new IllegalArgumentException("User with ID: " + userID + " has not rated the store yet.");
        }
    }
    public ProductRating getStoreProductRating(int userID, int productID) {
        if (storeProducts.containsKey(productID)) {
            return storeProducts.get(productID).getRatingByUser(userID);
        } else {
            throw new IllegalArgumentException("Product with ID: " + productID + " does not exist in store ID: " + storeID);
        }
    }
    public void openStore() {
        this.isOpen = true;
    }
    public boolean isOpen() {
        return isOpen;
    }

    public double getAverageRating() {
        double sum = 0;
        for (StoreRating rating : Sratings.values()) {
            sum += rating.getRating();
        }
        return sum / Sratings.size();
    }

}
