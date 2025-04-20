package UnitTesting;

import DomainLayer.Model.Registered;
import DomainLayer.Model.User;
import InfrastructureLayer.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {
    private UserRepository repository;
    private User user;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        user = new User();
        user.setUserType(new Registered("email@com", "password123"));
        repository.addUser(user);
    }
    /* 
    //TODO when initializing a registered user, no ID is required, ufter update this can do the tset
    @Test
    void givenExistingUser_whenFindById_returnUser() {
        Optional<User> found = repository.findById(user.getUserID());
        assertTrue(found.isPresent());
        assertEquals(user, found.get());
    }
    //TODO whenFindByUserName get email
    @Test
    void givenExistingUser_whenFindByUserName_returnUser() {
        Optional<User> found = repository.findByUserName(user.getEmail());
        assertTrue(found.isPresent());
        assertEquals(user, found.get());
    }
    */
    @Test
    void givenUserAlreadyExists_whenAddUser_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addUser(user);
        });
        assertEquals("User with ID " + user.getUserID() + " already exists.", exception.getMessage());
    }

    @Test
    void givenNonExistingUser_whenDelete_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteByUserName("nonexistent@email.com");
        });
        assertEquals("User not found", exception.getMessage());
    }    

    @Test
    void givenExistingUser_whenDelete_thenUserRemoved() {
        repository.deleteByUserName(user.getEmail());
        assertFalse(repository.findById(user.getUserID()).isPresent());
    }

    @Test
    void givenValidUpdate_whenUpdate_thenUserUpdated() {
        user.logout(); // modifies user
        User updated = repository.update(user);
        assertEquals(user, updated);
    }

    @Test
    void givenNonExistingUser_whenUpdate_thenThrows() {
        User newUser = new User();
        newUser.setUserType(new Registered("new@email.com", "pass"));
        assertThrows(IllegalArgumentException.class, () -> repository.update(newUser));
    }
}
