package ApplicationLayer.Services;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.User;
public class UserService implements IUserService {
    private final IUserRepository userRepository;

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
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during logout: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
        
    }
    public User LoginGuest(String token,int userID) {
        //assume i cheked everything and i have the user
        User guest = userRepository.findByToken(token).get();
        User registedUser = userRepository.findById(userID).get();
        registedUser.mergeCart(guest.getCart());
        userRepository.deleteByUserByToken(token);
        return registedUser;
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
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during add role: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
    @Override
    public void removeRole(int userID, int storeID) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isPresent() ) {
            try {
                user.get().removeRole(storeID);
            } catch (Exception e) {
                // Handle exception if needed
                System.out.println("Error during remove role: " + e.getMessage());
            }
        } else {
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
}
