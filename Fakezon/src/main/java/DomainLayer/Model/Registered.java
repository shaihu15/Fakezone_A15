package DomainLayer.Model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.security.crypto.bcrypt.BCrypt;

import ApplicationLayer.DTO.UserDTO;
import DomainLayer.Enums.RoleName;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Model.helpers.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "registered_users")
@DiscriminatorValue("REGISTERED")
public class Registered extends User {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_product_purchases", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "store_id")
    @Column(name = "product_ids")
    protected Map<Integer, List<Integer>> productsPurchase = new HashMap<>(); // storeId -> List of productIDs

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    private List<UserRole> userRoles = new ArrayList<>();

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "age")
    private int age;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_messages_from_store", joinColumns = @JoinColumn(name = "user_id"))
    private List<StoreMsg> messagesFromStore = new ArrayList<>(); // List to allow multiple messages

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_assignment_messages", joinColumns = @JoinColumn(name = "user_id"))
    private List<StoreMsg> assignmentMessages = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_auction_ended_messages", joinColumns = @JoinColumn(name = "user_id"))
    private List<StoreMsg> auctionEndedMessages = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_offers_messages", joinColumns = @JoinColumn(name = "user_id"))
    private List<StoreMsg> offersMessages = new ArrayList<>();

    // Default constructor for JPA
    protected Registered() {
        super();
        initializeFields();
    }

    public Registered(String email, String password, LocalDate dateOfBirth, String state) {
        super(); // Use regular constructor with auto-generated ID
        this.email = email;
        setPassword(password);
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        this.productsPurchase = new HashMap<>();
    }

    /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public Registered(String email, String password, LocalDate dateOfBirth, String state, int userId) {
        super(userId);
        this.email = email;
        setPassword(password);
        this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        this.productsPurchase = new HashMap<>();
    }

    private void initializeFields() {
        this.productsPurchase = new HashMap<>();
        this.userRoles = new ArrayList<>();
    }

    private IRegisteredRole createRoleFromName(RoleName roleName) {
        switch (roleName) {
            case STORE_FOUNDER:
                return new StoreFounder();
            case STORE_OWNER:
                return new StoreOwner();
            case STORE_MANAGER:
                return new StoreManager();
            case SYSTEM_ADMINISTRATOR:
                return new SystemAdministrator();
            case UNASSIGNED:
                return new UnassignedRole();
            default:
                throw new IllegalArgumentException("Unknown role name: " + roleName);
        }
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

    public int addMessageFromStore(StoreMsg message) {
        this.messagesFromStore.add(message);
        return message.getMsgId();
    }

    public int addOfferMessage(StoreMsg message) {
        this.offersMessages.add(message);
        return message.getMsgId();
    }


    public Map<Integer, StoreMsg> getMessagesFromStore() {
        Map<Integer, StoreMsg> result = new HashMap<>();
        for (StoreMsg msg : messagesFromStore) {
            result.put(msg.getMsgId(), msg);
        }
        return result;
    }

    public Map<Integer, StoreMsg> getAssignmentMessages() {
        Map<Integer, StoreMsg> result = new HashMap<>();
        for (StoreMsg msg : assignmentMessages) {
            result.put(msg.getMsgId(), msg);
        }
        return result;
    }

    public Map<Integer, StoreMsg> getOffersMessages() {
        Map<Integer, StoreMsg> result = new HashMap<>();
        for (StoreMsg msg : offersMessages) {
            result.put(msg.getMsgId(), msg);
        }
        return result;
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

    public void addRole(int storeID, IRegisteredRole role) { // system admin (storeID = -1) or store owner
        // Persist to database
        userRoles.add(new UserRole(storeID, role.getRoleName()));
    }

    public void removeRole(int storeID) { // system admin (storeID = -1) or store owner
        // Remove from database
        if(!userRoles.removeIf(userRole -> userRole.getStoreId() == storeID)){
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
        for (UserRole userRole : userRoles) {
            if (userRole.getStoreId() == storeID) {
                return createRoleFromName(userRole.getRoleName());
            }
        }
        throw new IllegalArgumentException("Role not found for the given store ID.");
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
        this.assignmentMessages.add(msg);
        return msg.getMsgId();
    }

    @Override
    public void saveCartOrderAndDeleteIt() {
        Map<Integer, Map<Integer, Integer>> products = cart.getAllProducts();
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

    public void removeAssignmentMessage(int storeId) {
        for (StoreMsg msg : assignmentMessages) {
            if (msg.getStoreId() == storeId) {
                assignmentMessages.remove(msg);
            }
        }
    }

    public boolean removeMsgById(int msgId) {
        for (StoreMsg msg : messagesFromStore) {
            if (msg.getMsgId() == msgId) {
                messagesFromStore.remove(msg);
                return true;
            }
        }
        for (StoreMsg msg : assignmentMessages) {
            if (msg.getMsgId() == msgId) {
                assignmentMessages.remove(msg);
                return true;
            }
        }
        for (StoreMsg msg : offersMessages) {
            if (msg.getMsgId() == msgId) {
                offersMessages.remove(msg);
                return true;
            }
        }
        return false;
    }

    public int addAuctionEndedMessage(StoreMsg message) {
        // Ensure the message has the correct userId
        message.setUserId(this.userId);
        this.auctionEndedMessages.add(message);
        return message.getMsgId();
    }

    public Map<Integer, StoreMsg> getAuctionEndedMessages() {
        Map<Integer, StoreMsg> result = new HashMap<>();
        for (StoreMsg msg : auctionEndedMessages) {
            result.put(msg.getMsgId(), msg);
        }
        return result;
    }

    public void removeOfferMessage(int storeId, int productId, int offeredBy) {
        for (StoreMsg entry : offersMessages) {
            if (entry.getStoreId() == storeId && entry.getProductId() == productId && entry.getOfferedBy() == offeredBy) {
                offersMessages.remove(entry);
                return;
            }
        }
    }

    public HashMap<Integer, IRegisteredRole> getAllRoles() {
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        for (UserRole userRole : userRoles) {
            roles.put(userRole.getStoreId(), createRoleFromName(userRole.getRoleName()));
        }
        return roles;
    }

}
