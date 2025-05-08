package InfrastructureLayer.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public class UserRepository implements IUserRepository {
    private Map<Integer, Registered> users;
    private Map<Integer, LocalDate> suspendedUsers; // Map of userId to suspension end date (null if permanent)
    private Set<Integer> systemAdmins; // Set of user IDs who are system administrators

    public UserRepository() {
        this.users = new HashMap<>();
        this.suspendedUsers = new HashMap<>();
        this.systemAdmins = new HashSet<>();
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
    public void update(Registered user) {
        if (!users.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " does not exist.");
        }
        users.put(user.getUserId(), user); // update existing
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
        return Optional.ofNullable(users.get(userID));
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
}
