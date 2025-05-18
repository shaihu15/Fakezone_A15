package UnitTesting;

import ApplicationLayer.DTO.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDTOTest {

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(1, "test@example.com", 25);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(userDTO, "UserDTO object should be created");
        assertEquals(1, userDTO.getUserId(), "User ID should match");
        assertEquals("test@example.com", userDTO.getUserEmail(), "User email should match");
        assertEquals(25, userDTO.getUserAge(), "User age should match");
    }

    @Test
    void defaultConstructor_ShouldCreateObjectWithDefaultValues() {
        UserDTO defaultUserDTO = new UserDTO();
        assertNotNull(defaultUserDTO, "Default UserDTO object should be created");
        assertEquals(0, defaultUserDTO.getUserId(), "Default User ID should be 0");
        assertNull(defaultUserDTO.getUserEmail(), "Default User email should be null");
        assertEquals(0, defaultUserDTO.getUserAge(), "Default User age should be 0");
    }

    @Test
    void getUserId_ShouldReturnCorrectId() {
        assertEquals(1, userDTO.getUserId(), "User ID should match");
    }

    @Test
    void getUserEmail_ShouldReturnCorrectEmail() {
        assertEquals("test@example.com", userDTO.getUserEmail(), "User email should match");
    }

    @Test
    void getUserAge_ShouldReturnCorrectAge() {
        assertEquals(25, userDTO.getUserAge(), "User age should match");
    }

    @Test
    void setUserId_ValidId_ShouldUpdateId() {
        userDTO.setUserId(2);
        assertEquals(2, userDTO.getUserId(), "User ID should be updated");
    }

    @Test
    void setUserEmail_ValidEmail_ShouldUpdateEmail() {
        userDTO.setUserEmail("new@example.com");
        assertEquals("new@example.com", userDTO.getUserEmail(), "User email should be updated");
    }

    @Test
    void setUserAge_ValidAge_ShouldUpdateAge() {
        userDTO.setUserAge(30);
        assertEquals(30, userDTO.getUserAge(), "User age should be updated");
    }
}