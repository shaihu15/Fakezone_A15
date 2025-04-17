package DomainLayer.IRepository;
import DomainLayer.Model.User;
import java.util.Optional;
import java.util.List;

public interface IUserRepository {
    Optional<User> findByUserName(String userName);
    Optional<User> findById(int userID);
    List<User> findAll();
    User save(User user);
    void deleteByUserName(String userName);
    User update(User user);
}
