package ApplicationLayer.Interfaces;
import DomainLayer.Model.User;
import java.util.Optional;
import java.util.List;
public interface IUserService {
    Optional<User> getUserByUserName(String userName);
    List<User> getAllUsers();
    User registerUser(User user);
    void deleteUser(String userName);//need?
    User updateUser(User user);

}
