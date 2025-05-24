package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.User;

public class UserServiceAdminTest {

    private UserService userService;
    private IUserRepository mockUserRepository;
    private Registered adminUser;
    private Registered regularUser;
    private Registered suspendedUser;
    
    @BeforeEach
    void setUp() {
        // Create mock objects
        mockUserRepository = mock(IUserRepository.class);
        userService = new UserService(mockUserRepository);
        
        // Create test user objects
        adminUser = new Registered("admin@example.com", "password", LocalDate.of(2000, 1, 1), "US");
        regularUser = new Registered("user@example.com", "password", LocalDate.of(2000, 1, 1), "US");
        suspendedUser = new Registered("suspended@example.com", "password", LocalDate.of(2000, 1, 1), "US");
        
        // Set up common mock behavior
        when(mockUserRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));
        when(mockUserRepository.findById(regularUser.getUserId())).thenReturn(Optional.of(regularUser));
        when(mockUserRepository.findById(suspendedUser.getUserId())).thenReturn(Optional.of(suspendedUser));
        when(mockUserRepository.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserRepository.isSystemAdmin(regularUser.getUserId())).thenReturn(false);
    }
    
    // System Admin Tests
    
    @Test
    void testAddSystemAdmin() {
        // Act
        userService.addSystemAdmin(regularUser.getUserId());
        
        // Assert
        verify(mockUserRepository).addSystemAdmin(regularUser.getUserId());
    }
    
    @Test
    void testAddSystemAdminThrowsExceptionWhenUserNotFound() {
        // Arrange
        int nonExistentUserId = 999;
        when(mockUserRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.addSystemAdmin(nonExistentUserId);
        });
    }
    
    @Test
    void testRemoveSystemAdmin() {
        // Arrange
        when(mockUserRepository.removeSystemAdmin(regularUser.getUserId())).thenReturn(true);
        
        // Act
        boolean result = userService.removeSystemAdmin(regularUser.getUserId());
        
        // Assert
        assertTrue(result);
        verify(mockUserRepository).removeSystemAdmin(regularUser.getUserId());
    }
    
    @Test
    void testIsSystemAdmin() {
        // Act
        boolean isAdmin = userService.isSystemAdmin(adminUser.getUserId());
        boolean isNotAdmin = userService.isSystemAdmin(regularUser.getUserId());
        
        // Assert
        assertTrue(isAdmin);
        assertFalse(isNotAdmin);
    }
    
    @Test
    void testGetAllSystemAdmins() {
        // Arrange
        List<Registered> admins = new ArrayList<>();
        admins.add(adminUser);
        when(mockUserRepository.getAllSystemAdmins()).thenReturn(admins);
        
        // Act
        List<Registered> result = userService.getAllSystemAdmins();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(adminUser, result.get(0));
    }
    
    @Test
    void testGetSystemAdminCount() {
        // Arrange
        when(mockUserRepository.getSystemAdminCount()).thenReturn(1);
        
        // Act
        int count = userService.getSystemAdminCount();
        
        // Assert
        assertEquals(1, count);
    }
    
    // User Suspension Tests
    
    @Test
    void testSuspendUser() {
        // Arrange
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        // Act
        userService.suspendUser(adminUser.getUserId(), regularUser.getUserId(), endDate);
        
        // Assert
        verify(mockUserRepository).suspendUser(regularUser.getUserId(), endDate);
    }
    
    @Test
    void testSuspendUserThrowsExceptionWhenNotAdmin() {
        // Arrange
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.suspendUser(regularUser.getUserId(), adminUser.getUserId(), endDate);
        });
    }
    
    @Test
    void testSuspendUserThrowsExceptionWhenSuspendingAdmin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.suspendUser(adminUser.getUserId(), adminUser.getUserId(), LocalDate.now().plusDays(7));
        });
    }
    
    @Test
    void testUnsuspendUser() {
        // Arrange
        when(mockUserRepository.unsuspendUser(regularUser.getUserId())).thenReturn(true);
        
        // Act
        boolean result = userService.unsuspendUser(adminUser.getUserId(), regularUser.getUserId());
        
        // Assert
        assertTrue(result);
        verify(mockUserRepository).unsuspendUser(regularUser.getUserId());
    }
    
    @Test
    void testUnsuspendUserThrowsExceptionWhenNotAdmin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.unsuspendUser(regularUser.getUserId(), suspendedUser.getUserId());
        });
    }
    
    @Test
    void testIsUserSuspended() {
        // Arrange
        when(mockUserRepository.isUserSuspended(suspendedUser.getUserId())).thenReturn(true);
        when(mockUserRepository.isUserSuspended(regularUser.getUserId())).thenReturn(false);
        
        // Act
        boolean isSuspended = userService.isUserSuspended(suspendedUser.getUserId());
        boolean isNotSuspended = userService.isUserSuspended(regularUser.getUserId());
        
        // Assert
        assertTrue(isSuspended);
        assertFalse(isNotSuspended);
    }
    
    @Test
    void testGetSuspensionEndDate() {
        // Arrange
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(mockUserRepository.getSuspensionEndDate(suspendedUser.getUserId())).thenReturn(endDate);
        
        // Act
        LocalDate result = userService.getSuspensionEndDate(adminUser.getUserId(), suspendedUser.getUserId());
        
        // Assert
        assertEquals(endDate, result);
    }
    
    @Test
    void testGetSuspensionEndDateThrowsExceptionWhenNotAdmin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getSuspensionEndDate(regularUser.getUserId(), suspendedUser.getUserId());
        });
    }
    
    @Test
    void testGetAllSuspendedUsers() {
        // Arrange
        List<Registered> suspendedUsers = new ArrayList<>();
        suspendedUsers.add(suspendedUser);
        when(mockUserRepository.getAllSuspendedUsers()).thenReturn(suspendedUsers);
        
        // Act
        List<Registered> result = userService.getAllSuspendedUsers(adminUser.getUserId());
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(suspendedUser, result.get(0));
    }
    
    @Test
    void testGetAllSuspendedUsersThrowsExceptionWhenNotAdmin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getAllSuspendedUsers(regularUser.getUserId());
        });
    }
    
    @Test
    void testCleanupExpiredSuspensions() {
        // Arrange
        when(mockUserRepository.cleanupExpiredSuspensions()).thenReturn(2);
        
        // Act
        int count = userService.cleanupExpiredSuspensions(adminUser.getUserId());
        
        // Assert
        assertEquals(2, count);
    }
    
    @Test
    void testCleanupExpiredSuspensionsThrowsExceptionWhenNotAdmin() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.cleanupExpiredSuspensions(regularUser.getUserId());
        });
    }

}