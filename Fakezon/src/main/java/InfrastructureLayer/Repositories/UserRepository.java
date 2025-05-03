package InfrastructureLayer.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public class UserRepository implements IUserRepository {
    private Map<Integer, Registered> users;

    public UserRepository() {
        this.users = new HashMap<>();
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

}
