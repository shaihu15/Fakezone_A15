package InfrastructureLayer.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.RegisteredRole;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.User;
@Repository
public class UserRepository implements IUserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private Map<Integer, Registered> users;
    private Map<Integer, User> unsignedUsers; // Map to store unsigned (guest) users
    private Map<Integer, LocalDate> suspendedUsers; // Map of userId to suspension end date (null if permanent)
    private Set<Integer> systemAdmins; // Set of user IDs who are system administrators

    public UserRepository() {
        this.users = new HashMap<>();
        this.unsignedUsers = new HashMap<>(); // Initialize the unsigned users map
        this.suspendedUsers = new HashMap<>();
        this.systemAdmins = new HashSet<>();


        // USED BY UI - PUT IN A COMMENT IF NOT NEEDED
        init();
    }

    @Override
    public Optional<Registered> findByUserName(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email) && user instanceof Registered)
                .findFirst();
    }

    @Override
    public Optional<Registered> findById(int userID) {
        return Optional.ofNullable(users.get(userID));
    }

    @Override
    public List<Registered> findAll() {
        return List.copyOf(users.values());
    }

    @Override
    public void deleteByUserName(String email) {
        Optional<Registered> user = findByUserName(email);
        if (user.isPresent()) {
            users.remove(user.get().getUserId());
            // Also remove from suspended users if present
            suspendedUsers.remove(user.get().getUserId());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void addUser(Registered user) {
        System.out.println("Adding user with ID: " + user.getUserId());
        if (users.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " already exists.");
        }
        System.out.println("User with ID " + user.getUserId() + " does not exist in the repository. Proceeding to add.");
        users.put(user.getUserId(), user);
    }

    @Override
    public Optional<User> findAllById(int userID) {
        // First check registered users
        if (users.containsKey(userID)) {
            return Optional.ofNullable(users.get(userID));
        }
        // Then check unsigned users
        return Optional.ofNullable(unsignedUsers.get(userID));
    }

    /**
     * Suspend a user until a specific date. If endOfSuspension is null, the suspension is permanent.
     * 
     * @param userId The ID of the user to suspend
     * @param endOfSuspension The date when the suspension ends, or null for permanent suspension
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public void suspendUser(int userId, LocalDate endOfSuspension) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        suspendedUsers.put(userId, endOfSuspension);
    }

    /**
     * Remove suspension from a user
     * 
     * @param userId The ID of the user to unsuspend
     * @return true if the user was unsuspended, false if they weren't suspended
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public boolean unsuspendUser(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return suspendedUsers.remove(userId) != null;
    }

    /**
     * Check if a user is currently suspended
     * 
     * @param userId The ID of the user to check
     * @return true if the user is suspended, false otherwise
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public boolean isUserSuspended(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        
        LocalDate endDate = suspendedUsers.get(userId);
        if (endDate == null && suspendedUsers.containsKey(userId)) {
            // User is permanently suspended
            return true;
        } else if (endDate != null) {
            // Check if suspension period has ended
            return endDate.isAfter(LocalDate.now()) || endDate.isEqual(LocalDate.now());
        }
        
        return false;
    }

    /**
     * Get the end date of a user's suspension
     * 
     * @param userId The ID of the user to check
     * @return The end date of the suspension, or null if the suspension is permanent
     * @throws IllegalArgumentException If the user doesn't exist or isn't suspended
     */
    public LocalDate getSuspensionEndDate(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        if (!suspendedUsers.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " is not suspended.");
        }
        
        return suspendedUsers.get(userId);
    }

    /**
     * Get all suspended users
     * 
     * @return A list of all suspended users
     */
    public List<Registered> getAllSuspendedUsers() {
        List<Registered> result = new ArrayList<>();
        
        for (Integer userId : suspendedUsers.keySet()) {
            LocalDate endDate = suspendedUsers.get(userId);
            // Include users with permanent suspension or active temporary suspension
            if (endDate == null || endDate.isAfter(LocalDate.now()) || endDate.isEqual(LocalDate.now())) {
                Optional<Registered> user = findById(userId);
                user.ifPresent(result::add);
            }
        }
        
        return result;
    }

    /**
     * Cleanup expired suspensions
     * 
     * @return The number of expired suspensions that were removed
     */
    public int cleanupExpiredSuspensions() {
        List<Integer> expiredSuspensions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Map.Entry<Integer, LocalDate> entry : suspendedUsers.entrySet()) {
            LocalDate endDate = entry.getValue();
            if (endDate != null && endDate.isBefore(today)) {
                expiredSuspensions.add(entry.getKey());
            }
        }
        
        for (Integer userId : expiredSuspensions) {
            suspendedUsers.remove(userId);
        }
        
        return expiredSuspensions.size();
    }

    /**
     * Add a user as a system administrator
     * 
     * @param userId The ID of the user to make an admin
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public void addSystemAdmin(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        systemAdmins.add(userId);
    }

    /**
     * Remove system administrator privileges from a user
     * 
     * @param userId The ID of the user to remove admin privileges from
     * @return true if the user was an admin and privileges were removed, false if they weren't an admin
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public boolean removeSystemAdmin(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return systemAdmins.remove(userId);
    }

    /**
     * Check if a user is a system administrator
     * 
     * @param userId The ID of the user to check
     * @return true if the user is a system administrator, false otherwise
     * @throws IllegalArgumentException If the user doesn't exist
     */
    public boolean isSystemAdmin(int userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return systemAdmins.contains(userId);
    }

    /**
     * Get all system administrators
     * 
     * @return A list of all system administrators
     */
    public List<Registered> getAllSystemAdmins() {
        List<Registered> result = new ArrayList<>();
        
        for (Integer adminId : systemAdmins) {
            Optional<Registered> admin = findById(adminId);
            admin.ifPresent(result::add);
        }
        
        return result;
    }

    /**
     * Get the number of system administrators
     * 
     * @return The number of system administrators
     */
    public int getSystemAdminCount() {
        return systemAdmins.size();
    }
    
    /**
     * Add an unsigned (guest) user to the repository
     * 
     * @param user The user to add
     * @throws IllegalArgumentException If a user with the same ID already exists
     */
    public void addUnsignedUser(User user) {
        int userId = user.getUserId();
        if (unsignedUsers.containsKey(userId) || users.containsKey(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " already exists.");
        }
        unsignedUsers.put(userId, user);
    }
    
    /**
     * Find an unsigned user by ID
     * 
     * @param userId The ID of the user to find
     * @return The user wrapped in an Optional, or an empty Optional if not found
     */
    public Optional<User> findUnsignedUserById(int userId) {
        return Optional.ofNullable(unsignedUsers.get(userId));
    }
    
    /**
     * Get all unsigned users
     * 
     * @return A list of all unsigned users
     */
    public List<User> getAllUnsignedUsers() {
        return new ArrayList<>(unsignedUsers.values());
    }

    
    /**
     * Remove an unsigned user from the repository
     * 
     * @param userId The ID of the user to remove
     * @return true if the user was removed, false if they weren't found
     */
    public boolean removeUnsignedUser(int userId) {
        return unsignedUsers.remove(userId) != null;
    }
    
    /**
     * Check if a user ID belongs to an unsigned user
     * 
     * @param userId The ID to check
     * @return true if the ID belongs to an unsigned user, false otherwise
     */
    public boolean isUnsignedUser(int userId) {
        return unsignedUsers.containsKey(userId);
    }
    
    /**
     * Get the total count of unsigned users
     * 
     * @return The number of unsigned users
     */
    public int getUnsignedUserCount() {
        return unsignedUsers.size();
    }

    public void init(){
        logger.info("user repo init");
        //UID: 1001 founder of store 1001
        users.put(1001, new Registered("testFounder1001@gmail.com", "a12345", LocalDate.of(1998, 10, 15), "IL", 1001));
        this.findById(1001).get().addRole(1001, new StoreFounder());

        //UID: 1002 owner of store 1001
        users.put(1002, new Registered("testOwner1001@gmail.com", "a12345", LocalDate.of(1998, 10, 15), "IL", 1002));
        this.findById(1001).get().addRole(1001, new StoreOwner());

        //UID: 1003 manager of store 1001
        users.put(1003, new Registered("testManager1001@gmail.com", "a12345", LocalDate.of(1998, 10, 15), "IL", 1003));
        this.findById(1001).get().addRole(1001, new StoreManager());

        //UID: 1004 normal registered user
        users.put(1004, new Registered("testNormalUser1004@gmail.com", "a12345", LocalDate.of(1998, 10, 15), "IL", 1004));
        Registered uiUserNormal = this.findById(1004).get();
        uiUserNormal.addToBasket(1001, 1001, 1);
        uiUserNormal.addToBasket(1001, 1002, 2);

        systemAdmins.add(1001);
    }

    @Override
    public void clearAllData() {
        users.clear();
        
    }

    @Override
    public List<Registered> UsersWithRolesInStoreId(int storeID) {
        List<Registered> rolesInStore = new ArrayList<>();
        for (Registered user : users.values()) 
        {
            if(user.getAllRoles().containsKey(storeID))
                rolesInStore.add(user);
        }
        return rolesInStore;
    }

    @Override
    public boolean isUserRegistered(int userId) {
        return users.containsKey(userId) && users.get(userId) instanceof Registered;
    }


}
