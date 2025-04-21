package DomainLayer.IRepository;

import DomainLayer.Model.Registered;

import java.util.Optional;
import java.util.List;

public interface IUserRepository {
    Optional<Registered> findByUserName(String email);

    Optional<Registered> findById(int userID);

    List<Registered> findAll();

    void deleteByUserName(String email);

    void update(Registered user);

    void addUser(Registered user);
}
