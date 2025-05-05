package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Repositories.UserRepository;



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


}