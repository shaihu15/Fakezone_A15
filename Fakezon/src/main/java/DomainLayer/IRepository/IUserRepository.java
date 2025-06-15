package DomainLayer.IRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public interface IUserRepository {
    Optional<Registered> findByUserName(String email);

    List<Registered> findAll();

    boolean isUserRegistered(int userId);

    void deleteByUserName(String email);

    void clearAllData();

    void addUser(Registered user);

    Optional<Registered> findById(int userID);

    List<Registered> UsersWithRolesInStoreId(int storeID);

    Optional<User> findAllById(int userID); // Added method to find user by ID as String

    /**
     * Suspend a user until a specific date. If endOfSuspension is null, the
     * suspension is permanent.
     * 
     * @param userId          The ID of the user to suspend
     * @param endOfSuspension The date when the suspension ends, or null for
     *                        permanent suspension
     * @throws IllegalArgumentException If the user doesn't exist
     */
    void suspendUser(int userId, LocalDate endOfSuspension);

    /**
     * Remove suspension from a user
     * 
     * @param userId The ID of the user to unsuspend
     * @return true if the user was unsuspended, false if they weren't suspended
     * @throws IllegalArgumentException If the user doesn't exist
     */
    boolean unsuspendUser(int userId);

    /**
     * Check if a user is currently suspended
     * 
     * @param userId The ID of the user to check
     * @return true if the user is suspended, false otherwise
     * @throws IllegalArgumentException If the user doesn't exist
     */
    boolean isUserSuspended(int userId);

    /**
     * Get the end date of a user's suspension
     * 
     * @param userId The ID of the user to check
     * @return The end date of the suspension, or null if the suspension is
     *         permanent
     * @throws IllegalArgumentException If the user doesn't exist or isn't suspended
     */
    LocalDate getSuspensionEndDate(int userId);

    /**
     * Get all suspended users
     * 
     * @return A list of all suspended users
     */
    List<Registered> getAllSuspendedUsers();

    /**
     * Cleanup expired suspensions
     * 
     * @return The number of expired suspensions that were removed
     */
    int cleanupExpiredSuspensions();

    /**
     * Add a user as a system administrator
     * 
     * @param userId The ID of the user to make an admin
     * @throws IllegalArgumentException If the user doesn't exist
     */
    void addSystemAdmin(int userId);

    /**
     * Remove system administrator privileges from a user
     * 
     * @param userId The ID of the user to remove admin privileges from
     * @return true if the user was an admin and privileges were removed, false if
     *         they weren't an admin
     * @throws IllegalArgumentException If the user doesn't exist
     */
    boolean removeSystemAdmin(int userId);

    /**
     * Check if a user is a system administrator
     * 
     * @param userId The ID of the user to check
     * @return true if the user is a system administrator, false otherwise
     * @throws IllegalArgumentException If the user doesn't exist
     */
    boolean isSystemAdmin(int userId);

    /**
     * Get all system administrators
     * 
     * @return A list of all system administrators
     */
    List<Registered> getAllSystemAdmins();

    /**
     * Get the number of system administrators
     * 
     * @return The number of system administrators
     */
    int getSystemAdminCount();

    /**
     * Add an unsigned (guest) user to the repository
     * 
     * @param user The user to add
     * @throws IllegalArgumentException If a user with the same ID already exists
     */
    void addUnsignedUser(User user);

    /**
     * Find an unsigned user by ID
     * 
     * @param userId The ID of the user to find
     * @return The user wrapped in an Optional, or an empty Optional if not found
     */
    Optional<User> findUnsignedUserById(int userId);

    /**
     * Get all unsigned users
     * 
     * @return A list of all unsigned users
     */
    List<User> getAllUnsignedUsers();

    /**
     * Remove an unsigned user from the repository
     * 
     * @param userId The ID of the user to remove
     * @return true if the user was removed, false if they weren't found
     */
    boolean removeUnsignedUser(int userId);

    /**
     * Check if a user ID belongs to an unsigned user
     * 
     * @param userId The ID to check
     * @return true if the ID belongs to an unsigned user, false otherwise
     */
    boolean isUnsignedUser(int userId);

    /**
     * Get the total count of unsigned users
     * 
     * @return The number of unsigned users
     */
    int getUnsignedUserCount();

    /**
     * Save a user's changes to persistent storage
     * 
     * @param user The user to save
     * @return The saved user
     * @throws IllegalArgumentException If the user doesn't exist
     */
    User save(User user);

}
