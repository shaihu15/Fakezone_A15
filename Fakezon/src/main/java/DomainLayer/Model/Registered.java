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

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.helpers.AssignmentEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionEndedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionFailedToOwnersEvent;
import DomainLayer.Model.helpers.AuctionEvents.AuctionGotHigherBidEvent;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;

public class Registered extends User {
    protected HashMap<Integer, OrderDTO> orders; // orderId -> Order
    protected HashMap<Integer, List<Integer>> productsPurchase; // storeId -> List of productIDs
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role

    private String email;
    private String password;
    private int age;
    private Stack<SimpleEntry<Integer, String>> messagesFromUser; // storeID -> message
    private Queue<SimpleEntry<Integer, String>> messagesFromStore; // storeID -> message
    private Queue<SimpleEntry<Integer, String>> assignmentMessages; // storeID -> message
    private Queue<SimpleEntry<Integer, String>> auctionEndedMessages; // storeID -> message    \
    public Registered(String email, String password, LocalDate dateOfBirth,String state) {
        super();
        this.email = email;
        this.password = password;
        this.roles = new HashMap<>();
        this.isLoggedIn = false;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new LinkedList<>();
        assignmentMessages = new LinkedList<>();
        auctionEndedMessages = new LinkedList<>();
        this.orders = new HashMap<>();
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
        messagesFromStore = new LinkedList<>();
        assignmentMessages = new LinkedList<>();
        auctionEndedMessages = new LinkedList<>();
        this.orders = new HashMap<>();
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
        messagesFromUser.push(new SimpleEntry<>(storeID, message));

    }

    public void addMessageFromStore(SimpleEntry<Integer, String> message) {
        this.messagesFromStore.add(message);
    }

    public void addAuctionEndedMessage(SimpleEntry<Integer, String> message) {
        this.auctionEndedMessages.add(message);
    }

    public HashMap<Integer, String> getMessagesFromUser() {
        return messagesFromUser.stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
        
    }

    public HashMap<Integer, String> getMessagesFromStore() {
        return messagesFromStore.stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }
    public HashMap<Integer, String> getAssignmentMessages() {
        return assignmentMessages.stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }
    public HashMap<Integer, String> getAuctionEndedMessages() {
        return auctionEndedMessages.stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }
    public HashMap<Integer, String> getAllMessages() {
        HashMap<Integer, String> allMessages = new HashMap<>();
        allMessages.putAll(getMessagesFromStore());
        allMessages.putAll(getAssignmentMessages());
        allMessages.putAll(getAuctionEndedMessages());
        return allMessages;
    }

    @Override
    public boolean logout() {
        this.cart.clear();
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

    public HashMap<Integer, OrderDTO> getOrders() {
        return orders; // userID -> Order
    }
    public void saveOrder(OrderDTO order) {
        orders.put(order.getOrderId(), order);
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

    public void AssignmentMessages(SimpleEntry simpleEntry) {
        this.assignmentMessages.add(simpleEntry);
    }

    
}
