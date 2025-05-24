package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Cart;

public class SystemServiceDiscountPolicyTest {

    private SystemService systemService;
    
    @Mock
    private IUserService mockUserService;
    @Mock
    private IStoreService mockStoreService;
    @Mock
    private IProductService mockProductService;
    @Mock
    private IOrderService mockOrderService;
    @Mock
    private IDelivery mockDeliveryService;
    @Mock
    private IAuthenticator mockAuthenticatorService;
    @Mock
    private IPayment mockPaymentService;
    @Mock
    private ApplicationEventPublisher mockPublisher;
    @Mock
    private INotificationWebSocketHandler mockNotificationWebSocketHandler;

    private final int STORE_ID = 1;
    private final int USER_ID = 100;
    private final int CART_ID = 200;
    private final double VALID_PERCENTAGE = 25.0;
    private final List<Integer> PRODUCT_IDS = Arrays.asList(1, 2, 3);
    private final List<Predicate<Cart>> CONDITIONS = Arrays.asList(cart -> cart.getAllProducts().size() > 0);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
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

        // Default mock behaviors
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(true);
        when(mockUserService.getUserCart(CART_ID)).thenReturn(new Cart());
    }

    // Tests for addSimpleDiscountWithProductsScope
    
    @Test
    void testAddSimpleDiscountWithProductsScope_Success() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Simple discount with products scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, VALID_PERCENTAGE);
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_InvalidPercentageNegative() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, -5.0);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Percentage must be between 0 and 100", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_InvalidPercentageOver100() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, 150.0);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Percentage must be between 0 and 100", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_NullProductIDs() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, null, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Product IDs list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_EmptyProductIDs() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, new ArrayList<>(), VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Product IDs list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithProductsScope_StoreServiceThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Store error")).when(mockStoreService)
            .addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, VALID_PERCENTAGE);

        // Act
        Response<Void> response = systemService.addSimpleDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during adding simple discount with products scope"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    // Tests for addSimpleDiscountWithStoreScope

    @Test
    void testAddSimpleDiscountWithStoreScope_Success() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Simple discount with store scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, VALID_PERCENTAGE);
    }

    @Test
    void testAddSimpleDiscountWithStoreScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithStoreScope(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testAddSimpleDiscountWithStoreScope_InvalidPercentage() {
        // Act
        Response<Void> response = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, -10.0);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Percentage must be between 0 and 100", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addSimpleDiscountWithStoreScope(anyInt(), anyInt(), anyDouble());
    }

    // Tests for addConditionDiscountWithProductsScope

    @Test
    void testAddConditionDiscountWithProductsScope_Success() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Condition discount with products scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addConditionDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddConditionDiscountWithProductsScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithProductsScope_InvalidPercentage() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, 110.0);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Percentage must be between 0 and 100", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithProductsScope_NullProductIDs() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, null, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Product IDs list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithProductsScope_NullConditions() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, null, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Conditions list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithProductsScope_EmptyConditions() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, new ArrayList<>(), VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Conditions list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    // Tests for addConditionDiscountWithStoreScope

    @Test
    void testAddConditionDiscountWithStoreScope_Success() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Condition discount with store scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addConditionDiscountWithStoreScope(STORE_ID, USER_ID, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddConditionDiscountWithStoreScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addConditionDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithStoreScope_InvalidPercentage() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, -1.0);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Percentage must be between 0 and 100", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    @Test
    void testAddConditionDiscountWithStoreScope_NullConditions() {
        // Act
        Response<Void> response = systemService.addConditionDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, null, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Conditions list cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addConditionDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    // Tests for addAndDiscountWithProductsScope

    @Test
    void testAddAndDiscountWithProductsScope_Success() {
        // Act
        Response<Void> response = systemService.addAndDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("AND discount with products scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addAndDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddAndDiscountWithProductsScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addAndDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addAndDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    // Tests for addAndDiscountWithStoreScope

    @Test
    void testAddAndDiscountWithStoreScope_Success() {
        // Act
        Response<Void> response = systemService.addAndDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("AND discount with store scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addAndDiscountWithStoreScope(STORE_ID, USER_ID, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddAndDiscountWithStoreScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addAndDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addAndDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    // Tests for addOrDiscountWithProductsScope

    @Test
    void testAddOrDiscountWithProductsScope_Success() {
        // Act
        Response<Void> response = systemService.addOrDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OR discount with products scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addOrDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddOrDiscountWithProductsScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addOrDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addOrDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    // Tests for addOrDiscountWithStoreScope

    @Test
    void testAddOrDiscountWithStoreScope_Success() {
        // Act
        Response<Void> response = systemService.addOrDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OR discount with store scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addOrDiscountWithStoreScope(STORE_ID, USER_ID, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddOrDiscountWithStoreScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addOrDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addOrDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    // Tests for addXorDiscountWithProductsScope

    @Test
    void testAddXorDiscountWithProductsScope_Success() {
        // Act
        Response<Void> response = systemService.addXorDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("XOR discount with products scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addXorDiscountWithProductsScope(STORE_ID, USER_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddXorDiscountWithProductsScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addXorDiscountWithProductsScope(STORE_ID, USER_ID, CART_ID, PRODUCT_IDS, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addXorDiscountWithProductsScope(anyInt(), anyInt(), anyList(), anyList(), anyDouble());
    }

    // Tests for addXorDiscountWithStoreScope

    @Test
    void testAddXorDiscountWithStoreScope_Success() {
        // Act
        Response<Void> response = systemService.addXorDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("XOR discount with store scope added successfully", response.getMessage());
        verify(mockUserService).isUserLoggedIn(USER_ID);
        verify(mockStoreService).addXorDiscountWithStoreScope(STORE_ID, USER_ID, CONDITIONS, VALID_PERCENTAGE);
    }

    @Test
    void testAddXorDiscountWithStoreScope_UserNotLoggedIn() {
        // Arrange
        when(mockUserService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // Act
        Response<Void> response = systemService.addXorDiscountWithStoreScope(STORE_ID, USER_ID, CART_ID, CONDITIONS, VALID_PERCENTAGE);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
        verify(mockStoreService, never()).addXorDiscountWithStoreScope(anyInt(), anyInt(), anyList(), anyDouble());
    }

    // Additional edge case tests for all methods

    @Test
    void testDiscountMethods_BoundaryPercentageValues() {
        // Test 0% discount
        Response<Void> response0 = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, 0.0);
        assertTrue(response0.isSuccess());

        // Test 100% discount
        Response<Void> response100 = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, 100.0);
        assertTrue(response100.isSuccess());

        verify(mockStoreService, times(2)).addSimpleDiscountWithStoreScope(eq(STORE_ID), eq(USER_ID), anyDouble());
    }

    @Test
    void testDiscountMethods_StoreServiceExceptions() {
        // Test that service layer properly handles and wraps store service exceptions
        doThrow(new IllegalArgumentException("Store not found")).when(mockStoreService)
            .addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, VALID_PERCENTAGE);

        Response<Void> response = systemService.addSimpleDiscountWithStoreScope(STORE_ID, USER_ID, VALID_PERCENTAGE);

        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding simple discount with store scope"));
    }
} 