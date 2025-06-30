package NewAcceptanceTesting.AT_User.AT_SystemAdministrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessingTest {
    @Mock
    private SystemService systemService;

    private int userId = 1;
    private int storeId = 101;
    private int productId = 1001;
    private String validBirthDate = "1990-01-01";

    @BeforeEach
    void setUp() {
        // Initialize the systemService mock and any other necessary setup
        systemService.clearAllData();

        // Mocks are handled by MockitoExtension
    }

    @Test
    void testSuccessfulPayment() {
        // Arrange
        when(systemService.addToBasket(userId, productId, storeId, 1)).thenReturn(new Response<>(null, "", true, null, null));
        when(systemService.purchaseCart(
            anyInt(),
            anyString(),
            any(LocalDate.class),
            any(PaymentMethod.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(new Response<>("Cart purchased successfully", "Cart purchased successfully", true, null, null));

        // Act
        Response<Void> addToCartResponse = systemService.addToBasket(userId, productId, storeId, 1);
        assertTrue(addToCartResponse.isSuccess());

        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(validBirthDate),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "1234567812345678",
            "John Doe",
            "12/25",
            "123",
            "123 Main St*New York*USA*12345",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertTrue(paymentResponse.isSuccess());
        assertEquals("Cart purchased successfully", paymentResponse.getMessage());
    }

    @Test
    void testInvalidPaymentDetails() {
        // Arrange
        when(systemService.addToBasket(userId, productId, storeId, 1)).thenReturn(new Response<>(null, "", true, null, null));
        when(systemService.purchaseCart(
            anyInt(),
            anyString(),
            any(LocalDate.class),
            any(PaymentMethod.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(new Response<>(null, "Invalid payment details", false, ApplicationLayer.Enums.ErrorType.BAD_REQUEST, null));

        // Act
        Response<Void> addToCartResponse = systemService.addToBasket(userId, productId, storeId, 1);
        assertTrue(addToCartResponse.isSuccess());

        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(validBirthDate),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "INVALID_CARD",
            "John Doe",
            "12/25",
            "123",
            "123 Main St",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertFalse(paymentResponse.isSuccess());
        assertEquals("Invalid payment details", paymentResponse.getMessage());
    }

    @Test
    void testOrderNotInStock() {
        // Arrange
        int nonExistentProductId = 999;
        when(systemService.addToBasket(userId, nonExistentProductId, storeId, 1)).thenReturn(new Response<>(null, "Product not in stock", false, ApplicationLayer.Enums.ErrorType.INVALID_INPUT, null));
        when(systemService.purchaseCart(
            anyInt(),
            anyString(),
            any(LocalDate.class),
            any(PaymentMethod.class),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(new Response<>(null, "Cart is empty", false, ApplicationLayer.Enums.ErrorType.INVALID_INPUT, null));
        
        // Act
        Response<Void> addToCartResponse = systemService.addToBasket(userId, nonExistentProductId, storeId, 1);
        assertFalse(addToCartResponse.isSuccess());

        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(validBirthDate),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "1234567812345678",
            "John Doe",
            "12/25",
            "123",
            "123 Main St",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertFalse(paymentResponse.isSuccess());
        assertEquals("Cart is empty", paymentResponse.getMessage());
    }
}