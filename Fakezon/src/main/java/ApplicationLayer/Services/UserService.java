package ApplicationLayer.Services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.Enums.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<Registered> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public List<Registered> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDTO registerUser(String email, String password, LocalDate dateOfBirth, String country) {
        Registered user = new Registered(email, password, dateOfBirth, country);
        // Check if the user already exists
        Optional<Registered> existingUser = userRepository.findByUserName(email);
        if (existingUser.isPresent()) {
            logger.error("User already exists: " + email);
            throw new IllegalArgumentException("User already exists");
        }
        // Check if the email is valid
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            logger.error("Invalid email format: " + email);
            throw new IllegalArgumentException("Invalid email format");
        }
        try {
            logger.info("Registering user: " + user.getEmail());
            userRepository.addUser(user);
        } catch (Exception e) {
            // Handle exception if needed
            System.out.println("Error during registration: " + e.getMessage());
            logger.error("Error during registration: " + e.getMessage());
            throw new IllegalArgumentException("Error during registration: " + e.getMessage());
        }
        UserDTO userDTO = user.toDTO();
        return userDTO;
    }

    @Override
    public void clearUserCart(int userId){
        Optional<User> optionalUser = userRepository.findAllById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.saveCartOrderAndDeleteIt();
            logger.info("User "+userId+" clear cart");
        } else {
            logger.warn("Clear cart failed: User with id {} not found", userId);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void deleteUser(String userName) {
        userRepository.deleteByUserName(userName);
        logger.info("User deleted: " + userName);
    }

    @Override
    public Optional<Registered> getUserById(int userID) {
        return userRepository.findById(userID);
    }

    @Override
    public void logout(String email) {
        Optional<Registered> optionalUser = userRepository.findByUserName(email);
        if (optionalUser.isPresent()) {
            Registered user = optionalUser.get();
            user.logout();
            logger.info("User logged out: " + email);
        } else {
            logger.warn("Logout failed: User with email {} not found", email);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public UserDTO login(String email, String password) {
        try {
            Optional<Registered> optionalUser = userRepository.findByUserName(email);
            if (optionalUser.isEmpty()) {
                logger.error("Login failed: User with email {} not found", email);
                throw new IllegalArgumentException("User not found");
            }
            Registered user = optionalUser.get();
            if (user.getPassword().equals(password)) {    
                logger.info("User password match " + email);
            } else {
                logger.error("Login failed: Incorrect password for user with email {}", email);
                throw new IllegalArgumentException("Incorrect password");
            }
            user.login();
            return user.toDTO();
        } catch (Exception e) {
            // Handle exception if needed
            logger.error("Error during login: " + e.getMessage());
            throw new IllegalArgumentException("Error during login: " + e.getMessage());
        }
    }

    @Override
    public void addRole(int userID, int storeID, IRegisteredRole role) {
        Optional<Registered> Registered = userRepository.findById(userID);
        if (Registered.isPresent()) {
            try {
                Registered.get().addRole(storeID, role);
                logger.info("Role added to user: " + userID + " for store: " + storeID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during add role: " + e.getMessage());
                logger.error("Error during add role: " + e.getMessage());
            }
        } else {
            logger.error("User not found: " + userID);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void removeRole(int userID, int storeID) {
        Optional<Registered> Registered = userRepository.findById(userID);
        if (Registered.isPresent()) {
            try {
                Registered.get().removeRole(storeID);
                logger.info("Role removed from user: " + userID + " for store: " + storeID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during remove role: " + e.getMessage());
                logger.error("Error during remove role: " + e.getMessage());
            }
        } else {
            logger.error("User not found: " + userID);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public IRegisteredRole getRoleByStoreID(int userID, int storeID) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                return user.get().getRoleByStoreID(storeID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get role: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return null;
    }

        @Override
        public HashMap<Integer, IRegisteredRole> getAllRoles(int userID) {
            Optional<Registered> user = userRepository.findById(userID);
            logger.info("Getting all roles for user: " + userID);
            if (!user.isPresent()) {
                logger.error("User not found with ID: " + userID);
                throw new IllegalArgumentException("User not found with ID: " + userID);
            }

            try {
                HashMap<Integer, IRegisteredRole> roles = user.get().getAllRoles();
                return roles != null ? roles : new HashMap<>();
            } catch (Exception e) {
                logger.error("Error retrieving roles for user " + userID + ": " + e.getMessage());
                throw new RuntimeException("Error retrieving roles for user " + userID + ": " + e.getMessage(), e);
            }
        }

    public boolean didPurchaseStore(int userID, int storeID) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                return user.get().didPurchaseStore(storeID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during check purchase: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return false;
    }

    public boolean didPurchaseProduct(int userID, int storeID, int productID) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                return user.get().didPurchaseProduct(storeID, productID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during check purchase: " + e.getMessage());
                logger.error("Error during check purchase: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return false;
    }

    @Override
    public Response<List<OrderDTO>> getOrdersByUser(int userID) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                List<OrderDTO> orders = user.get().getOrders().values().stream().toList();
                return new Response<>(orders, "Orders retrieved successfully", true, null, null);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get orders: " + e.getMessage());
                logger.error("Error during get orders: " + e.getMessage());
                return new Response<>(null, "Error during get orders: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
            }
        } else {
            logger.error("User not found: " + userID);
            return new Response<>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
        }
    }

    @Override
    public boolean isUserLoggedIn(int userID) {
        Optional<User> user = userRepository.findAllById(userID);
        if (user.isPresent()) {
            try {
                return user.get().isLoggedIn();
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during check login: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }

        return false;
    }

    @Override
    public void sendMessageToStore(int userID, int storeID, String message) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                user.get().sendMessageToStore(storeID, message);
                logger.info("Message sent to store: " + storeID + " from user: " + userID);
            } catch (Exception e) {
                // Handle exception if needed
                logger.error("Error during send message: " + e.getMessage());
                System.out.println("Error during send message: " + e.getMessage());
            }
        } else {
            logger.error("User not found: " + userID);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void addToBasket(int userId, int storeId, int productId, int quantity) {
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                user.get().addToBasket(storeId, productId, quantity);
                logger.info("Product added to basket: " + productId+ " from store: " + storeId + " by user: "
                        + userId);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during add to basket: " + e.getMessage());
                logger.error("Error during add to basket: " + e.getMessage());
            }
        } else {
            logger.error("User not found: " + userId);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> viewCart(int userId) {
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                return user.get().viewCart();
            } catch (Exception e) {
                System.out.println("Error during view cart: " + e.getMessage());
                throw new IllegalArgumentException("Error during view cart: " + e.getMessage(), e); // Rethrow for testability
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
    @Override
    public Cart getUserCart(int userId) {
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                return user.get().getCart();
            } catch (Exception e) {
                // Handle exception if needed
                logger.error("Error during get user cart: " + e.getMessage());
                throw new IllegalArgumentException("Error during get user cart: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void saveCartOrder(int userId) {
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                user.get().saveCartOrderAndDeleteIt();
                logger.info("Order saved for user: " + userId);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during save order: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        
    }


    public UserDTO convertUserToDTO(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return user.toDTO();
    }    

    @Override
    public Response<HashMap<Integer, String>> getAllMessages(int userID) {
        Optional<Registered> Registered = userRepository.findById(userID);
        if (Registered.isPresent()) {

            try {
                HashMap<Integer, String> messages = Registered.get().getAllMessages();
                if (messages.isEmpty()) {
                    logger.info("No messages found for user: " + userID);
                    return new Response<>(null, "No messages found", true, null, null);
                }
                logger.info("Messages retrieved for user: " + userID);
                return new Response<>(messages, "Messages retrieved successfully", true, null, null);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get messages: " + e.getMessage());
                logger.error("Error during get messages: " + e.getMessage());
                return new Response<>(null, "Error during get messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
            }
        } else {
            logger.error("User not found: " + userID);
            return new Response<>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getAssignmentMessages(int userID) {
        Optional<Registered> Registered = userRepository.findById(userID);
        if (Registered.isPresent()) {

            try {
                HashMap<Integer, String> messages = Registered.get().getAssignmentMessages();
                if (messages.isEmpty()) {
                    logger.info("No messages found for user: " + userID);
                    return new Response<>(null, "No messages found", true, null, null);
                }
                logger.info("Messages retrieved for user: " + userID);
                return new Response<>(messages, "Messages retrieved successfully", true, null, null);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get messages: " + e.getMessage());
                logger.error("Error during get messages: " + e.getMessage());
                return new Response<>(null, "Error during get messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
            }
        } else {
            logger.error("User not found: " + userID);
            return new Response<>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getAuctionEndedMessages(int userID) {
        Optional<Registered> Registered = userRepository.findById(userID);
        if (Registered.isPresent()) {

            try {
                HashMap<Integer, String> messages = Registered.get().getAuctionEndedMessages();
                if (messages.isEmpty()) {
                    logger.info("No messages found for user: " + userID);
                    return new Response<>(null, "No messages found", true, null, null);
                }
                logger.info("Messages retrieved for user: " + userID);
                return new Response<>(messages, "Messages retrieved successfully", true, null, null);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get messages: " + e.getMessage());
                logger.error("Error during get messages: " + e.getMessage());
                return new Response<>(null, "Error during get messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
            }
        } else {
            logger.error("User not found: " + userID);
            return new Response<>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
        }
    }

    @Override
    public Optional<User> getAnyUserById(int Id) {
        Optional<User> user = userRepository.findAllById(Id);
        if (user.isPresent()) {
            return user;
        } else {
            logger.error("User not found: " + Id);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void addSystemAdmin(int userId) {
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            userRepository.addSystemAdmin(userId);
            logger.info("Added system admin: User ID " + userId);
        } else {
            logger.error("Failed to add system admin: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public boolean removeSystemAdmin(int userId) {
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            boolean removed = userRepository.removeSystemAdmin(userId);
            if (removed) {
                logger.info("Removed system admin: User ID " + userId);
            } else {
                logger.info("User ID " + userId + " was not a system admin");
            }
            return removed;
        } else {
            logger.error("Failed to remove system admin: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public boolean isSystemAdmin(int userId) {
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            return userRepository.isSystemAdmin(userId);
        } else {
            logger.error("Failed to check system admin status: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public List<Registered> getAllSystemAdmins() {
        List<Registered> admins = userRepository.getAllSystemAdmins();
        logger.info("Retrieved " + admins.size() + " system administrators");
        return admins;
    }

    @Override
    public int getSystemAdminCount() {
        int count = userRepository.getSystemAdminCount();
        logger.info("Current system admin count: " + count);
        return count;
    }

    // Suspension management methods (admin only)
    
    /**
     * Suspend a user until a specific date. Requires admin privileges.
     * If endOfSuspension is null, the suspension is permanent.
     * 
     * @param adminId The ID of the admin performing the action
     * @param userId The ID of the user to suspend
     * @param endOfSuspension The date when the suspension ends, or null for permanent suspension
     * @throws IllegalArgumentException If the user doesn't exist or the admin doesn't have privileges
     */
    @Override
    public void suspendUser(int adminId, int userId, LocalDate endOfSuspension) {
        if (!userRepository.isSystemAdmin(adminId)) {
            logger.error("Unauthorized attempt to suspend user: Admin privileges required for user ID " + adminId);
            throw new IllegalArgumentException("Admin privileges required");
        }
        
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("Failed to suspend user: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
        
        if (userRepository.isSystemAdmin(userId)) {
            logger.error("Cannot suspend an admin user: User ID " + userId);
            throw new IllegalArgumentException("Cannot suspend admin users");
        }
        
        userRepository.suspendUser(userId, endOfSuspension);
        
        if (endOfSuspension == null) {
            logger.info("User ID " + userId + " permanently suspended by admin ID " + adminId);
        } else {
            logger.info("User ID " + userId + " suspended until " + endOfSuspension + " by admin ID " + adminId);
        }
    }
    
    /**
     * Remove suspension from a user. Requires admin privileges.
     * 
     * @param adminId The ID of the admin performing the action
     * @param userId The ID of the user to unsuspend
     * @return true if the user was unsuspended, false if they weren't suspended
     * @throws IllegalArgumentException If the user doesn't exist or the admin doesn't have privileges
     */
    @Override
    public boolean unsuspendUser(int adminId, int userId) {
        if (!userRepository.isSystemAdmin(adminId)) {
            logger.error("Unauthorized attempt to unsuspend user: Admin privileges required for user ID " + adminId);
            throw new IllegalArgumentException("Admin privileges required");
        }
        
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("Failed to unsuspend user: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
        
        boolean wasUnsuspended = userRepository.unsuspendUser(userId);
        
        if (wasUnsuspended) {
            logger.info("User ID " + userId + " unsuspended by admin ID " + adminId);
        } else {
            logger.info("User ID " + userId + " was not suspended (unsuspend request by admin ID " + adminId + ")");
        }
        
        return wasUnsuspended;
    }
    
    /**
     * Check if a user is currently suspended.
     * 
     * @param userId The ID of the user to check
     * @return true if the user is suspended, false otherwise
     * @throws IllegalArgumentException If the user doesn't exist
     */
    @Override
    public boolean isUserSuspended(int userId) {
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("Failed to check suspension status: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
        
        return userRepository.isUserSuspended(userId);
    }
    
    /**
     * Get the end date of a user's suspension. Requires admin privileges.
     * 
     * @param adminId The ID of the admin performing the action
     * @param userId The ID of the user to check
     * @return The end date of the suspension, or null if the suspension is permanent
     * @throws IllegalArgumentException If the user doesn't exist, isn't suspended, or the admin doesn't have privileges
     */
    @Override
    public LocalDate getSuspensionEndDate(int adminId, int userId) {
        if (!userRepository.isSystemAdmin(adminId)) {
            logger.error("Unauthorized attempt to get suspension end date: Admin privileges required for user ID " + adminId);
            throw new IllegalArgumentException("Admin privileges required");
        }
        
        Optional<Registered> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("Failed to get suspension end date: User with ID " + userId + " not found");
            throw new IllegalArgumentException("User not found");
        }
        
        try {
            LocalDate endDate = userRepository.getSuspensionEndDate(userId);
            logger.info("Suspension end date for user ID " + userId + " checked by admin ID " + adminId);
            return endDate;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to get suspension end date: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all suspended users. Requires admin privileges.
     * 
     * @param adminId The ID of the admin performing the action
     * @return A list of all suspended users
     * @throws IllegalArgumentException If the admin doesn't have privileges
     */
    @Override
    public List<Registered> getAllSuspendedUsers(int adminId) {
        if (!userRepository.isSystemAdmin(adminId)) {
            logger.error("Unauthorized attempt to get suspended users: Admin privileges required for user ID " + adminId);
            throw new IllegalArgumentException("Admin privileges required");
        }
        
        List<Registered> suspendedUsers = userRepository.getAllSuspendedUsers();
        logger.info("List of " + suspendedUsers.size() + " suspended users retrieved by admin ID " + adminId);
        return suspendedUsers;
    }
    
    /**
     * Cleanup expired suspensions. Requires admin privileges.
     * 
     * @param adminId The ID of the admin performing the action
     * @return The number of expired suspensions that were removed
     * @throws IllegalArgumentException If the admin doesn't have privileges
     */
    @Override
    public int cleanupExpiredSuspensions(int adminId) {
        if (!userRepository.isSystemAdmin(adminId)) {
            logger.error("Unauthorized attempt to cleanup suspensions: Admin privileges required for user ID " + adminId);
            throw new IllegalArgumentException("Admin privileges required");
        }
        
        int removedCount = userRepository.cleanupExpiredSuspensions();
        logger.info(removedCount + " expired suspensions cleaned up by admin ID " + adminId);
        return removedCount;
    }

    // Unsigned (guest) user management methods
    
    /**
     * Create a new unsigned (guest) user
     * 
     * @return The created user
     */
    @Override
    public User createUnsignedUser() {
        try {
            User unsignedUser = new User();
            userRepository.addUnsignedUser(unsignedUser);
            logger.info("Created unsigned user with ID: " + unsignedUser.getUserId());
            return unsignedUser;
          } catch (IllegalArgumentException e) {
        logger.error("Failed to add unsigned user: " + e.getMessage());
        throw e;
    } catch (Exception e) {
        logger.error("Error during adding unsigned user: " + e.getMessage());
        throw new IllegalArgumentException("Error adding unsigned user: " + e.getMessage());
    }
}
    
    /**
     * Find an unsigned user by ID
     * 
     * @param userId The ID of the user to find
     * @return The user wrapped in an Optional, or an empty Optional if not found
     */
    @Override
    public Optional<User> getUnsignedUserById(int userId) {
        try {
            Optional<User> user = userRepository.findUnsignedUserById(userId);
            if (user.isPresent()) {
                logger.info("Found unsigned user with ID: " + userId);
            } else {
                logger.info("Unsigned user with ID " + userId + " not found");
            }
            return user;
        } catch (Exception e) {
            logger.error("Error during getting unsigned user: " + e.getMessage());
            throw new IllegalArgumentException("Error getting unsigned user: " + e.getMessage());
        }
    }
    
    /**
     * Get all unsigned users
     * 
     * @return A list of all unsigned users
     */
    @Override
    public List<User> getAllUnsignedUsers() {
        try {
            List<User> users = userRepository.getAllUnsignedUsers();
            logger.info("Retrieved " + users.size() + " unsigned users");
            return users;
        } catch (Exception e) {
            logger.error("Error during getting all unsigned users: " + e.getMessage());
            throw new IllegalArgumentException("Error getting all unsigned users: " + e.getMessage());
        }
    }

      /**
     * Get all unsigned usersDTO
     * 
     * @return A list of all unsigned users DTO
     */
    @Override
    public List<UserDTO> getAllUnsignedUsersDTO() {
        try {
            List<User> users = userRepository.getAllUnsignedUsers();
            logger.info("Retrieved " + users.size() + " unsigned users");
            return users.stream().map(User::toDTO).toList();
        } catch (Exception e) {
            logger.error("Error during getting all unsigned users: " + e.getMessage());
            throw new IllegalArgumentException("Error getting all unsigned users: " + e.getMessage());
        }
    }

    /**
     * Remove an unsigned user from the repository
     * 
     * @param userId The ID of the user to remove
     * @return true if the user was removed, false if they weren't found
     */
    @Override
    public boolean removeUnsignedUser(int userId) {
        try {
            boolean result = userRepository.removeUnsignedUser(userId);
            if (result) {
                logger.info("Removed unsigned user with ID: " + userId);
            } else {
                logger.info("No unsigned user with ID " + userId + " to remove");
            }
            return result;
        } catch (Exception e) {
            logger.error("Error during removing unsigned user: " + e.getMessage());
            throw new IllegalArgumentException("Error removing unsigned user: " + e.getMessage());
        }
    }
    
    /**
     * Check if a user ID belongs to an unsigned user
     * 
     * @param userId The ID to check
     * @return true if the ID belongs to an unsigned user, false otherwise
     */
    @Override
    public boolean isUnsignedUser(int userId) {
        try {
            boolean result = userRepository.isUnsignedUser(userId);
            logger.info("User with ID " + userId + " is " + (result ? "an unsigned user" : "not an unsigned user"));
            return result;
        } catch (Exception e) {
            logger.error("Error checking if user is unsigned: " + e.getMessage());
            throw new IllegalArgumentException("Error checking if user is unsigned: " + e.getMessage());
        }
    }
    
    /**
     * Get the total count of unsigned users
     * 
     * @return The number of unsigned users
     */
    @Override
    public int getUnsignedUserCount() {
        try {
            int count = userRepository.getUnsignedUserCount();
            logger.info("Current unsigned user count: " + count);
            return count;
        } catch (Exception e) {
            logger.error("Error getting unsigned user count: " + e.getMessage());
            throw new IllegalArgumentException("Error getting unsigned user count: " + e.getMessage());
        }
    }

    @Override
    public void setCart(int userId, Map<Integer, Map<Integer, Integer>> validCart) {
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                user.get().setCart(validCart);
                logger.info("Cart set for user: " + userId);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during set cart: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void removeFromBasket(int userId, int storeId, int productId){
        Optional<User> user = userRepository.findAllById(userId);
        if (user.isPresent()) {
            try {
                user.get().removeFromBasket(storeId, productId);
                logger.info("Cart for user: " + userId + " removed product " + productId + " from store " + storeId);
            } catch (Exception e) {
                logger.error("Error during set cart: " + e.getMessage());
                throw e;
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void clearAllData() {
        userRepository.clearAllData();
    }

    @Override
    public void removeAssignmentMessage(int storeId, int userId){
        Optional<Registered> user = userRepository.findById(userId);
        logger.info("Trying to remove assignment message");
        if(user.isPresent()){
            user.get().removeAssignmentMessage(storeId);
        }
        else{
            logger.error("User not found while removeAssignmentMessage");
            throw new IllegalArgumentException("User not found");
        }
    }
}
