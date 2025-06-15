package InfrastructureLayer;

import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.User;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Repositories.UserJpaRepository;
import InfrastructureLayer.Repositories.UserRepositoryJpaImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {com.fakezone.fakezone.FakezoneApplication.class})
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserPersistenceTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private Registered testRegistered1;
    private Registered testRegistered2;
    private User testGuest1;
    private User testGuest2;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        userRepository.clearAllData();

        // Create test registered users
        testRegistered1 = new Registered("test1@example.com", "password123", 
                                       LocalDate.of(1990, 5, 15), "US");
        testRegistered1.setUserId(1001);

        testRegistered2 = new Registered("test2@example.com", "password456", 
                                       LocalDate.of(1985, 8, 22), "CA");
        testRegistered2.setUserId(1002);

        // Create test guest users with explicit IDs
        testGuest1 = new User(2001);
        testGuest2 = new User(2002);
    }

    @Test
    void testAddAndFindRegisteredUser() {
        // Add registered user
        userRepository.addUser(testRegistered1);

        // Find by ID
        Optional<Registered> found = userRepository.findById(testRegistered1.getUserId());
        assertTrue(found.isPresent(), "Registered user should be found by ID");
        assertEquals(testRegistered1.getEmail(), found.get().getEmail(), "Email should match");
        assertEquals(testRegistered1.getUserId(), found.get().getUserId(), "User ID should match");

        // Find by email
        Optional<Registered> foundByEmail = userRepository.findByUserName(testRegistered1.getEmail());
        assertTrue(foundByEmail.isPresent(), "Registered user should be found by email");
        assertEquals(testRegistered1.getUserId(), foundByEmail.get().getUserId(), "User ID should match");
    }

    @Test
    void testAddAndFindGuestUser() {
        // Add guest user
        userRepository.addUnsignedUser(testGuest1);

        // Find by ID
        Optional<User> found = userRepository.findUnsignedUserById(testGuest1.getUserId());
        assertTrue(found.isPresent(), "Guest user should be found by ID");
        assertEquals(testGuest1.getUserId(), found.get().getUserId(), "User ID should match");
        assertFalse(found.get().isRegistered(), "User should not be registered");
    }

    @Test
    void testFindAllRegisteredUsers() {
        // Add multiple registered users
        userRepository.addUser(testRegistered1);
        userRepository.addUser(testRegistered2);

        // Find all registered users
        List<Registered> allRegistered = userRepository.findAll();
        assertEquals(2, allRegistered.size(), "Should find 2 registered users");
        
        // Verify both users are present
        assertTrue(allRegistered.stream().anyMatch(u -> u.getEmail().equals("test1@example.com")), 
                  "First user should be in the list");
        assertTrue(allRegistered.stream().anyMatch(u -> u.getEmail().equals("test2@example.com")), 
                  "Second user should be in the list");
    }

    @Test
    void testFindAllGuestUsers() {
        // Add multiple guest users
        userRepository.addUnsignedUser(testGuest1);
        userRepository.addUnsignedUser(testGuest2);

        // Find all guest users
        List<User> allGuests = userRepository.getAllUnsignedUsers();
        assertEquals(2, allGuests.size(), "Should find 2 guest users");
        
        // Verify both users are present
        assertTrue(allGuests.stream().anyMatch(u -> u.getUserId() == testGuest1.getUserId()), 
                  "First guest should be in the list");
        assertTrue(allGuests.stream().anyMatch(u -> u.getUserId() == testGuest2.getUserId()), 
                  "Second guest should be in the list");
    }

    @Test
    void testDeleteRegisteredUser() {
        // Add user
        userRepository.addUser(testRegistered1);
        
        // Verify user exists
        assertTrue(userRepository.findById(testRegistered1.getUserId()).isPresent(), 
                  "User should exist before deletion");

        // Delete user
        userRepository.deleteByUserName(testRegistered1.getEmail());

        // Verify user is deleted
        assertFalse(userRepository.findById(testRegistered1.getUserId()).isPresent(), 
                   "User should not exist after deletion");
    }

    @Test
    void testDeleteGuestUser() {
        // Add guest user
        userRepository.addUnsignedUser(testGuest1);
        
        // Verify user exists
        assertTrue(userRepository.findUnsignedUserById(testGuest1.getUserId()).isPresent(), 
                  "Guest user should exist before deletion");

        // Delete user
        boolean deleted = userRepository.removeUnsignedUser(testGuest1.getUserId());
        assertTrue(deleted, "Delete operation should return true");

        // Verify user is deleted
        assertFalse(userRepository.findUnsignedUserById(testGuest1.getUserId()).isPresent(), 
                   "Guest user should not exist after deletion");
    }

    @Test
    void testIsUserRegistered() {
        // Add registered and guest users
        userRepository.addUser(testRegistered1);
        userRepository.addUnsignedUser(testGuest1);

        // Test registered user
        assertTrue(userRepository.isUserRegistered(testRegistered1.getUserId()), 
                  "Registered user should be identified as registered");

        // Test guest user
        assertFalse(userRepository.isUserRegistered(testGuest1.getUserId()), 
                   "Guest user should not be identified as registered");
    }

    @Test
    void testUserCounts() {
        // Initially should be empty
        assertEquals(0, userRepository.getUnsignedUserCount(), "Initial guest count should be 0");

        // Add users
        userRepository.addUser(testRegistered1);
        userRepository.addUser(testRegistered2);
        userRepository.addUnsignedUser(testGuest1);
        userRepository.addUnsignedUser(testGuest2);

        // Test counts
        assertEquals(2, userRepository.getUnsignedUserCount(), "Should have 2 guest users");
        assertEquals(2, userRepository.findAll().size(), "Should have 2 registered users");
    }

    @Test
    void testSystemAdminManagement() {
        // Add user first
        userRepository.addUser(testRegistered1);

        // Initially should not be admin
        assertFalse(userRepository.isSystemAdmin(testRegistered1.getUserId()), 
                   "User should not be admin initially");

        // Add as system admin
        userRepository.addSystemAdmin(testRegistered1.getUserId());
        assertTrue(userRepository.isSystemAdmin(testRegistered1.getUserId()), 
                  "User should be admin after addition");

        // Test admin count
        assertEquals(1, userRepository.getSystemAdminCount(), 
                    "Should have 1 admin after adding"); // Just our test user

        // Remove admin privileges
        boolean removed = userRepository.removeSystemAdmin(testRegistered1.getUserId());
        assertTrue(removed, "Remove operation should return true");
        assertFalse(userRepository.isSystemAdmin(testRegistered1.getUserId()), 
                   "User should not be admin after removal");
    }

    @Test
    void testUserSuspension() {
        // Add user first
        userRepository.addUser(testRegistered1);

        // Initially should not be suspended
        assertFalse(userRepository.isUserSuspended(testRegistered1.getUserId()), 
                   "User should not be suspended initially");

        // Suspend user
        userRepository.suspendUser(testRegistered1.getUserId(), LocalDate.now().plusDays(7));
        assertTrue(userRepository.isUserSuspended(testRegistered1.getUserId()), 
                  "User should be suspended after suspension");

        // Test suspended users list
        List<Registered> suspendedUsers = userRepository.getAllSuspendedUsers();
        assertEquals(1, suspendedUsers.size(), "Should have 1 suspended user");
        assertEquals(testRegistered1.getUserId(), suspendedUsers.get(0).getUserId(), 
                    "Suspended user should match");

        // Unsuspend user
        boolean unsuspended = userRepository.unsuspendUser(testRegistered1.getUserId());
        assertTrue(unsuspended, "Unsuspend operation should return true");
        assertFalse(userRepository.isUserSuspended(testRegistered1.getUserId()), 
                   "User should not be suspended after unsuspension");
    }

    @Test
    void testAddDuplicateRegisteredUser() {
        userRepository.addUser(testRegistered1);
        
        // Try to add same user again
        assertThrows(DataAccessException.class, () -> {
            userRepository.addUser(testRegistered1);
        }, "Should throw exception for duplicate user");
    }

    @Test
    void testAddDuplicateGuestUser() {
        userRepository.addUnsignedUser(testGuest1);
        
        // Try to add same guest user again
        assertThrows(DataAccessException.class, () -> {
            userRepository.addUnsignedUser(testGuest1);
        }, "Should throw exception for duplicate guest user");
    }

    @Test
    void testDeleteNonExistentUser() {
        assertThrows(DataAccessException.class, () -> {
            userRepository.deleteByUserName("nonexistent@example.com");
        }, "Should throw exception for non-existent user");
    }

    @Test
    void testFindAllById() {
        // Add both registered and guest users
        userRepository.addUser(testRegistered1);
        userRepository.addUnsignedUser(testGuest1);

        // Test finding registered user
        Optional<User> foundRegistered = userRepository.findAllById(testRegistered1.getUserId());
        assertTrue(foundRegistered.isPresent(), "Should find registered user");
        assertTrue(foundRegistered.get().isRegistered(), "Should be a registered user");

        // Test finding guest user
        Optional<User> foundGuest = userRepository.findAllById(testGuest1.getUserId());
        assertTrue(foundGuest.isPresent(), "Should find guest user");
        assertFalse(foundGuest.get().isRegistered(), "Should be a guest user");
    }

    @Test
    void testClearAllData() {
        // Add some users
        userRepository.addUser(testRegistered1);
        userRepository.addUnsignedUser(testGuest1);

        // Verify users exist
        assertEquals(1, userRepository.findAll().size(), "Should have 1 registered user");
        assertEquals(1, userRepository.getUnsignedUserCount(), "Should have 1 guest user");

        // Clear all data
        userRepository.clearAllData();

        // Verify all data is cleared
        assertEquals(0, userRepository.findAll().size(), "Should have no registered users");
        assertEquals(0, userRepository.getUnsignedUserCount(), "Should have no guest users");
    }
} 