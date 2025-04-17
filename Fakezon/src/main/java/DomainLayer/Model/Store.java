package DomainLayer.Model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.helpers.*;
public class Store {

    private String name;
    private int storeID;
    private boolean isOpen = true;
    private int storeFounderID; //store founder ID
    private HashMap<Integer, Product> products;
    private HashMap<Integer, StoreRating> Sratings; //HASH userID to store rating
    private HashMap<Integer, StoreProduct> storeProducts; //HASH productID to store product
    private HashMap<Integer, PurchasePolicy> purchasePolicies; //HASH policyID to purchase policy
    private HashMap<Integer, DiscountPolicy> discountPolicies; //HASH policyID to discount policy
    private List<Integer> storeOwners;
    private HashMap<Integer,Integer> pendingOwners; //appointee : appointor
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers; //HASH userID to store manager
    private Tree rolesTree;
    public Store(String name, int storeID, int founderID) {
        this.storeFounderID = founderID;
        this.storeOwners = new ArrayList<>();
        //storeOwners.put(founderID, new StoreOwner(founderID, name));
        this.storeManagers = new HashMap<>();
        this.name = name;
        this.storeID = storeID;
        this.products = new HashMap<>();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();
        this.rolesTree = new Tree(founderID);
        this.storeOwners.add(founderID);
        this.pendingOwners = new HashMap<>(); //appointee : appointor
        this.storeManagers = new HashMap<>(); //HASH userID to store manager
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
    public void addStoreProduct(int productID, String name, double basePrice, int quantity, ProductState state) {
        storeProducts.put(productID, new StoreProduct(productID, name, basePrice, quantity, state));
    }
    //To Do: change the paramers of the function and decide on the structure of purchase policy and discount policy
    public void addPurchasePolicy(int userID, PurchasePolicy purchasePolicy) {
        if(storeOwners.contains(userID) || (storeManagers.containsKey(userID) && storeManagers.get(userID).contains(StoreManagerPermission.PURCHASE_POLICY))){
            purchasePolicies.put(purchasePolicy.getPolicyID(), purchasePolicy);
        }
        else{
            throw new IllegalArgumentException("User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
        }    }
    //To Do: change the paramers of the function and decide on the structure of purchase policy and discount policy
    public void addDiscountPolicy(int userID, DiscountPolicy discountPolicy) {
        if(storeOwners.contains(userID) || (storeManagers.containsKey(userID) && storeManagers.get(userID).contains(StoreManagerPermission.DISCOUNT_POLICY))){
            discountPolicies.put(discountPolicy.getPolicyID(), discountPolicy);
        }
        else{
            throw new IllegalArgumentException("User with ID: " + userID + " has insufficient permissions for store ID: " + storeID);
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
    public List<Integer> getStoreOwners() {
        return storeOwners;
    }
    //TO DO: Send Approval Request
    public void addStoreOwner(int appointor, int appointee){
        if(storeOwners.contains(appointee)){
            throw new IllegalArgumentException("User with ID: " + appointee + " is already a store owner for store with ID: " + storeID);
        }
        if(pendingOwners.containsKey(appointee)){
            throw new IllegalArgumentException("Already waiting for User with ID: " + appointee + "'s approval");
        }
        if(!storeOwners.contains(appointor)){
            throw new IllegalArgumentException("Appointor ID: " + appointor + " is not a valid store owner for store ID: " + storeID);
        }
        //pendingOwners.put(appointee, appointor); TO DO WHEN OBSERVER/ABLE IS IMPLEMENTED
        
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
    
    public int getStoreFounderID() {
        return storeFounderID;
    }
    public void closerStore() {
        this.isOpen = false;
    }
    public void openStore() {
        this.isOpen = true;
    }
    public boolean isOpen() {
        return isOpen;
    }
}
