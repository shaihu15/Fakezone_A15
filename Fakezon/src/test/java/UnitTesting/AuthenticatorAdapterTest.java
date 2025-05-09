package UnitTesting;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.IUserService;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Security.TokenService;

import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;

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
        String country = "IL";
        
        when(mockUserService.registerUser(email, password, dateOfBirth,country)).thenReturn(mockUserDTO);
        when(mockTokenService.generateToken(email, 1)).thenReturn(expectedToken);
        
        // Act
        Response<String> response = authenticatorAdapter.register(email, password, dateOfBirth, country);
        String actualToken = response.getData(); // Assuming getData() extracts the String from Response
        
        // Assert
        assertEquals(expectedToken, actualToken);
        verify(mockUserService).registerUser(email, password, dateOfBirth,country);
        verify(mockTokenService).generateToken(email, 1);
    }
    
    @Test
    void register_WhenUserServiceReturnsNull_ShouldReturnNull() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        String country = "IL";
        when(mockUserService.registerUser(email, password, dateOfBirth,country)).thenReturn(null);
        
        // Act
        Response<String> response = authenticatorAdapter.register(email, password, dateOfBirth,country);
        String actualToken = response.getData(); // Assuming getData() extracts the String from Response

        // Assert
        assertNull(actualToken);
        verify(mockUserService).registerUser(email, password, dateOfBirth,country);
        verify(mockTokenService, never()).generateToken(anyString(), anyInt());
    }
    
    @Test
    void register_WhenUserServiceThrowsException_ShouldReturnNull() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        String country = "IL";

        when(mockUserService.registerUser(email, password, dateOfBirth,country))
            .thenThrow(new IllegalArgumentException("User already exists"));
        
        // Act
        Response<String> response = authenticatorAdapter.register(email, password, dateOfBirth,country);
        
        // Assert
        assertNull(response.getData()); // Assuming getData() extracts the String from Response
        verify(mockUserService).registerUser(email, password, dateOfBirth,country);
        verify(mockTokenService, never()).generateToken(anyString(), anyInt());
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

    @Test
    void getUserIdFromToken_WhenValidToken_ShouldReturnUserId() {
        // Arrange
        String token = "valid-token-789";
        int expectedUserId = 42;
        when(mockTokenService.validateToken(token)).thenReturn(true);
        when(mockTokenService.extractUserId(token)).thenReturn(expectedUserId);
        
        // Act
        int actualUserId = authenticatorAdapter.getUserId(token);
        
        // Assert
        assertEquals(expectedUserId, actualUserId);
    }
    
    @Test
    void getUserIdFromToken_WhenInvalidToken_ShouldReturnNull() {
        // Arrange
        String token = "invalid-token";
        when(mockTokenService.validateToken(token)).thenReturn(false);
        
        // Act
        int userId = authenticatorAdapter.getUserId(token);
        
        // Assert
        assertEquals(0, userId); // Assuming 0 is the default value for an invalid token
    }
    
}