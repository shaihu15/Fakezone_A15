package DomainLayer.Model;

import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.security.crypto.bcrypt.BCrypt;


import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.helpers.StoreMsg;

import jakarta.persistence.*;

@Entity
@Table(name = "registered_users")
@DiscriminatorValue("REGISTERED")
public class Registered extends User {
    @ElementCollection
    @CollectionTable(name = "user_product_purchases", 
                    joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "store_id")
    @Column(name = "product_ids")
    protected Map<Integer, List<Integer>> productsPurchase; // storeId -> List of productIDs
    
    @Transient
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role

    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "age")
    private int age;
    
    @Transient
    private List<StoreMsg> messagesFromUser;
    
    @Transient
    private Map<Integer, StoreMsg> messagesFromStore;//msgId -> StoreMsg
    
    @Transient
    private Map<Integer, StoreMsg> assignmentMessages;//msgId -> StoreMsg
    @Transient
    private Map<Integer, StoreMsg> auctionEndedMessages;//msgId -> StoreMsg
    @Transient
    private Map<Integer, StoreMsg> offersMessages;//msgId -> StoreMsg
    protected static final AtomicInteger MsgIdCounter = new AtomicInteger(0);

    // Default constructor for JPA
    protected Registered() {
        super(true); // Use JPA constructor
        initializeFields();
    }

    public Registered(String email, String password, LocalDate dateOfBirth,String state) {
        super(); // Use regular constructor with auto-generated ID
        this.email = email;
        setPassword(password);
        this.roles = new HashMap<>();
        this.isLoggedIn = false;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new ConcurrentHashMap<>();
        assignmentMessages = new ConcurrentHashMap<>();
        offersMessages = new ConcurrentHashMap<>();
        auctionEndedMessages = new ConcurrentHashMap<>();
        this.productsPurchase = new HashMap<>();
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public Registered(String email, String password, LocalDate dateOfBirth,String state, int userId) {
        super(userId);
        this.email = email;
        setPassword(password);
        this.roles = new HashMap<>();
        this.isLoggedIn = false;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new HashMap<>();
        assignmentMessages = new HashMap<>();
        offersMessages = new HashMap<>();
        auctionEndedMessages = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }

    private void initializeFields() {
        this.roles = new HashMap<>();
        this.messagesFromUser = new Stack<>();
        this.messagesFromStore = new HashMap<>();
        this.assignmentMessages = new HashMap<>();
        this.offersMessages = new HashMap<>();
        this.auctionEndedMessages = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }

    public void setPassword(String rawPassword) {
        this.password = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean checkPassword(String rawPassword) {
        return BCrypt.checkpw(rawPassword, this.password);
    }

    public void setproductsPurchase(int storeID, List<Integer> productsPurchase) {
        this.productsPurchase.put(storeID, productsPurchase);
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

    public void sendMessageToStore(int storeID, String message) {
        messagesFromUser.add(new StoreMsg(storeID,-1, message, null));

    }

    public int addMessageFromStore(StoreMsg message) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.messagesFromStore.put(msgId, message);
        return msgId;
    }

    public int addOfferMessage(StoreMsg message) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.offersMessages.put(msgId, message);
        return msgId;
    }

    public List<StoreMsg> getMessagesFromUser() {
        return messagesFromUser;
    }

    public Map<Integer, StoreMsg> getMessagesFromStore() {
        return messagesFromStore;
    }
    public Map<Integer, StoreMsg> getAssignmentMessages() {
        return assignmentMessages;
    }
    public Map<Integer, StoreMsg> getOffersMessages() {
        return offersMessages;
    }
    public Map<Integer, StoreMsg> getAllMessages() {
        Map<Integer, StoreMsg> allMessages = new HashMap<>();
        allMessages.putAll(getMessagesFromStore());
        allMessages.putAll(getAssignmentMessages());
        allMessages.putAll(getOffersMessages());
        return allMessages;
    }

    @Override
    public boolean logout() {
        this.isLoggedIn = false;
        return true;
    }

    public void addRole(int storeID, IRegisteredRole role) { // sytem admin (storeID = -1)or store owner
        roles.put(storeID, role);
    }

    public void removeRole(int storeID) { // sytem admin (storeID = -1)or store owner
        if (roles.containsKey(storeID)) {
            roles.remove(storeID);
        } else {
            throw new IllegalArgumentException("Role not found for the given store ID.");
        }
    }

    public boolean didPurchaseStore(int storeID) {
        return productsPurchase.containsKey(storeID);
    }

    public boolean didPurchaseProduct(int storeID, int productID) {
        if (productsPurchase.containsKey(storeID)) {
            return productsPurchase.get(storeID).contains(productID);
        }
        return false;
    }

    public String getEmail() {
        return email;
    }

    public IRegisteredRole getRoleByStoreID(int storeID) {
        return roles.get(storeID); // system admin (storeID = -1)or store owner
    }

    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        return roles; // system admin (storeID = -1)or store owner
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void login() {
        this.isLoggedIn = true;
    }

    public String getPassword() {
        return "*********"; // Return masked password for security
    }

    @Override
    public UserDTO toDTO() {
        return new UserDTO(userId, email, age);
    }

    public int addAssignmentMessage(StoreMsg msg) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.assignmentMessages.put(msgId, msg);
        return msgId;
    }

    @Override
    public void saveCartOrderAndDeleteIt() {
        Map<Integer,Map<Integer,Integer>> products = cart.getAllProducts();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : products.entrySet()) {
            int storeId = entry.getKey();
            Map<Integer, Integer> productQuantities = entry.getValue();
            for (Map.Entry<Integer, Integer> productEntry : productQuantities.entrySet()) {
                int productId = productEntry.getKey();
                if (!productsPurchase.containsKey(storeId)) {
                    productsPurchase.put(storeId, new ArrayList<>());
                }
                productsPurchase.get(storeId).add(productId);
            }
        }
        this.cart.clear();
    }

    public void removeAssignmentMessage(int storeId){
        assignmentMessages.values().removeIf(msg -> msg.getStoreId() == storeId);
    }

    public boolean removeMsgById(int msgId) {
        if (!messagesFromStore.containsKey(msgId) && !assignmentMessages.containsKey(msgId) && !offersMessages.containsKey(msgId)) {
            return false; // Message ID not found
        }
        messagesFromStore.remove(msgId);
        assignmentMessages.remove(msgId);
        offersMessages.remove(msgId);
        return true;
    }

    public int addAuctionEndedMessage(StoreMsg message) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.auctionEndedMessages.put(msgId, message);
        return msgId;
    }

    public Map<Integer, StoreMsg> getAuctionEndedMessages() {
        return auctionEndedMessages;
    }

    public void removeOfferMessage(int storeId, int productId, int offeredBy){
        for(Map.Entry<Integer, StoreMsg> entry : offersMessages.entrySet()){
            StoreMsg msg = entry.getValue();
            if(msg.getStoreId() == storeId && msg.getProductId() == productId && msg.getOfferedBy() == offeredBy){
                offersMessages.remove(entry.getKey());
                return;
            }
        }
    }


}
