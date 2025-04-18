package ApplicationLayer.Services;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Model.Guest;
import DomainLayer.Model.Order;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.User;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
public class UserService implements IUserService {
    private final IUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User registerUser(User user) {
        // You might add validation logic here

        return userRepository.save(user);
    }

    public void deleteUser(String userName) {
        userRepository.deleteByUserName(userName);
        logger.info("User deleted: " + userName);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.update(user);
    }
    @Override
    public Optional<User> getUserById(int userID) {
        return userRepository.findById(userID);
    }
    @Override
    public void logout(int userID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                user.get().logout();
                logger.info("User logged out: " + userID);
                user.get().setUserType(new Guest());
                logger.info("User type changed to Guest for user: " + userID);

            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during logout: " + e.getMessage());
                logger.error("Error during logout: " + e.getMessage());
                
            }
        } else {
            logger.error("User not found: " + userID);
            throw new IllegalArgumentException("User not found");

        }
        
    }
    @Override
    public void login(int userID, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }
    @Override
    public void addRole(int userID, int storeID, IRegisteredRole role) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                user.get().addRole(storeID, role);
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
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                user.get().removeRole(storeID);
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
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
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
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                return user.get().getAllRoles();
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get all roles: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return null;
    }
    public boolean didPurchaseStore(int userID, int storeID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
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
    public boolean didPurchaseProduct(int userID, int storeID,int productID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                return user.get().didPurchaseProduct(storeID,productID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during check purchase: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return false;
    }
    @Override
    public HashMap<Integer, Order> getOrdersByUser(int userID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                return user.get().getOrders();
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during get orders: " + e.getMessage());
            }
        } else {
                throw new IllegalArgumentException("User not found");
            }
            return null;
        }

    public boolean isUserLoggedIn(int userID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
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
}
