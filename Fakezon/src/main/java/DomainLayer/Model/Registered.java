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
import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.helpers.StoreMsg;

public class Registered extends User {
    protected HashMap<Integer, List<Integer>> productsPurchase; // storeId -> List of productIDs
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role

    private String email;
    private String password;
    private int age;
    private List<StoreMsg> messagesFromUser;
    private Map<Integer, StoreMsg> messagesFromStore;//msgId -> StoreMsg
    private Map<Integer, StoreMsg> assignmentMessages;//msgId -> StoreMsg
    private Map<Integer, StoreMsg> auctionEndedMessages;//msgId -> StoreMsg
    protected static final AtomicInteger MsgIdCounter = new AtomicInteger(0);

    public Registered(String email, String password, LocalDate dateOfBirth,String state) {
        super();
        this.email = email;
        this.password = password;
        this.roles = new HashMap<>();
        this.isLoggedIn = false;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new HashMap<>();
        assignmentMessages = new HashMap<>();
        auctionEndedMessages = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public Registered(String email, String password, LocalDate dateOfBirth,String state, int userId) {
        super(userId);
        this.email = email;
        this.password = password;
        this.roles = new HashMap<>();
        this.isLoggedIn = false;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new HashMap<>();
        assignmentMessages = new HashMap<>();
        auctionEndedMessages = new HashMap<>();
        this.productsPurchase = new HashMap<>();
    }


    public void setproductsPurchase(int storeID, List<Integer> productsPurchase) {
        this.productsPurchase.put(storeID, productsPurchase);
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

    public void sendMessageToStore(int storeID, String message) {
        messagesFromUser.add(new StoreMsg(storeID,-1, message));

    }

    public int addMessageFromStore(StoreMsg message) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.messagesFromStore.put(msgId, message);
        return msgId;
    }

    public int addAuctionEndedMessage(StoreMsg message) {
        int msgId = MsgIdCounter.getAndIncrement();
        this.auctionEndedMessages.put(msgId, message);
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
    public Map<Integer, StoreMsg> getAuctionEndedMessages() {
        return auctionEndedMessages;
    }
    public Map<Integer, StoreMsg> getAllMessages() {
        Map<Integer, StoreMsg> allMessages = new HashMap<>();
        allMessages.putAll(getMessagesFromStore());
        allMessages.putAll(getAssignmentMessages());
        allMessages.putAll(getAuctionEndedMessages());
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
        return password;
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
        if (!messagesFromStore.containsKey(msgId) && !assignmentMessages.containsKey(msgId) && !auctionEndedMessages.containsKey(msgId)) {
            return false; // Message ID not found
        }
        messagesFromStore.remove(msgId);
        assignmentMessages.remove(msgId);
        auctionEndedMessages.remove(msgId);
        return true;
    }

}
