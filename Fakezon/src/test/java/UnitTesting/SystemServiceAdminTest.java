package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Registered;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import DomainLayer.Model.User;
import DomainLayer.IRepository.IRegisteredRole;

public class SystemServiceAdminTest {

    private SystemService systemService;
    private IUserService mockUserService;
    private IStoreService mockStoreService;
    private IProductService mockProductService;
    private IOrderService mockOrderService;
    private IDelivery mockDeliveryService;
    private IAuthenticator mockAuthenticatorService;
    private IPayment mockPaymentService;
    private ApplicationEventPublisher mockPublisher;
    private INotificationWebSocketHandler mockNotificationWebSocketHandler;
    
    private Registered adminUser;
    private Registered regularUser;
    private Registered suspendedUser;
    
    @BeforeEach
    void setUp() {
        // Create mock services
        mockUserService = mock(IUserService.class);
        mockStoreService = mock(IStoreService.class);
        mockProductService = mock(IProductService.class);
        mockOrderService = mock(IOrderService.class);
        mockDeliveryService = mock(IDelivery.class);
        mockAuthenticatorService = mock(IAuthenticator.class);
        mockPaymentService = mock(IPayment.class);
        mockPublisher = mock(ApplicationEventPublisher.class);
        mockNotificationWebSocketHandler = mock(INotificationWebSocketHandler.class);
        
        // Initialize SystemService with mocks
        systemService = new SystemService(
            mockStoreService,
            mockUserService,
            mockProductService,
            mockOrderService,
            mockDeliveryService,
            mockAuthenticatorService,
            mockPaymentService,
            mockPublisher,
            mockNotificationWebSocketHandler
        );
        
        // Create test user objects
        adminUser = new Registered("admin@example.com", "password", LocalDate.of(2000, 1, 1), "US");
        regularUser = new Registered("user@example.com", "password", LocalDate.of(2000, 1, 1), "US");
        suspendedUser = new Registered("suspended@example.com", "password", LocalDate.of(2000, 1, 1), "US");

        // Common mock behaviors
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserService.isSystemAdmin(regularUser.getUserId())).thenReturn(false);
    }
    
    // Test user login with suspension check
    @Test
    void testLoginSuccessWhenUserNotSuspended() {
        // Arrange
        String email = "user@example.com";
        String password = "password";
        when(mockUserService.getUserByUserName(email)).thenReturn(Optional.of(regularUser));
        when(mockUserService.isUserSuspended(regularUser.getUserId())).thenReturn(false);
        when(mockAuthenticatorService.login(email, password)).thenReturn("token123");
        
        // Act
        Response<?> response = systemService.login(email, password);
        
        // Assert
        assertTrue(response.isSuccess());
    }
    
    @Test
    void testLoginFailureWhenUserSuspended() {
        // Arrange
        String email = "suspended@example.com";
        String password = "password";
        when(mockUserService.getUserByUserName(email)).thenReturn(Optional.of(suspendedUser));
        when(mockUserService.isUserSuspended(suspendedUser.getUserId())).thenReturn(true);
        
        // Act
        Response<?> response = systemService.login(email, password);
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        assertTrue(response.getMessage().contains("suspended"));
    }
    
    // System Admin Tests
    
    @Test
    void testAddSystemAdmin() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        // No need to stub addSystemAdmin since it's void

        // Act
        Response<?> response = systemService.addSystemAdmin(adminUser.getUserId(), regularUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        verify(mockUserService).addSystemAdmin(regularUser.getUserId());
    }
    
    @Test
    void testAddSystemAdminFailsWhenRequesterNotAdmin() {
        // Act
        Response<?> response = systemService.addSystemAdmin(regularUser.getUserId(), adminUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).addSystemAdmin(any(Integer.class));
    }
    
    @Test
    void testRemoveSystemAdmin() {
                    // Ensure unique user IDs for test logic
                    adminUser.setUserId(1);
                    regularUser.setUserId(2);
                    suspendedUser.setUserId(3);
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserService.removeSystemAdmin(regularUser.getUserId())).thenReturn(true);

        // Act
        Response<Boolean> response = systemService.removeSystemAdmin(adminUser.getUserId(), regularUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        verify(mockUserService).removeSystemAdmin(regularUser.getUserId());
    }
    
    @Test
    void testRemoveSystemAdminFailsWhenRequesterNotAdmin() {
        // Act
        Response<Boolean> response = systemService.removeSystemAdmin(regularUser.getUserId(), adminUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).removeSystemAdmin(any(Integer.class));
    }
    
    @Test
    void testRemoveSystemAdminFailsWhenRemovingSelf() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);

        // Act
        Response<Boolean> response = systemService.removeSystemAdmin(adminUser.getUserId(), adminUser.getUserId());

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        assertTrue(response.getMessage().contains("Cannot remove yourself"));
        verify(mockUserService, never()).removeSystemAdmin(any(Integer.class));
    }
    
    @Test
    void testIsSystemAdmin() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        
        // Act
        Response<Boolean> response = systemService.isSystemAdmin(adminUser.getUserId());
        
        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
    }
    
    @Test
    void testGetAllSystemAdmins() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        List<Registered> admins = new ArrayList<>();
        admins.add(adminUser);
        when(mockUserService.getAllSystemAdmins()).thenReturn(admins);
        
        // Act
        Response<List<Registered>> response = systemService.getAllSystemAdmins(adminUser.getUserId());
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals(adminUser, response.getData().get(0));
    }
    
    @Test
    void testGetAllSystemAdminsFailsWhenRequesterNotAdmin() {
        // Act
        Response<List<Registered>> response = systemService.getAllSystemAdmins(regularUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).getAllSystemAdmins();
    }
    
    @Test
    void testGetSystemAdminCount() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserService.getSystemAdminCount()).thenReturn(1);

        // Act
        Response<Integer> response = systemService.getSystemAdminCount(adminUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData());
    }
    
    @Test
    void testGetSystemAdminCountFailsWhenRequesterNotAdmin() {
        // Act
        Response<Integer> response = systemService.getSystemAdminCount(regularUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).getSystemAdminCount();
    }
    
    // User Suspension Tests
    
    @Test
    void testSuspendUserTemporarily() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        LocalDate endDate = LocalDate.now().plusDays(7);
        // No need to stub suspendUser if it returns void

        // Act
        Response<?> response = systemService.suspendUser(adminUser.getUserId(), regularUser.getUserId(), endDate);

        // Assert
        assertTrue(response.isSuccess());
        verify(mockUserService).suspendUser(adminUser.getUserId(), regularUser.getUserId(), endDate);
    }
    
    @Test
    void testSuspendUserPermanently() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        // No need to stub suspendUser if it returns void

        // Act
        Response<?> response = systemService.suspendUser(adminUser.getUserId(), regularUser.getUserId(), null);

        // Assert
        assertTrue(response.isSuccess());
        verify(mockUserService).suspendUser(adminUser.getUserId(), regularUser.getUserId(), null);
    }
    
    @Test
    void testSuspendUserFailsWhenRequesterNotAdmin() {
        // Act
        Response<?> response = systemService.suspendUser(regularUser.getUserId(), adminUser.getUserId(), LocalDate.now().plusDays(7));
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).suspendUser(anyInt(), anyInt(), any(LocalDate.class));
    }
    
    @Test
    void testUnsuspendUser() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserService.unsuspendUser(adminUser.getUserId(), suspendedUser.getUserId())).thenReturn(true);

        // Act
        Response<Boolean> response = systemService.unsuspendUser(adminUser.getUserId(), suspendedUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        verify(mockUserService).unsuspendUser(adminUser.getUserId(), suspendedUser.getUserId());
    }
    
    @Test
    void testUnsuspendUserFailsWhenRequesterNotAdmin() {
        // Act
        Response<Boolean> response = systemService.unsuspendUser(regularUser.getUserId(), suspendedUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).unsuspendUser(anyInt(), anyInt());
    }
    
    @Test
    void testIsUserSuspended() {
        // Arrange
        when(mockUserService.isUserSuspended(suspendedUser.getUserId())).thenReturn(true);
        
        // Act
        Response<Boolean> response = systemService.isUserSuspended(suspendedUser.getUserId());
        
        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
    }
    
    @Test
    void testGetSuspensionEndDate() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        LocalDate endDate = LocalDate.now().plusDays(7);
        when(mockUserService.getSuspensionEndDate(adminUser.getUserId(), suspendedUser.getUserId())).thenReturn(endDate);

        // Act
        Response<LocalDate> response = systemService.getSuspensionEndDate(adminUser.getUserId(), suspendedUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(endDate, response.getData());
    }
    
    @Test
    void testGetSuspensionEndDateFailsWhenRequesterNotAdmin() {
        // Act
        Response<LocalDate> response = systemService.getSuspensionEndDate(regularUser.getUserId(), suspendedUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).getSuspensionEndDate(anyInt(), anyInt());
    }
    
    @Test
    void testGetAllSuspendedUsers() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        List<Registered> suspendedUsers = new ArrayList<>();
        suspendedUsers.add(suspendedUser);
        when(mockUserService.getAllSuspendedUsers(adminUser.getUserId())).thenReturn(suspendedUsers);
        
        // Act
        Response<List<Registered>> response = systemService.getAllSuspendedUsers(adminUser.getUserId());
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals(suspendedUser, response.getData().get(0));
    }
    
    @Test
    void testGetAllSuspendedUsersFailsWhenRequesterNotAdmin() {
        // Act
        Response<List<Registered>> response = systemService.getAllSuspendedUsers(regularUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).getAllSuspendedUsers(anyInt());
    }
    
    @Test
    void testCleanupExpiredSuspensions() {
        // Arrange
        when(mockUserService.isSystemAdmin(adminUser.getUserId())).thenReturn(true);
        when(mockUserService.cleanupExpiredSuspensions(adminUser.getUserId())).thenReturn(2);

        // Act
        Response<Integer> response = systemService.cleanupExpiredSuspensions(adminUser.getUserId());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData());
    }
    
    @Test
    void testCleanupExpiredSuspensionsFailsWhenRequesterNotAdmin() {
        // Act
        Response<Integer> response = systemService.cleanupExpiredSuspensions(regularUser.getUserId());
        
        // Assert
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).cleanupExpiredSuspensions(anyInt());
    }
    //----------------Unsigned User Tests------------------------------
    
     @Test
    void testCreateUnsignedUser_Success() {
        // Arrange
        User mockUser = mock(User.class);
        when(mockUserService.createUnsignedUser()).thenReturn(mockUser);

        // Act
        Response<UserDTO> response = systemService.createUnsignedUser();

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Unsigned user created successfully", response.getMessage());
        verify(mockUserService, times(1)).createUnsignedUser();
    }

    @Test
    void testCreateUnsignedUser_IllegalArgumentException() {
        // Arrange
        when(mockUserService.createUnsignedUser()).thenThrow(new IllegalArgumentException("User already exists"));

        // Act
        Response<UserDTO> response = systemService.createUnsignedUser();

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User already exists", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, times(1)).createUnsignedUser();
    }

    @Test
    void testCreateUnsignedUser_GenericException() {
        // Arrange
        when(mockUserService.createUnsignedUser())
                .thenThrow(new RuntimeException("Database failure"));

        // Act
        Response<UserDTO> response = systemService.createUnsignedUser();

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error adding unsigned user: Database failure"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        verify(mockUserService, times(1)).createUnsignedUser();
    }

    @Test
    void testRemoveUnsignedUser_Success() {
        when(mockUserService.removeUnsignedUser(-1)).thenReturn(true);

        Response<Boolean> response = systemService.removeUnsignedUser(-1);

        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        assertEquals("Unsigned user removed successfully", response.getMessage());
    }

    @Test
    void testRemoveUnsignedUser_NotFound() {
        when(mockUserService.removeUnsignedUser(-2)).thenReturn(false);

        Response<Boolean> response = systemService.removeUnsignedUser(-2);

        assertTrue(response.isSuccess());
        assertFalse(response.getData());
        assertEquals("No unsigned user with that ID found", response.getMessage());
    }

    @Test
    void testRemoveUnsignedUser_IllegalArgumentException() {
        when(mockUserService.removeUnsignedUser(-3))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        Response<Boolean> response = systemService.removeUnsignedUser(-3);

        assertFalse(response.isSuccess());
        assertEquals("Invalid user ID", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetUserRoles_Success() {
        int userId = 123;
        // Arrange
        when(mockUserService.isUserLoggedIn(userId)).thenReturn(true);
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        IRegisteredRole mockRole = mock(IRegisteredRole.class);
        roles.put(1, mockRole);
        when(mockUserService.getAllRoles(userId)).thenReturn(roles);

        // Act
        Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(roles, response.getData());
        assertEquals("User roles retrieved successfully", response.getMessage());
    }

    @Test
    void testGetUserRoles_UserNotLoggedIn() {
        int userId = 123;
        // Arrange
        when(mockUserService.isUserLoggedIn(userId)).thenReturn(false);

        // Act
        Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ApplicationLayer.Enums.ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockUserService, never()).getAllRoles(userId);
    }

    @Test
    void testGetUserRoles_IllegalArgumentException() {
        int userId = 123;
        // Arrange
        when(mockUserService.isUserLoggedIn(userId)).thenReturn(true);
        when(mockUserService.getAllRoles(userId)).thenThrow(new IllegalArgumentException("Bad input"));

        // Act
        Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Bad input", response.getMessage());
        assertEquals(ApplicationLayer.Enums.ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetUserRoles_GenericException() {
        int userId = 123;
        // Arrange
        when(mockUserService.isUserLoggedIn(userId)).thenReturn(true);
        when(mockUserService.getAllRoles(userId)).thenThrow(new RuntimeException("Something failed"));

        // Act
        Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error getting user roles: Something failed"));
        assertEquals(ApplicationLayer.Enums.ErrorType.INTERNAL_ERROR, response.getErrorType());
    }



}