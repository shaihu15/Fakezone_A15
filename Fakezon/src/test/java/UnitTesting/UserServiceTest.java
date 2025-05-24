package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;

public class UserServiceTest {

    private UserService userService;
    private IUserRepository userRepository;
    private String Country = "IL"; // Example country  

    @BeforeEach
    void setUp() {
        userRepository = mock(IUserRepository.class); 
        userService = new UserService(userRepository); 
    }

    @Test
    void testConvertUserToDTO_Success() {
        Registered user = new Registered("test@example.com", "password123", LocalDate.of(2000, 1, 1),Country);

        UserDTO userDTO = userService.convertUserToDTO(user);

        assertNotNull(userDTO);
        assertEquals(user.getEmail(), userDTO.getUserEmail());
        assertEquals(user.getUserId(), userDTO.getUserId());
    }

    @Test
    void testConvertUserToDTO_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.convertUserToDTO(null));
    }

    @Test
    void testRemoveAssignmentMessage_UserExists_RemovesMessage() {
        int userId = 1;
        int storeId = 10;

        Registered mockUser = mock(Registered.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        userService.removeAssignmentMessage(storeId, userId);

        verify(mockUser, times(1)).removeAssignmentMessage(storeId);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testRemoveAssignmentMessage_UserNotFound_ThrowsException() {
        int userId = 999;
        int storeId = 10;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.removeAssignmentMessage(storeId, userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }
}
