package DomainLayer.Model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap.SimpleEntry;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.helpers.ClosingStoreEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Registered extends User {
    private HashMap<Integer, IRegisteredRole> roles; // storeID -> Role

    private String email;
    private String password;
    private int age;
    private HashMap<Integer, Order> orders; // orderId -> Order
    private HashMap<Integer, List<Integer>> productsPurchase; // storeId -> List of productIDs
    private Stack<SimpleEntry<Integer, String>> messagesFromUser; // storeID -> message
    private Queue<SimpleEntry<Integer, String>> messagesFromStore; // storeID -> message
    private  Queue<Integer> storesToClose;

    public Registered(String email, String password, LocalDate dateOfBirth) {
        super();
        this.email = email;
        this.password = password;
        this.orders = new HashMap<>();
        this.productsPurchase = new HashMap<>();
        this.roles = new HashMap<>();
        this.isLoggedIn = true;
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        messagesFromUser = new Stack<>();
        messagesFromStore = new LinkedList<>();
        storesToClose = new LinkedList<>();
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
    public void receivingMessageFromStore(int storeID, String message) {
        messagesFromStore.add(new SimpleEntry<>(storeID, message));
    }

    private boolean shouldHandle(ClosingStoreEvent event) {
        if(this.roles.get(event.getId()).getRoleName() == RoleName.STORE_OWNER) {
            return true;
        }
        return false;
    }

    @EventListener(condition = "#root.target.shouldHandle(#event)")
    public void handleCloseStore(ClosingStoreEvent event) {
        if(!isLoggedIn) {
            storesToClose.add(event.getId());
            return;
        }
        // your logic to send to UI
    }

    public List<SimpleEntry<Integer, String>> getMessagesFromUser() {
        return messagesFromUser;
    }

    public List<SimpleEntry<Integer, String>> getMessagesFromStore() {
        return messagesFromStore.stream().toList();
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

    public int getUserID() {
        return userID;
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

    public HashMap<Integer, Order> getOrders() {
        return orders; // userID -> Order
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

    public UserDTO toDTO() {
        return new UserDTO(userID, email, age);
    }

    
}
