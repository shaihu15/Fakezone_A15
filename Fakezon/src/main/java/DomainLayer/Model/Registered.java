package DomainLayer.Model;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.AbstractMap.SimpleEntry;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.IRepository.IRegisteredRole;


public class Registered extends UserType{
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role
    private boolean isLoggedIn;
    private int userID;
    private String email;
    private String password;
    private HashMap<Integer, Order> orders; // orderId -> Order
    private HashMap<Integer,List<Integer>> productsPurchase; // storeId -> List of productIDs
    private Stack<SimpleEntry<Integer, String>> messagesFromUser; // storeID -> message
    private Queue<SimpleEntry<Integer, String>> messagesFromStore; // storeID -> message
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public Registered(String email, String password){
        super();
        this.userID = idCounter.incrementAndGet(); // auto-increment userID
        this.email = email;
        this.password = password;
        this.orders = new HashMap<>();
        this.productsPurchase = new HashMap<>();
        this.roles = new HashMap<>();
        this.isLoggedIn = true;
        messagesFromUser = new Stack<>();
        messagesFromStore = new LinkedList<>();
    }
    public void setproductsPurchase(int storeID, List<Integer> productsPurchase) {
        this.productsPurchase.put(storeID, productsPurchase);
    }

    public boolean isRegistered(){
        return true;
    }
    public void sendMessageToStore(int storeID, String message) {
        messagesFromUser.push(new SimpleEntry<>(storeID, message));

    }
    public void receivingMessageFromStore(int storeID, String message) {
        messagesFromStore.add(new SimpleEntry<>(storeID, message));
    }
    public List<SimpleEntry<Integer, String>> getMessagesFromUser() {
        return messagesFromUser;
    }
    public List<SimpleEntry<Integer, String>> getMessagesFromStore() {
        return messagesFromStore.stream().toList();
    }

    public boolean logout(){
        this.isLoggedIn = false;
        return true;
    }
    public void addRole(int storeID, IRegisteredRole role){ // sytem admin (storeID = -1)or store owner
        roles.put(storeID, role);
    }
    public void removeRole(int storeID){ // sytem admin (storeID = -1)or store owner
        if (roles.containsKey(storeID)) {
            roles.remove(storeID);
        } else {
            throw new IllegalArgumentException("Role not found for the given store ID.");
        }
    }
    public boolean didPurchaseStore(int storeID) {
        return productsPurchase.containsKey(storeID);
    }
    public boolean didPurchaseProduct(int storeID,int productID) {
        if (productsPurchase.containsKey(storeID)) {
            return productsPurchase.get(storeID).contains(productID);
        }
        return false;
    }
    public int getUserID() {
        return userID;
    }
    public String getEmail() {
        return email;
    }

    @Override
    public IRegisteredRole getRoleByStoreID(int storeID) {
        return roles.get(storeID); // system admin (storeID = -1)or store owner
    }

    @Override
    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        return roles; // system admin (storeID = -1)or store owner
    }

    @Override
    public HashMap<Integer, Order> getOrders() {
        return orders; // userID -> Order
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

	@Override
	public boolean addToCart(int storeID, ProductDTO product) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'addToCart'");
        //add product to productsPurchase
	}

    @Override
    public String getPassword(){
        return password;
    }
}
