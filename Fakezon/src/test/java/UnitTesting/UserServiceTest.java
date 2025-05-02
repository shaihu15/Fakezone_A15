package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;

public class UserServiceTest {

    private UserService userService;
    private IUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class); 
        userService = new UserService(userRepository); 
    }

    @Test
    void testConvertUserToDTO_Success() {
        Registered user = new Registered("test@example.com", "password123", LocalDate.of(2000, 1, 1));

        UserDTO userDTO = userService.convertUserToDTO(user);

        assertNotNull(userDTO);
        assertEquals(user.getEmail(), userDTO.getUserEmail());
        assertEquals(user.getUserID(), userDTO.getUserId());
    }

    @Test
    void testConvertUserToDTO_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.convertUserToDTO(null));
    }
}
