package ApplicationLayer.Services;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Model.User;
import DomainLayer.IRepository.IUserRepository;

import java.util.List;
import java.util.Optional;
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
    @Override
    public void login(int userID, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }
}
