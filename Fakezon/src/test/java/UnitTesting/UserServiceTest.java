package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IRegisteredRole;
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

    @Test
    void testGetAllRoles_UserExists_ReturnsRoles() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        IRegisteredRole mockRole = mock(IRegisteredRole.class);
        roles.put(10, mockRole);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(roles);

        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
        assertEquals(roles, result);
    }

    @Test
    void testGetAllRoles_UserExists_RolesNull_ReturnsEmptyMap() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(null);

        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllRoles_UserNotFound_ThrowsException() {
        int userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.getAllRoles(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void testGetAllRoles_ExceptionInGetAllRoles_ThrowsRuntimeException() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenThrow(new RuntimeException("fail"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getAllRoles(userId));
        assertTrue(exception.getMessage().contains("Error retrieving roles for user " + userId));
    }
}
