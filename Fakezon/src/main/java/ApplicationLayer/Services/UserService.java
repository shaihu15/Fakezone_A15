package ApplicationLayer.Services;

import java.time.LocalDate;

import ApplicationLayer.Interfaces.IUserService;

import DomainLayer.Model.Order;
import DomainLayer.Model.Registered;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ApplicationLayer.DTO.UserDTO;

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
    public UserDTO registerUser(String email, String password, LocalDate dateOfBirth) {
        Registered user = new Registered(email, password, dateOfBirth);
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
    public void deleteUser(String userName) {
        userRepository.deleteByUserName(userName);
        logger.info("User deleted: " + userName);
    }

    @Override
    public void updateUser(Registered user) {
        userRepository.update(user);
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
    public void login(String email, String password) {
        try {
            Optional<Registered> optionalUser = userRepository.findByUserName(email);
            if (optionalUser.isEmpty()) {
                logger.warn("Login failed: User with email {} not found", email);
                throw new IllegalArgumentException("User not found");
            }
            Registered user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                user.login();
                logger.info("User logged in: " + email);
            } else {
                logger.warn("Login failed: Incorrect password for user with email {}", email);
                throw new IllegalArgumentException("Incorrect password");
            }
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
        if (user.isPresent()) {
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
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        return false;
    }

    @Override
    public HashMap<Integer, Order> getOrdersByUser(int userID) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
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
        Optional<Registered> user = userRepository.findById(userID);
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
    public void receivingMessageFromStore(int userID, int storeID, String message) {
        Optional<Registered> user = userRepository.findById(userID);
        if (user.isPresent()) {
            try {
                user.get().receivingMessageFromStore(storeID, message);
                logger.info("Message received from store: " + storeID + " to user: " + userID);
            } catch (Exception e) {
                // Handle exception if needed
                logger.error("Error during receiving message: " + e.getMessage());
                System.out.println("Error during receiving message: " + e.getMessage());
            }
        } else {
            logger.error("User not found: " + userID);
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public UserDTO addUser(String password, String email, LocalDate dateOfBirth) {
        Registered user;
        try {
            user = new Registered(email, password, dateOfBirth);
            userRepository.addUser(user);
            logger.info("User added: " + email);
        } catch (Exception e) {
            // Handle exception if needed
            System.out.println("Error during add user: " + e.getMessage());
            logger.error("Error during add user: " + e.getMessage());
            throw new IllegalArgumentException("User already exists");
        }
        return user.toDTO();
    }

    @Override
    public void guestRegister(String userName, String password, String email, int UserId, LocalDate dateOfBirth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
