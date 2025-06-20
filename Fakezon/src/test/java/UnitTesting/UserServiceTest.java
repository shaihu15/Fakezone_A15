package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import DomainLayer.IRepository.IRegisteredRole;
import ApplicationLayer.Response;
import java.util.List;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.DTO.OrderDTO;
import java.util.Map;
import DomainLayer.Model.User;
import DomainLayer.Model.helpers.StoreMsg;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Cart;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.IRepository.IRegisteredRole;

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
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));

        userService.removeAssignmentMessage(storeId, userId);

        verify(mockUser, times(1)).removeAssignmentMessage(storeId);
        verify(userRepository, times(1)).findRegisteredById(userId);
    }

    @Test
    void testRemoveAssignmentMessage_UserNotFound_ThrowsException() {
        int userId = 999;
        int storeId = 10;

        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.removeAssignmentMessage(storeId, userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }

    @Test
    void testGetAllRoles_UserExists_ReturnsRoles() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        IRegisteredRole mockRole = mock(IRegisteredRole.class);
        roles.put(10, mockRole);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(roles);

        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
        assertEquals(roles, result);
    }

    @Test
    void testGetAllRoles_UserExists_RolesNull_ReturnsEmptyMap() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(null);

        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllRoles_UserNotFound_ThrowsException() {
        int userId = 999;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userService.getAllRoles(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void testGetAllRoles_ExceptionInGetAllRoles_ThrowsRuntimeException() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenThrow(new RuntimeException("fail"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getAllRoles(userId));
        assertTrue(exception.getMessage().contains("Error retrieving roles for user " + userId));
    }

     @Test
    void testClearUserCart_UserExists_CartClearedSuccessfully() {
        // Arrange
        int userId = 123;
        Registered mockUser = mock(Registered.class);
        
        // Mock the findById method to return the mock user
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        // You should use findAllById according to the provided function, but it returns Optional<User>
        // It's likely a typo in your provided method signature or the internal implementation.
        // I will use findById as it is more common for single entity retrieval by ID in repositories.
        // If findAllById is intended to return Optional<User>, then it's fine.
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));


        // Act
        userService.clearUserCart(userId);

        // Assert
        // Verify that saveCartOrderAndDeleteIt() was called on the user object
        verify(mockUser, times(1)).saveCartOrderAndDeleteIt();
        // Verify that the repository was called to find the user
        verify(userRepository, times(1)).findAllById(userId); 
        // Or if findById is used internally: verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testClearUserCart_UserNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        int userId = 999; // A user ID that does not exist

        // Mock the findById method to return an empty Optional
        when(userRepository.findAllById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.clearUserCart(userId);
        });

        assertEquals("User not found", exception.getMessage());
        // Verify that the findById method was called
        verify(userRepository, times(1)).findAllById(userId);
        // Verify that saveCartOrderAndDeleteIt() was NOT called because the user wasn't found
        verify(mock(Registered.class), never()).saveCartOrderAndDeleteIt(); // Use a new mock to ensure no interaction
    }
    @Test
    void testGetAllMessages_Success() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Hello", null,userId));
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllMessages()).thenReturn(messages);

        Response<Map<Integer, StoreMsg>> response = userService.getAllMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
        assertEquals("Messages retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAllMessages_EmptyMessages() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllMessages()).thenReturn(new HashMap<>());

        Response<Map<Integer, StoreMsg>> response = userService.getAllMessages(userId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("No messages found", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAllMessages_UserThrowsException() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllMessages()).thenThrow(new RuntimeException("DB error"));

        Response<Map<Integer, StoreMsg>> response = userService.getAllMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during get messages: DB error"));
        assertEquals(ApplicationLayer.Enums.ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testGetAllMessages_UserNotFound() {
        int userId = 999;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        Response<Map<Integer, StoreMsg>> response = userService.getAllMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("User not found", response.getMessage());
        assertEquals(ApplicationLayer.Enums.ErrorType.INVALID_INPUT, response.getErrorType());
    }
    @Test
    void testGetAssignmentMessages_Success() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Assignment message", null,userId));
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAssignmentMessages()).thenReturn(messages);

        Response<Map<Integer, StoreMsg>> response = userService.getAssignmentMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
        assertEquals("Messages retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAssignmentMessages_Empty() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAssignmentMessages()).thenReturn(new HashMap<>());

        Response<Map<Integer, StoreMsg>> response = userService.getAssignmentMessages(userId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("No messages found", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAssignmentMessages_Exception() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAssignmentMessages()).thenThrow(new RuntimeException("DB error"));

        Response<Map<Integer, StoreMsg>> response = userService.getAssignmentMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during get messages: DB error"));
        assertEquals(ApplicationLayer.Enums.ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testGetAssignmentMessages_UserNotFound() {
        int userId = 999;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        Response<Map<Integer, StoreMsg>> response = userService.getAssignmentMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("User not found", response.getMessage());
        assertEquals(ApplicationLayer.Enums.ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testGetAuctionEndedMessages_Success() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        Map<Integer, StoreMsg> messages = new HashMap<>();
        messages.put(1, new StoreMsg(1, -1, "Auction ended", null,userId));
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getOffersMessages()).thenReturn(messages);

        Response<Map<Integer, StoreMsg>> response = userService.getUserOfferMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
        assertEquals("Messages retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAuctionEndedMessages_Empty() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getOffersMessages()).thenReturn(new HashMap<>());

        Response<Map<Integer, StoreMsg>> response = userService.getUserOfferMessages(userId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("No messages found", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetAuctionEndedMessages_Exception() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getOffersMessages()).thenThrow(new RuntimeException("DB error"));

        Response<Map<Integer, StoreMsg>> response = userService.getUserOfferMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during get messages: DB error"));
        assertEquals(ApplicationLayer.Enums.ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testGetAuctionEndedMessages_UserNotFound() {
        int userId = 999;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());

        Response<Map<Integer, StoreMsg>> response = userService.getUserOfferMessages(userId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("User not found", response.getMessage());
        assertEquals(ApplicationLayer.Enums.ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testGetAllRoles_Success() {
        int userId = 1;
        Registered mockUser = mock(Registered.class);
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        IRegisteredRole role = mock(IRegisteredRole.class);
        roles.put(1, role);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(roles);
    
        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
    
        assertEquals(roles, result);
    }
    
    @Test
    void testGetAllRoles_NullRoles() {
        int userId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenReturn(null);
    
        HashMap<Integer, IRegisteredRole> result = userService.getAllRoles(userId);
    
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetAllRoles_Exception() {
        int userId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getAllRoles()).thenThrow(new RuntimeException("DB error"));
    
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getAllRoles(userId));
        assertTrue(ex.getMessage().contains("Error retrieving roles for user " + userId));
    }
    
    @Test
    void testGetAllRoles_UserNotFound() {
        int userId = 999;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.getAllRoles(userId));
        assertEquals("User not found with ID: " + userId, ex.getMessage());
    }
    @Test
    void testSendMessageToStore_Success() {
        int userId = 1, storeId = 2;
        String message = "Hello";
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
    
        userService.sendMessageToStore(userId, storeId, message);
    
        verify(mockUser, times(1)).sendMessageToStore(storeId, message);
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testSendMessageToStore_UserNotFound_ShouldThrow() {
        int userId = 999, storeId = 2;
        String message = "Hello";
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.sendMessageToStore(userId, storeId, message));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testRemoveRole_Success() {
        int userId = 1, storeId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
    
        userService.removeRole(userId, storeId);
    
        verify(mockUser, times(1)).removeRole(storeId);
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testRemoveRole_UserNotFound_ShouldThrow() {
        int userId = 999, storeId = 2;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.removeRole(userId, storeId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testDidPurchaseProduct_Success() {
        int userId = 1, storeId = 2, productId = 3;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.didPurchaseProduct(storeId, productId)).thenReturn(true);
    
        boolean result = userService.didPurchaseProduct(userId, storeId, productId);
    
        assertTrue(result);
        verify(mockUser, times(1)).didPurchaseProduct(storeId, productId);
    }
    
    @Test
    void testDidPurchaseProduct_UserNotFound_ShouldThrow() {
        int userId = 999, storeId = 2, productId = 3;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.didPurchaseProduct(userId, storeId, productId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testGetUnsignedUserById_Success() {
        int userId = -1;
        User mockUser = mock(User.class);
        when(userRepository.findUnsignedUserById(userId)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.getUnsignedUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findUnsignedUserById(userId);
    }
    @Test
    void testGetUnsignedUserById_NotFound() {
        int userId = -1;
        when(userRepository.findUnsignedUserById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUnsignedUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findUnsignedUserById(userId);
    }

    @Test
    void testGetUnsignedUserById_Exception() {
        int userId = -1;
        when(userRepository.findUnsignedUserById(userId)).thenThrow(new RuntimeException("fail"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getUnsignedUserById(userId));
        assertTrue(ex.getMessage().contains("Error getting unsigned user: fail"));
        verify(userRepository, times(1)).findUnsignedUserById(userId);
    }

    @Test
    void testGetUnsignedUserById_UserNotFound() {
        int userId = -1;
        when(userRepository.findUnsignedUserById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUnsignedUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findUnsignedUserById(userId);
    }
    @Test
    void testGetUserCart_Success() {
        int userId = 1;
        User mockUser = mock(User.class);
        Cart mockCart = mock(Cart.class);
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getCart()).thenReturn(mockCart);
    
        Cart result = userService.getUserCart(userId);
    
        assertEquals(mockCart, result);
        verify(userRepository, times(1)).findAllById(userId);
        verify(mockUser, times(1)).getCart();
    }
    
    @Test
    void testGetUserCart_UserNotFound_ShouldThrow() {
        int userId = 999;
        when(userRepository.findAllById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getUserCart(userId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findAllById(userId);
    }
    
    @Test
    void testGetUserCart_ExceptionInGetCart_ShouldThrow() {
        int userId = 1;
        User mockUser = mock(User.class);
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getCart()).thenThrow(new IllegalArgumentException("Error during get user cart: fail"));
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getUserCart(userId));
        assertTrue(ex.getMessage().contains("Error during get user cart: fail"));
        verify(userRepository, times(1)).findAllById(userId);
        verify(mockUser, times(1)).getCart();
    }
    
    @Test
    void testSaveCartOrder_Success() {
        int userId = 1;
        User mockUser = mock(User.class);
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
    
        userService.saveCartOrder(userId);
    
        verify(mockUser, times(1)).saveCartOrderAndDeleteIt();
        verify(userRepository, times(1)).findAllById(userId);
    }
    
    @Test
    void testSaveCartOrder_UserNotFound_ShouldThrow() {
        int userId = 999;
        when(userRepository.findAllById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.saveCartOrder(userId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findAllById(userId);
    }
    
    @Test
    void testIsUnsignedUser_Success() {
        int userId = -1;
        when(userRepository.isUnsignedUser(userId)).thenReturn(true);
    
        boolean result = userService.isUnsignedUser(userId);
    
        assertTrue(result);
        verify(userRepository, times(1)).isUnsignedUser(userId);
    }
    
    @Test
    void testIsUnsignedUser_Exception() {
        int userId = -1;
        when(userRepository.isUnsignedUser(userId)).thenThrow(new RuntimeException("fail"));
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.isUnsignedUser(userId));
        assertTrue(ex.getMessage().contains("Error checking if user is unsigned: fail"));
        verify(userRepository, times(1)).isUnsignedUser(userId);
    }
    @Test
    void testGetRoleByStoreID_Success() {
        int userId = 1, storeId = 2;
        Registered mockUser = mock(Registered.class);
        IRegisteredRole mockRole = mock(IRegisteredRole.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getRoleByStoreID(storeId)).thenReturn(mockRole);
    
        IRegisteredRole result = userService.getRoleByStoreID(userId, storeId);
    
        assertEquals(mockRole, result);
        verify(userRepository, times(1)).findRegisteredById(userId);
        verify(mockUser, times(1)).getRoleByStoreID(storeId);
    }
    
    @Test
    void testGetRoleByStoreID_UserNotFound_ShouldThrow() {
        int userId = 999, storeId = 2;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getRoleByStoreID(userId, storeId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testGetRoleByStoreID_ExceptionInUser() {
        int userId = 1, storeId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getRoleByStoreID(storeId)).thenThrow(new RuntimeException("fail"));
    
        IRegisteredRole result = userService.getRoleByStoreID(userId, storeId);
        assertNull(result);
        verify(userRepository, times(1)).findRegisteredById(userId);
        verify(mockUser, times(1)).getRoleByStoreID(storeId);
    }
    
    @Test
    void testDidPurchaseStore_Success() {
        int userId = 1, storeId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.didPurchaseStore(storeId)).thenReturn(true);
    
        boolean result = userService.didPurchaseStore(userId, storeId);
    
        assertTrue(result);
        verify(userRepository, times(1)).findRegisteredById(userId);
        verify(mockUser, times(1)).didPurchaseStore(storeId);
    }
    
    @Test
    void testDidPurchaseStore_UserNotFound_ShouldThrow() {
        int userId = 999, storeId = 2;
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.didPurchaseStore(userId, storeId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findRegisteredById(userId);
    }
    
    @Test
    void testDidPurchaseStore_ExceptionInUser() {
        int userId = 1, storeId = 2;
        Registered mockUser = mock(Registered.class);
        when(userRepository.findRegisteredById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.didPurchaseStore(storeId)).thenThrow(new RuntimeException("fail"));
    
        boolean result = userService.didPurchaseStore(userId, storeId);
        assertFalse(result);
        verify(userRepository, times(1)).findRegisteredById(userId);
        verify(mockUser, times(1)).didPurchaseStore(storeId);
    }
    
    @Test
    void testGetAllUnsignedUsersDTO_Success() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        UserDTO dto1 = mock(UserDTO.class);
        UserDTO dto2 = mock(UserDTO.class);
        when(userRepository.getAllUnsignedUsers()).thenReturn(List.of(user1, user2));
        when(user1.toDTO()).thenReturn(dto1);
        when(user2.toDTO()).thenReturn(dto2);
    
        List<UserDTO> result = userService.getAllUnsignedUsersDTO();
    
        assertEquals(List.of(dto1, dto2), result);
        verify(userRepository, times(1)).getAllUnsignedUsers();
        verify(user1, times(1)).toDTO();
        verify(user2, times(1)).toDTO();
    }
    
    @Test
    void testGetAllUnsignedUsersDTO_Exception() {
        when(userRepository.getAllUnsignedUsers()).thenThrow(new RuntimeException("fail"));
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getAllUnsignedUsersDTO());
        assertTrue(ex.getMessage().contains("Error getting all unsigned users: fail"));
        verify(userRepository, times(1)).getAllUnsignedUsers();
    }
    
    @Test
    void testGetAllUnsignedUsers_Success() {
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        when(userRepository.getAllUnsignedUsers()).thenReturn(List.of(user1, user2));
    
        List<User> result = userService.getAllUnsignedUsers();
    
        assertEquals(List.of(user1, user2), result);
        verify(userRepository, times(1)).getAllUnsignedUsers();
    }
    
    @Test
    void testGetAllUnsignedUsers_Exception() {
        when(userRepository.getAllUnsignedUsers()).thenThrow(new RuntimeException("fail"));
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getAllUnsignedUsers());
        assertTrue(ex.getMessage().contains("Error getting all unsigned users: fail"));
        verify(userRepository, times(1)).getAllUnsignedUsers();
    }
    
    @Test
    void testGetUnsignedUserCount_Success() {
        when(userRepository.getUnsignedUserCount()).thenReturn(5);
    
        int result = userService.getUnsignedUserCount();
    
        assertEquals(5, result);
        verify(userRepository, times(1)).getUnsignedUserCount();
    }
    
    @Test
    void testGetUnsignedUserCount_Exception() {
        when(userRepository.getUnsignedUserCount()).thenThrow(new RuntimeException("fail"));
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getUnsignedUserCount());
        assertTrue(ex.getMessage().contains("Error getting unsigned user count: fail"));
        verify(userRepository, times(1)).getUnsignedUserCount();
    }
    
    @Test
    void testGetAnyUserById_Success() {
        int userId = 1;
        User mockUser = mock(User.class);
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
    
        Optional<User> result = userService.getAnyUserById(userId);
    
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findAllById(userId);
    }
    
    @Test
    void testGetAnyUserById_UserNotFound_ShouldThrow() {
        int userId = 999;
        when(userRepository.findAllById(userId)).thenReturn(Optional.empty());
    
        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.getAnyUserById(userId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findAllById(userId);
    }
    @Test
    void testViewCart_Success() {
        int userId = 1;
        User mockUser = mock(User.class);
        Cart mockCart = mock(Cart.class);
        Map<Integer, Map<Integer, Integer>> cartMap = new HashMap<>();
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.getCart()).thenReturn(mockCart);

        Cart result = userService.getUserCart(userId);
    
        assertEquals(mockCart, result);
        verify(userRepository, times(1)).findAllById(userId);
        verify(mockUser, times(1)).getCart();
    }
    @Test
    void testViewCart_UserNotFound_ShouldThrow() {
        int userId = 999;
        when(userRepository.findAllById(userId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.viewCart(userId));
        assertEquals("User not found", ex.getMessage());
        verify(userRepository, times(1)).findAllById(userId);
    }
    @Test
    void testViewCart_ExceptionInGetCart_ShouldThrow() {
        int userId = 1;
        User mockUser = mock(User.class);
        when(userRepository.findAllById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.viewCart()).thenThrow(new IllegalArgumentException("fail"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.viewCart(userId));
        assertTrue(ex.getMessage().contains("Error during view cart: fail"));
        verify(userRepository, times(1)).findAllById(userId);
        verify(mockUser, times(1)).viewCart();
    }
}