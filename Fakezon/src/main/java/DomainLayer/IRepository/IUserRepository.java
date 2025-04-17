package DomainLayer.IRepository;
import java.util.List;
import java.util.Optional;

import DomainLayer.Model.User;

public interface IUserRepository {
    Optional<User> findByUserName(String email);
    Optional<User> findById(int userID);
    Optional<User> findByToken(String token);
    List<User> findAll();
    User save(User user);
    void deleteByUserName(String email);
    User update(User user);
}
