package InfrastructureLayer.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.User;

public class UserRepository implements IUserRepository{
    private Map<Integer, User> users;

    public UserRepository() {
        this.users = new HashMap<>();
    }

    @Override
    public Optional<User> findByUserName(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findById(int userID) {
        return Optional.ofNullable(users.get(userID));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(users.values());
    }

    @Override
    public void deleteByUserName(String email) {
        Optional<User> user = findByUserName(email);
        if (user.isPresent()) {
            users.remove(user.get().getUserID());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getUserID())) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " does not exist.");
        }
        users.put(user.getUserID(), user); // update existing
        return user;
    }

    @Override
    public User addUser(User user){
        if (users.containsKey(user.getUserID())) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " already exists.");
        }
        users.put(user.getUserID(), user);
        return user;
    }

}
