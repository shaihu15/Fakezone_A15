package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import java.time.LocalDate;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Repositories.UserJpaRepository;
import InfrastructureLayer.Repositories.UserRepository;
import DomainLayer.Model.User;

import com.fakezone.fakezone.FakezoneApplication;
import org.springframework.boot.test.context.SpringBootTest;

public class UserRepositoryTest {

    private UserRepository userRepository;
    private Registered registedUser;
    String testEmail = "test@gmail.com";
    String testPassword = "password123";
    LocalDate testDate = LocalDate.of(2000, 1, 1);
    private String testCountry = "IL";
    
    @Mock
    private UserJpaRepository userJpaRepository;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize Mockito annotations
        registedUser = new Registered(testEmail,testPassword ,testDate,testCountry);
        userRepository = new UserRepository(userJpaRepository); // Initialize the repository before each test
        // Mock default behavior for the main test user
        Mockito.when(userJpaRepository.findRegisteredById(registedUser.getUserId())).thenReturn(Optional.of(registedUser));
        Mockito.when(userJpaRepository.findRegisteredByEmail(registedUser.getEmail())).thenReturn(Optional.of(registedUser));
        Mockito.when(userJpaRepository.save(registedUser)).thenReturn(registedUser);
    }

    @Test
    void testAddUser() {
        Registered registedUser2 = new Registered("test2@gmail.com", "password123", testDate, testCountry);
        Mockito.when(userJpaRepository.findRegisteredById(registedUser2.getUserId()))
               .thenReturn(Optional.of(registedUser2));
        userRepository.addUser(registedUser2); // Should not throw
        verify(userJpaRepository).save(registedUser2);
        Optional<Registered> retrievedUser2 = userRepository.findRegisteredById(registedUser2.getUserId());
        assertTrue(retrievedUser2.isPresent(), "User should be added to the repository");
    }

    @Test
    void testFindById() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        Optional<Registered> retrievedUser = userRepository.findRegisteredById(user.getUserId());
        assertTrue(retrievedUser.isPresent(), "User should be found by ID");
        assertEquals(user.getUserId(), retrievedUser.get().getUserId(), "User ID should match");
    }

    @Test
    void testFindByEmail() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Optional<Registered> retrievedUser = userRepository.findByUserName(user.getEmail());
        assertTrue(retrievedUser.isPresent(), "User should be found by username");
        assertEquals(user.getEmail(), retrievedUser.get().getEmail(), "Email should match");
    }

    @Test
    void testDeleteByUserName() {
        Registered user = registedUser;
        // Simulate repository state with an in-memory map
        Map<Integer, Registered> db = new HashMap<>();
        db.put(user.getUserId(), user);

        Mockito.when(userJpaRepository.findRegisteredByEmail(user.getEmail()))
                .thenReturn(Optional.ofNullable(db.get(user.getUserId())));
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId()))
                .thenAnswer(invocation -> Optional.ofNullable(db.get(user.getUserId())));
        Mockito.doAnswer(invocation -> {
            db.remove(user.getUserId());
            return null;
        }).when(userJpaRepository).deleteById(user.getUserId());

        userRepository.deleteByUserName(user.getEmail());
        Optional<Registered> deletedUser = userRepository.findRegisteredById(user.getUserId());
        assertFalse(deletedUser.isPresent(), "User should be deleted from the repository");
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
        Registered user = registedUser;
        LocalDate endDate = LocalDate.now().plusDays(7);
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        userRepository.suspendUser(user.getUserId(), endDate);
        assertTrue(userRepository.isUserSuspended(user.getUserId()), "User should be suspended");
        assertEquals(endDate, userRepository.getSuspensionEndDate(user.getUserId()), "End date should match");
    }
    
    @Test
    void testSuspendUserPermanently() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        userRepository.suspendUser(user.getUserId(), null);
        assertTrue(userRepository.isUserSuspended(user.getUserId()), "User should be suspended");
        assertNull(userRepository.getSuspensionEndDate(user.getUserId()), "End date should be null for permanent suspension");
    }
    
    @Test
    void testUnsuspendUser() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        userRepository.suspendUser(user.getUserId(), LocalDate.now().plusDays(7));
        assertTrue(userRepository.isUserSuspended(user.getUserId()), "User should be suspended");
        boolean result = userRepository.unsuspendUser(user.getUserId());
        assertTrue(result, "Unsuspend operation should return true");
        assertFalse(userRepository.isUserSuspended(user.getUserId()), "User should no longer be suspended");
    }
    
    @Test
    void testUnsuspendNonSuspendedUserReturnsFalse() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        boolean result = userRepository.unsuspendUser(user.getUserId());
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
        Registered user1 = registedUser;
        Registered user2 = new Registered("suspended2@example.com", "password", LocalDate.now(), "US");
        // Mock both findByUserId and findRegisteredById for both users
        Mockito.when(userJpaRepository.findRegisteredById(user1.getUserId())).thenReturn(Optional.of(user1));
        Mockito.when(userJpaRepository.findRegisteredById(user2.getUserId())).thenReturn(Optional.of(user2));
        Mockito.when(userJpaRepository.findRegisteredById(user1.getUserId())).thenReturn(Optional.of(user1));
        Mockito.when(userJpaRepository.findRegisteredById(user2.getUserId())).thenReturn(Optional.of(user2));
        userRepository.suspendUser(user1.getUserId(), LocalDate.now().plusDays(7));
        userRepository.suspendUser(user2.getUserId(), null); // Permanent suspension
        List<Registered> suspendedUsers = userRepository.getAllSuspendedUsers();
        assertEquals(2, suspendedUsers.size(), "There should be 2 suspended users");
        assertTrue(suspendedUsers.stream().anyMatch(u -> u.getUserId() == user1.getUserId()), "First user should be in suspended list");
        assertTrue(suspendedUsers.stream().anyMatch(u -> u.getUserId() == user2.getUserId()), "Second user should be in suspended list");
    }
    
    @Test
    void testCleanupExpiredSuspensions() {
        Registered user1 = new Registered("expired@example.com", "password", LocalDate.now(), "US");
        Registered user2 = new Registered("active@example.com", "password", LocalDate.now(), "US");
        Registered user3 = new Registered("permanent@example.com", "password", LocalDate.now(), "US");
        Mockito.when(userJpaRepository.findRegisteredById(user1.getUserId())).thenReturn(Optional.of(user1));
        Mockito.when(userJpaRepository.findRegisteredById(user2.getUserId())).thenReturn(Optional.of(user2));
        Mockito.when(userJpaRepository.findRegisteredById(user3.getUserId())).thenReturn(Optional.of(user3));
        userRepository.suspendUser(user1.getUserId(), LocalDate.now().minusDays(1)); // Expired
        userRepository.suspendUser(user2.getUserId(), LocalDate.now().plusDays(7)); // Active
        userRepository.suspendUser(user3.getUserId(), null); // Permanent
        int removedCount = userRepository.cleanupExpiredSuspensions();
        assertEquals(1, removedCount, "Should remove 1 expired suspension");
        assertFalse(userRepository.isUserSuspended(user1.getUserId()), "Expired suspension should be removed");
        assertTrue(userRepository.isUserSuspended(user2.getUserId()), "Active suspension should remain");
        assertTrue(userRepository.isUserSuspended(user3.getUserId()), "Permanent suspension should remain");
    }

        @Test
    void testUserDoesNotExist_throwsException() {
        int userId = 9999;
        Mockito.when(userJpaRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.getSuspensionEndDate(userId);
        });
        assertNotNull(exception, "Exception should not be null");
    }

    @Test
    void testUserNotSuspended_throwsException() {
        Registered user = registedUser;
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.getSuspensionEndDate(user.getUserId());
        });
        assertNotNull(exception, "Exception should not be null");
    }

    @Test
    void testUserSuspended_returnsEndDate() {
        Registered user = registedUser;
        LocalDate endDate = LocalDate.now().plusDays(7);
        Mockito.when(userJpaRepository.findRegisteredById(user.getUserId())).thenReturn(Optional.of(user));
        userRepository.suspendUser(user.getUserId(), endDate);
        assertEquals(endDate, userRepository.getSuspensionEndDate(user.getUserId()));
    }

}