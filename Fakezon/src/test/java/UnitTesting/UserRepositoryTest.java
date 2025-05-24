package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Repositories.UserRepository;
import DomainLayer.Model.User;

public class UserRepositoryTest {

    private UserRepository userRepository;
    private Registered registedUser;
    String testEmail = "test@gmail.com";
    String testPassword = "password123";
    LocalDate testDate = LocalDate.of(2000, 1, 1);
    private String testCountry = "IL";
    
    @BeforeEach
    void setUp() {
        registedUser = new Registered(testEmail,testPassword ,testDate,testCountry);
        userRepository = new UserRepository(); // Initialize the repository before each test
        userRepository.addUser(registedUser); // Add a test user to the repository
    }

    @Test
    void testAddUser() {
     
        // Act
        System.out.println("Registered User ID: " + registedUser.getUserId());

        // Assert
        Optional<Registered> retrievedUser = userRepository.findById(registedUser.getUserId());
        System.out.println("Retrieved User ID: " + retrievedUser.get().getUserId());

        assertTrue(retrievedUser.isPresent(), "User should be added to the repository");
        assertEquals("test@gmail.com", retrievedUser.get().getEmail(), "Email should match");
    }

    @Test
    void testFindById() {
        // Act
        Optional<Registered> retrievedUser = userRepository.findById(registedUser.getUserId());

        // Assert
        assertTrue(retrievedUser.isPresent(), "User should be found by ID");
        assertEquals(registedUser.getUserId(), retrievedUser.get().getUserId(), "User ID should match");
    }

    @Test
    void testFindByEmail() {
       
        // Act
        Optional<Registered> retrievedUser = userRepository.findByUserName(registedUser.getEmail());

        // Assert
        assertTrue(retrievedUser.isPresent(), "User should be found by username");
        assertEquals("test@gmail.com", retrievedUser.get().getEmail(), "Email should match");
    }

    @Test
    void testDeleteByUserName() {
        // Act
        userRepository.deleteByUserName(testEmail);

        // Assert
        Optional<Registered> deletedUser = userRepository.findById(registedUser.getUserId());
        assertFalse(deletedUser.isPresent(), "User should be deleted from the repository");
    }

    // System Admin Tests
    @Test
    void testAddSystemAdmin() {
        // Act
        userRepository.addSystemAdmin(registedUser.getUserId());
        
        // Assert
        assertTrue(userRepository.isSystemAdmin(registedUser.getUserId()), "User should be an admin");
        assertEquals(1, userRepository.getSystemAdminCount(), "Admin count should be 1");
    }
    
    @Test
    void testRemoveSystemAdmin() {
        // Arrange
        userRepository.addSystemAdmin(registedUser.getUserId());
        assertTrue(userRepository.isSystemAdmin(registedUser.getUserId()), "User should be an admin");
        
        // Act
        boolean result = userRepository.removeSystemAdmin(registedUser.getUserId());
        
        // Assert
        assertTrue(result, "Remove operation should return true");
        assertFalse(userRepository.isSystemAdmin(registedUser.getUserId()), "User should no longer be an admin");
        assertEquals(0, userRepository.getSystemAdminCount(), "Admin count should be 0");
    }
    
    @Test
    void testRemoveNonAdminReturnsfalse() {
        // Act
        boolean result = userRepository.removeSystemAdmin(registedUser.getUserId());
        
        // Assert
        assertFalse(result, "Remove operation should return false for non-admin user");
        assertEquals(0, userRepository.getSystemAdminCount(), "Admin count should be 0");
    }
    
    @Test
    void testGetAllSystemAdmins() {
        // Arrange
        Registered secondUser = new Registered("admin2@example.com", "password", LocalDate.now(), "US");
        userRepository.addUser(secondUser);
        
        userRepository.addSystemAdmin(registedUser.getUserId());
        userRepository.addSystemAdmin(secondUser.getUserId());
        
        // Act
        List<Registered> admins = userRepository.getAllSystemAdmins();
        
        // Assert
        assertEquals(2, admins.size(), "There should be 2 admins");
        assertTrue(admins.stream().anyMatch(u -> u.getUserId() == registedUser.getUserId()), "First user should be in admin list");
        assertTrue(admins.stream().anyMatch(u -> u.getUserId() == secondUser.getUserId()), "Second user should be in admin list");
    }
    
    @Test
    void testInvalidUserIdThrowsExceptionForAdmin() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.addSystemAdmin(999); // Non-existent user ID
        }, "Should throw exception for non-existent user");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.removeSystemAdmin(999); // Non-existent user ID
        }, "Should throw exception for non-existent user");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.isSystemAdmin(999); // Non-existent user ID
        }, "Should throw exception for non-existent user");
    }
    
    // User Suspension Tests
    @Test
    void testSuspendUserTemporarily() {
        // Arrange
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        // Act
        userRepository.suspendUser(registedUser.getUserId(), endDate);
        
        // Assert
        assertTrue(userRepository.isUserSuspended(registedUser.getUserId()), "User should be suspended");
        assertEquals(endDate, userRepository.getSuspensionEndDate(registedUser.getUserId()), "End date should match");
    }
    
    @Test
    void testSuspendUserPermanently() {
        // Act
        userRepository.suspendUser(registedUser.getUserId(), null);
        
        // Assert
        assertTrue(userRepository.isUserSuspended(registedUser.getUserId()), "User should be suspended");
        assertNull(userRepository.getSuspensionEndDate(registedUser.getUserId()), "End date should be null for permanent suspension");
    }
    
    @Test
    void testUnsuspendUser() {
        // Arrange
        userRepository.suspendUser(registedUser.getUserId(), LocalDate.now().plusDays(7));
        assertTrue(userRepository.isUserSuspended(registedUser.getUserId()), "User should be suspended");
        
        // Act
        boolean result = userRepository.unsuspendUser(registedUser.getUserId());
        
        // Assert
        assertTrue(result, "Unsuspend operation should return true");
        assertFalse(userRepository.isUserSuspended(registedUser.getUserId()), "User should no longer be suspended");
    }
    
    @Test
    void testUnsuspendNonSuspendedUserReturnsFalse() {
        // Act
        boolean result = userRepository.unsuspendUser(registedUser.getUserId());
        
        // Assert
        assertFalse(result, "Unsuspend operation should return false for non-suspended user");
    }
    
    @Test
    void testInvalidUserIdThrowsExceptionForSuspension() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.suspendUser(999, LocalDate.now().plusDays(7)); // Non-existent user ID
        }, "Should throw exception for non-existent user");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.unsuspendUser(999); // Non-existent user ID
        }, "Should throw exception for non-existent user");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userRepository.isUserSuspended(999); // Non-existent user ID
        }, "Should throw exception for non-existent user");
    }
    
    @Test
    void testGetAllSuspendedUsers() {
        // Arrange
        Registered secondUser = new Registered("suspended2@example.com", "password", LocalDate.now(), "US");
        userRepository.addUser(secondUser);
        
        userRepository.suspendUser(registedUser.getUserId(), LocalDate.now().plusDays(7));
        userRepository.suspendUser(secondUser.getUserId(), null); // Permanent suspension
        
        // Act
        List<Registered> suspendedUsers = userRepository.getAllSuspendedUsers();
        
        // Assert
        assertEquals(2, suspendedUsers.size(), "There should be 2 suspended users");
        assertTrue(suspendedUsers.stream().anyMatch(u -> u.getUserId() == registedUser.getUserId()), "First user should be in suspended list");
        assertTrue(suspendedUsers.stream().anyMatch(u -> u.getUserId() == secondUser.getUserId()), "Second user should be in suspended list");
    }
    
    @Test
    void testCleanupExpiredSuspensions() {
        // Arrange
        Registered user1 = new Registered("expired@example.com", "password", LocalDate.now(), "US");
        Registered user2 = new Registered("active@example.com", "password", LocalDate.now(), "US");
        Registered user3 = new Registered("permanent@example.com", "password", LocalDate.now(), "US");
        
        userRepository.addUser(user1);
        userRepository.addUser(user2);
        userRepository.addUser(user3);
        
        userRepository.suspendUser(user1.getUserId(), LocalDate.now().minusDays(1)); // Expired
        userRepository.suspendUser(user2.getUserId(), LocalDate.now().plusDays(7)); // Active
        userRepository.suspendUser(user3.getUserId(), null); // Permanent
        
        // Act
        int removedCount = userRepository.cleanupExpiredSuspensions();
        
        // Assert
        assertEquals(1, removedCount, "Should remove 1 expired suspension");
        assertFalse(userRepository.isUserSuspended(user1.getUserId()), "Expired suspension should be removed");
        assertTrue(userRepository.isUserSuspended(user2.getUserId()), "Active suspension should remain");
        assertTrue(userRepository.isUserSuspended(user3.getUserId()), "Permanent suspension should remain");
    }

        @Test
    void testUserDoesNotExist_throwsException() {
        int userId = 1;

        // users map is empty => user doesn't exist
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.getSuspensionEndDate(userId);
        });

        assertNotNull(exception, "Exception should not be null");
    }

    @Test
    void testUserNotSuspended_throwsException() {

        // suspendedUsers map is empty => user exists but not suspended
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.getSuspensionEndDate(registedUser.getUserId());
        });

        assertNotNull(exception, "Exception should not be null");
    }

    @Test
    void testUserSuspended_returnsEndDate() {
        LocalDate endDate = LocalDate.now().plusDays(7);
        userRepository.suspendUser(registedUser.getUserId(), endDate);

        assertEquals(endDate, userRepository.getSuspensionEndDate(registedUser.getUserId()));
    }
   

}