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
}
