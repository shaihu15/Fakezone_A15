package DomainLayer.Model;

import java.util.HashMap;

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
    private HashMap<Integer, StoreOwner> storeOwners; //HASH userID to store owner
    private HashMap<Integer, StoreManager> storeManagers; //HASH userID to store manager
    public Store(String name, int storeID, int founderID) {
        this.storeFounderID = founderID;
        this.storeOwners = new HashMap<>();
        //storeOwners.put(founderID, new StoreOwner(founderID, name));
        this.storeManagers = new HashMap<>();
        this.name = name;
        this.storeID = storeID;
        this.products = new HashMap<>();
        this.Sratings = new HashMap<>();
        this.storeProducts = new HashMap<>();
        this.purchasePolicies = new HashMap<>();
        this.discountPolicies = new HashMap<>();

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
        purchasePolicies.put(userID, purchasePolicy);
    }
    //To Do: change the paramers of the function and decide on the structure of purchase policy and discount policy
    public void addDiscountPolicy(int userID, DiscountPolicy discountPolicy) {
        discountPolicies.put(userID, discountPolicy);
    }
    
    public HashMap<Integer, StoreProduct> getStoreProducts() {
        return storeProducts;
    }
    public HashMap<Integer, StoreRating> getRatings() {
        return ratings;
    }
    public HashMap<Integer, PurchasePolicy> getPurchasePolicies() {
        return purchasePolicies;
    }
    public HashMap<Integer, DiscountPolicy> getDiscountPolicies() {
        return discountPolicies;
    }
    public HashMap<Integer, StoreOwner> getStoreOwners() {
        return storeOwners;
    }
    public HashMap<Integer, StoreManager> getStoreManagers() {
        return storeManagers;
    }
    public int getStoreFounderID() {
        return storeFounderID;
    }
    public void closerStore() {
        this.isOpen = false;
    }
    public void openStore() {
        this.isOpen = true;
    }
}
