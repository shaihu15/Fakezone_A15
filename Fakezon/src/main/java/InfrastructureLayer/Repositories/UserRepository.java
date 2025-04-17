package InfrastructureLayer.Repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.User;

public class UserRepository implements IUserRepository{
    private Map<Integer, User> users;
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
    public User save(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

}
