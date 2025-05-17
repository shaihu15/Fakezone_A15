package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import DomainLayer.Interfaces.INotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Registered;

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
        // Arrange
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
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        // Act
        Response<?> response = systemService.suspendUser(adminUser.getUserId(), regularUser.getUserId(), endDate);
        
        // Assert
        assertTrue(response.isSuccess());
        verify(mockUserService).suspendUser(adminUser.getUserId(), regularUser.getUserId(), endDate);
    }
    
    @Test
    void testSuspendUserPermanently() {
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
}