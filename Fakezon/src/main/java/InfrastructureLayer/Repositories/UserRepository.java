package InfrastructureLayer.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;

public class UserRepository implements IUserRepository {
    private Map<Integer, Registered> users;

    public UserRepository() {
        this.users = new HashMap<>();
    }

    @Override
    public Optional<Registered> findByUserName(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
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
            users.remove(user.get().getUserID());
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void update(Registered user) {
        if (!users.containsKey(user.getUserID())) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " does not exist.");
        }
        users.put(user.getUserID(), user); // update existing
    }

    @Override
    public void addUser(Registered user) {
        if (users.containsKey(user.getUserID())) {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " already exists.");
        }
        users.put(user.getUserID(), user);
    }

}
