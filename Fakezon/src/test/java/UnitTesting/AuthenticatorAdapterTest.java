package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Model.Registered;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Security.TokenService;

public class AuthenticatorAdapterTest {
    
    @Mock
    private IUserService mockUserService;
    
    @Mock
    private TokenService mockTokenService;
    
    private AuthenticatorAdapter authenticatorAdapter;
    
    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        
        // Create a test instance of AuthenticatorAdapter with the mocked dependencies
        authenticatorAdapter = new AuthenticatorAdapter(mockUserService);
        
        // Replace the automatically created TokenService with our mock
        // We need to use reflection since TokenService is initialized in the constructor
        try {
            java.lang.reflect.Field tokenServiceField = AuthenticatorAdapter.class.getDeclaredField("tokenService");
            tokenServiceField.setAccessible(true);
            tokenServiceField.set(authenticatorAdapter, mockTokenService);
        } catch (Exception e) {
            fail("Failed to set mock TokenService: " + e.getMessage());
        }
    }
    
    @Test
    void register_WhenSuccessful_ShouldReturnToken() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        UserDTO mockUserDTO = new UserDTO(1, email, 33);
        String expectedToken = "valid-token-123";
        
        when(mockUserService.registerUser(email, password, dateOfBirth)).thenReturn(mockUserDTO);
        when(mockTokenService.generateToken(email)).thenReturn(expectedToken);
        
        // Act
        String actualToken = authenticatorAdapter.register(email, password, dateOfBirth);
        
        // Assert
        assertEquals(expectedToken, actualToken);
        verify(mockUserService).registerUser(email, password, dateOfBirth);
        verify(mockTokenService).generateToken(email);
    }
    
    @Test
    void register_WhenUserServiceReturnsNull_ShouldReturnNull() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        
        when(mockUserService.registerUser(email, password, dateOfBirth)).thenReturn(null);
        
        // Act
        String token = authenticatorAdapter.register(email, password, dateOfBirth);
        
        // Assert
        assertNull(token);
        verify(mockUserService).registerUser(email, password, dateOfBirth);
        verify(mockTokenService, never()).generateToken(anyString());
    }
    
    @Test
    void register_WhenUserServiceThrowsException_ShouldReturnNull() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        
        when(mockUserService.registerUser(email, password, dateOfBirth))
            .thenThrow(new IllegalArgumentException("User already exists"));
        
        // Act
        String token = authenticatorAdapter.register(email, password, dateOfBirth);
        
        // Assert
        assertNull(token);
        verify(mockUserService).registerUser(email, password, dateOfBirth);
        verify(mockTokenService, never()).generateToken(anyString());
    }
    
    @Test
    void login_WhenSuccessful_ShouldReturnToken() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        Registered mockUser = mock(Registered.class);
        Optional<Registered> optionalUser = Optional.of(mockUser);
        String expectedToken = "valid-token-456";
        
        when(mockUserService.getUserByUserName(email)).thenReturn(optionalUser);
        when(mockUser.getEmail()).thenReturn(email);
        when(mockTokenService.generateToken(email)).thenReturn(expectedToken);
        
        // Act
        String actualToken = authenticatorAdapter.login(email, password);
        
        // Assert
        assertEquals(expectedToken, actualToken);
        verify(mockUserService).login(email, password);
        verify(mockUserService).getUserByUserName(email);
        verify(mockTokenService).generateToken(email);
    }
    
    @Test
    void login_WhenUserNotFound_ShouldReturnNull() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";
        
        when(mockUserService.getUserByUserName(email)).thenReturn(Optional.empty());
        
        // Act
        String token = authenticatorAdapter.login(email, password);
        
        // Assert
        assertNull(token);
        verify(mockUserService).login(email, password);
        verify(mockUserService).getUserByUserName(email);
        verify(mockTokenService, never()).generateToken(anyString());
    }
    
    @Test
    void login_WhenLoginThrowsException_ShouldReturnNull() {
        // Arrange
        String email = "test@example.com";
        String password = "wrong-password";
        
        doThrow(new IllegalArgumentException("Incorrect password"))
            .when(mockUserService).login(email, password);
        
        // Act
        String token = authenticatorAdapter.login(email, password);
        
        // Assert
        assertNull(token);
        verify(mockUserService).login(email, password);
        verify(mockUserService, never()).getUserByUserName(anyString());
        verify(mockTokenService, never()).generateToken(anyString());
    }
    
    @Test
    void logout_ShouldCallUserServiceLogout() {
        // Arrange
        String email = "test@example.com";
        
        // Act
        authenticatorAdapter.logout(email);
        
        // Assert
        verify(mockUserService).logout(email);
    }
    
    @Test
    void logout_WhenUserServiceThrowsException_ShouldCatchException() {
        // Arrange
        String email = "nonexistent@example.com";
        
        doThrow(new IllegalArgumentException("User not found"))
            .when(mockUserService).logout(email);
        
        // Act & Assert
        // Should not throw an exception
        assertDoesNotThrow(() -> authenticatorAdapter.logout(email));
        verify(mockUserService).logout(email);
    }
    
    @Test
    void isValid_WhenTokenIsValid_ShouldReturnTrue() {
        // Arrange
        String token = "valid-token-789";
        when(mockTokenService.validateToken(token)).thenReturn(true);
        
        // Act
        boolean isValid = authenticatorAdapter.isValid(token);
        
        // Assert
        assertTrue(isValid);
        verify(mockTokenService).validateToken(token);
    }
    
    @Test
    void isValid_WhenTokenIsInvalid_ShouldReturnFalse() {
        // Arrange
        String token = "invalid-token";
        when(mockTokenService.validateToken(token)).thenReturn(false);
        
        // Act
        boolean isValid = authenticatorAdapter.isValid(token);
        
        // Assert
        assertFalse(isValid);
        verify(mockTokenService).validateToken(token);
    }
}