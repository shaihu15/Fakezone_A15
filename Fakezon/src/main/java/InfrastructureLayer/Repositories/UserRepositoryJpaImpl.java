package InfrastructureLayer.Repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

@Repository
@Primary
@Transactional
public class UserRepositoryJpaImpl implements IUserRepository {

    private final UserJpaRepository userJpaRepository;
    
    // In-memory collections for features not yet persisted
    private final Set<Integer> suspendedUsers = new HashSet<>();
    private final Set<Integer> systemAdmins = new HashSet<>();

    @Autowired
    public UserRepositoryJpaImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        // Initialize with some test data if needed
        initializeTestData();
    }

    @Override
    public Optional<Registered> findByUserName(String email) {
        return userJpaRepository.findRegisteredByEmail(email);
    }

    @Override
    public List<Registered> findAll() {
        return userJpaRepository.findAllRegistered();
    }

    @Override
    public boolean isUserRegistered(int userId) {
        Optional<Registered> registered = userJpaRepository.findRegisteredById(userId);
        return registered.isPresent();
    }

    @Override
    public void deleteByUserName(String email) {
        Optional<Registered> user = findByUserName(email);
        if (user.isPresent()) {
            userJpaRepository.deleteById(user.get().getUserId());
            // Also remove from in-memory collections
            suspendedUsers.remove(user.get().getUserId());
            systemAdmins.remove(user.get().getUserId());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void clearAllData() {
        userJpaRepository.deleteAll();
        suspendedUsers.clear();
        systemAdmins.clear();
    }

    @Override
    public void addUser(Registered user) {
        if (userJpaRepository.findByUserId(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " already exists.");
        }
        userJpaRepository.save(user);
    }

    @Override
    public Optional<Registered> findById(int userID) {
        return userJpaRepository.findRegisteredById(userID);
    }

    @Override
    public List<Registered> UsersWithRolesInStoreId(int storeID) {
        // This method requires role information which is transient
        // For now, return all registered users (can be enhanced later)
        return findAll();
    }

    @Override
    public Optional<User> findAllById(int userID) {
        return userJpaRepository.findByUserId(userID);
    }

    // Suspension management (in-memory for now)
    @Override
    public void suspendUser(int userId, LocalDate endOfSuspension) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        suspendedUsers.add(userId);
    }

    @Override
    public boolean unsuspendUser(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return suspendedUsers.remove(userId);
    }

    @Override
    public boolean isUserSuspended(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return suspendedUsers.contains(userId);
    }

    @Override
    public LocalDate getSuspensionEndDate(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        if (!suspendedUsers.contains(userId)) {
            throw new IllegalArgumentException("User with ID " + userId + " is not suspended.");
        }
        return null; // Simplified for now
    }

    @Override
    public List<Registered> getAllSuspendedUsers() {
        List<Registered> result = new ArrayList<>();
        for (Integer userId : suspendedUsers) {
            Optional<Registered> user = findById(userId);
            user.ifPresent(result::add);
        }
        return result;
    }

    @Override
    public int cleanupExpiredSuspensions() {
        // Simplified implementation
        return 0;
    }

    // System admin management (in-memory for now)
    @Override
    public void addSystemAdmin(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        systemAdmins.add(userId);
    }

    @Override
    public boolean removeSystemAdmin(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return systemAdmins.remove(userId);
    }

    @Override
    public boolean isSystemAdmin(int userId) {
        if (!userJpaRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }
        return systemAdmins.contains(userId);
    }

    @Override
    public List<Registered> getAllSystemAdmins() {
        List<Registered> result = new ArrayList<>();
        for (Integer adminId : systemAdmins) {
            Optional<Registered> admin = findById(adminId);
            admin.ifPresent(result::add);
        }
        return result;
    }

    @Override
    public int getSystemAdminCount() {
        return systemAdmins.size();
    }

    // Guest user management
    @Override
    public void addUnsignedUser(User user) {
        if (userJpaRepository.findByUserId(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " already exists.");
        }
        userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findUnsignedUserById(int userId) {
        return userJpaRepository.findGuestById(userId);
    }

    @Override
    public List<User> getAllUnsignedUsers() {
        return userJpaRepository.findAllGuests();
    }

    @Override
    public boolean removeUnsignedUser(int userId) {
        Optional<User> user = userJpaRepository.findGuestById(userId);
        if (user.isPresent()) {
            userJpaRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Override
    public boolean isUnsignedUser(int userId) {
        return userJpaRepository.findGuestById(userId).isPresent();
    }

    @Override
    public int getUnsignedUserCount() {
        return userJpaRepository.findAllGuests().size();
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!userJpaRepository.findByUserId(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " does not exist.");
        }
        return userJpaRepository.save(user);
    }

    private void initializeTestData() {
        // Initialize some test data similar to the original repository
        systemAdmins.add(1001);
    }
} 