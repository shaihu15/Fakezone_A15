package DomainLayer.IRepository;

import java.util.List;
import java.util.Optional;

import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public interface IUserRepository {
    Optional<Registered> findByUserName(String email);

    List<Registered> findAll();

    void deleteByUserName(String email);

    void update(Registered user);

    void addUser(Registered user);

    Optional<Registered> findById(int userID);

    Optional<User> findAllById(int userID); // Added method to find user by ID as String
}
