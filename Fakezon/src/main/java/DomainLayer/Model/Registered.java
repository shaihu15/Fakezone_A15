package DomainLayer.Model;

import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import DomainLayer.Model.helpers.ClosingStoreEvent;
import DomainLayer.Model.helpers.ResponseFromStoreEvent;

@Component
public class Registered extends User {

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

    private boolean shouldHandleClosingStore(ClosingStoreEvent event) {
        if(!roles.containsKey(event.getId())) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleClosingStore(#event)")
    public void handleCloseStore(ClosingStoreEvent event) {
        this.messagesFromStore.add(new SimpleEntry<>(event.getId(), "Store " + event.getId() + " is now close."));
        return;
        
        // your logic to send to UI
    }
    private boolean shouldHandleResposeFromStore(ResponseFromStoreEvent event) {
        if(event.getUserId() != this.userId) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleResposeFromStore(#event)")
    public void handleResposeFromStore(ResponseFromStoreEvent event) {
        this.messagesFromStore.add(new SimpleEntry<>(event.getStoreId(), event.getMessage()));
        return;
        
        // your logic to send to UI
    }
    private boolean shouldHandleAssignmentEvent(AssignmentEvent event) {
        if(event.getUserId() != this.userId) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleAssignmentEvent(#event)")
    public void handleAssignmentEvent(AssignmentEvent event) {
        
        this.assignmentMessages.add(new SimpleEntry<>(event.getStoreId(), "Please approve or decline this role: " + event.getRoleName()+
        " for store " + event.getStoreId()));
        return;
        
        // your logic to send to UI
    }

    private boolean shouldHandleAuctionEndedToOwnersEvent(AuctionEndedToOwnersEvent event) {
        if(!roles.containsKey(event.getStoreId())) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleAuctionEndedToOwnersEvent(#event)")
    public void handleAuctionEndedToOwnersEvent(AuctionEndedToOwnersEvent event) {
        if(!isLoggedIn) {
            this.auctionEndedMessages.add(new SimpleEntry<>(event.getStoreId(), "Auction ended for product " + event.getProductID() +". Highest bid was " + event.getCurrentHighestBid() +
            " by user " + event.getUserIDHighestBid()+". Please approve or decline this bid."));
            return;
        }
        // your logic to send to UI
    }

    private boolean shouldHandleAuctionFailedToOwnersEvent(AuctionFailedToOwnersEvent event) {
        if(!roles.containsKey(event.getStoreId())) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleAuctionFailedToOwnersEvent(#event)")
    public void handleAuctionFailedToOwnersEvent(AuctionFailedToOwnersEvent event) {
        if(!isLoggedIn) {
            this.messagesFromStore.add(new SimpleEntry<>(event.getStoreId(), "Auction failed for product " + event.getProductID() +". Base price was " + event.getBasePrice()+". "+event.getMessage()));
            return;
        }
        // your logic to send to UI
    }

    private boolean shouldHandleApprovedBidOnAuctionEvent(AuctionApprovedBidEvent event) {
        if(event.getUserIDHighestBid() != this.userId) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleApprovedBidOnAuctionEvent(#event)")
    public void handleApprovedBidOnAuctionEvent(AuctionApprovedBidEvent event) {
        if(!isLoggedIn) {
            this.messagesFromStore.add(new SimpleEntry<>(event.getStoreId(), "We are pleased to inform you that your bid has won the auction on product: "+event.getProductID()+", at a price of: "+event.getCurrentHighestBid()+"! The product has been added to your shopping cart, please purchase it as soon as possible."));
            addToBasket(event.getStoreId(), event.getStoreProductDTO().getProductId(), 1);
            return;
        }
        // your logic to send to UI
    }
    private boolean shouldHandleDeclinedBidOnAuctionEvent(AuctionDeclinedBidEvent event) {
        if(event.getUserIDHighestBid() != this.userId) {
            return false;
        }
        return true;
    }

    @EventListener(condition = "#root.target.shouldHandleDeclinedBidOnAuctionEvent(#event)")
    public void handleDeclinedBidOnAuctionEvent(AuctionDeclinedBidEvent event) {
        if(!isLoggedIn) {
            this.messagesFromStore.add(new SimpleEntry<>(event.getStoreId(), "We regret to inform you that the offer for product: "+event.getProductID()+" was not approved by the store."));
            return;
        }
        // your logic to send to UI
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

    
}
