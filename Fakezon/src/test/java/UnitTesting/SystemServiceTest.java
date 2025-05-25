package UnitTesting;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;
import java.util.*;
import ApplicationLayer.Services.*;
import ApplicationLayer.Interfaces.*;
import org.springframework.context.ApplicationEventPublisher;
 

class SystemServiceTest {
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private IOrderService orderService;
    private ApplicationEventPublisher publisher;
    private INotificationWebSocketHandler notificationWebSocketHandler;
    private SystemService systemService;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);
        storeService = mock(IStoreService.class);
        productService = mock(IProductService.class);
        orderService = mock(IOrderService.class);
        publisher = mock(ApplicationEventPublisher.class);
        notificationWebSocketHandler = mock(INotificationWebSocketHandler.class);
        systemService = new SystemService(storeService, userService, productService, orderService, null, null, null, publisher, notificationWebSocketHandler);
    }

    @Test
    void testGetAllMessages_Success() {
        int userId = 1;
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        HashMap<Integer, String> messages = new HashMap<>();
        messages.put(1, "msg");
        Response<HashMap<Integer, String>> expected = new Response<>(messages, "ok", true, null, null);
        when(userService.getAllMessages(userId)).thenReturn(expected);

        Response<HashMap<Integer, String>> response = systemService.getAllMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
    }

    @Test
    void testGetAllMessages_NotLoggedIn() {
        int userId = 2;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<HashMap<Integer, String>> response = systemService.getAllMessages(userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAllMessages_Exception() {
        int userId = 3;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<HashMap<Integer, String>> response = systemService.getAllMessages(userId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during getting all messages: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetAssignmentMessages_Success() {
        int userId = 1;
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        HashMap<Integer, String> messages = new HashMap<>();
        messages.put(1, "assignment");
        Response<HashMap<Integer, String>> expected = new Response<>(messages, "ok", true, null, null);
        when(userService.getAssignmentMessages(userId)).thenReturn(expected);

        Response<HashMap<Integer, String>> response = systemService.getAssignmentMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
    }

    @Test
    void testGetAssignmentMessages_NotLoggedIn() {
        int userId = 2;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<HashMap<Integer, String>> response = systemService.getAssignmentMessages(userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAssignmentMessages_Exception() {
        int userId = 3;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<HashMap<Integer, String>> response = systemService.getAssignmentMessages(userId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during getting all messages: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetAuctionEndedMessages_Success() {
        int userId = 1;
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        HashMap<Integer, String> messages = new HashMap<>();
        messages.put(1, "auction ended");
        Response<HashMap<Integer, String>> expected = new Response<>(messages, "ok", true, null, null);
        when(userService.getAuctionEndedMessages(userId)).thenReturn(expected);

        Response<HashMap<Integer, String>> response = systemService.getAuctionEndedMessages(userId);

        assertTrue(response.isSuccess());
        assertEquals(messages, response.getData());
    }

    @Test
    void testGetAuctionEndedMessages_NotLoggedIn() {
        int userId = 2;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<HashMap<Integer, String>> response = systemService.getAuctionEndedMessages(userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAuctionEndedMessages_Exception() {
        int userId = 3;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<HashMap<Integer, String>> response = systemService.getAuctionEndedMessages(userId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during getting all messages: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
}