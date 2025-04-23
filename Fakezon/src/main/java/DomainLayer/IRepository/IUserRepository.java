package DomainLayer.IRepository;

import java.util.List;
import java.util.Optional;

import DomainLayer.Model.Registered;

public interface IUserRepository {
    Optional<Registered> findByUserName(String email);

    List<Registered> findAll();

    void deleteByUserName(String email);

    void update(Registered user);

    void addUser(Registered user);

    Optional<Registered> findById(int userID);
}
