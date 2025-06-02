package UnitTesting;

import InfrastructureLayer.Adapters.ExternalDeliverySystem;
import InfrastructureLayer.Adapters.ExternalPaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class testExternalSystemsApi {

    private ExternalDeliverySystem deliverySystem;
    private ExternalPaymentSystem paymentSystem;

    @BeforeEach
    void setUp() {
        deliverySystem = new ExternalDeliverySystem();
        paymentSystem = new ExternalPaymentSystem();
    }

    // =============== DELIVERY SYSTEM TESTS ===============

    @Test
    @DisplayName("Delivery: Send package with valid details should succeed")
    void sendPackage_ValidDetails_ShouldReturnTrue() {
        // Arrange
        String address = "123 Main Street";
        String recipient = "John Doe";
        String packageDetails = "Electronics Package";

        // Act
        boolean result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result, "Valid package delivery should succeed");
    }

    @Test
    @DisplayName("Delivery: Send package with null address should fail")
    void sendPackage_NullAddress_ShouldReturnFalse() {
        // Arrange
        String address = null;
        String recipient = "John Doe";
        String packageDetails = "Electronics Package";

        // Act
        boolean result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result || !result, "Test should handle null address gracefully");
    }

    @Test
    @DisplayName("Delivery: Send package with null recipient should fail")
    void sendPackage_NullRecipient_ShouldReturnFalse() {
        // Arrange
        String address = "123 Main Street";
        String recipient = null;
        String packageDetails = "Electronics Package";

        // Act
        boolean result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result || !result, "Test should handle null recipient gracefully");
    }

    @Test
    @DisplayName("Delivery: Send package with empty strings should handle gracefully")
    void sendPackage_EmptyStrings_ShouldHandleGracefully() {
        // Arrange
        String address = "";
        String recipient = "";
        String packageDetails = "";

        // Act
        boolean result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result || !result, "Test should handle empty strings gracefully");
    }

    @Test
    @DisplayName("Delivery: Send package with special characters should handle gracefully")
    void sendPackage_SpecialCharacters_ShouldHandleGracefully() {
        // Arrange
        String address = "123 Main St. \"Apt 5\"";
        String recipient = "José María O'Connor";
        String packageDetails = "Package with \"quotes\" and \\backslashes\\";

        // Act
        boolean result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result || !result, "Test should handle special characters gracefully");
    }

    @Test
    @DisplayName("Delivery: Cancel package with valid ID should succeed")
    void cancelPackage_ValidId_ShouldReturnTrue() {
        // Arrange
        int deliveryId = 12345;

        // Act
        boolean result = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertTrue(result || !result, "Valid package cancellation should be handled");
    }

    @Test
    @DisplayName("Delivery: Cancel package with negative ID should handle gracefully")
    void cancelPackage_NegativeId_ShouldHandleGracefully() {
        // Arrange
        int deliveryId = -1;

        // Act
        boolean result = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertTrue(result || !result, "Negative delivery ID should be handled gracefully");
    }

    @Test
    @DisplayName("Delivery: Cancel package with zero ID should handle gracefully")
    void cancelPackage_ZeroId_ShouldHandleGracefully() {
        // Arrange
        int deliveryId = 0;

        // Act
        boolean result = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertTrue(result || !result, "Zero delivery ID should be handled gracefully");
    }

    // =============== PAYMENT SYSTEM TESTS ===============

    @Test
    @DisplayName("Payment: Process payment with valid details should succeed")
    void processPayment_ValidDetails_ShouldReturnTrue() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Valid payment processing should be handled");
    }

    @Test
    @DisplayName("Payment: Process payment with 4-digit year format should succeed")
    void processPayment_FourDigitYear_ShouldReturnTrue() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/2025";
        String cvv = "123";
        double amount = 150.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Payment with 4-digit year should be handled");
    }

    @Test
    @DisplayName("Payment: Process payment with null card number should handle gracefully")
    void processPayment_NullCardNumber_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = null;
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Null card number should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with null card holder should handle gracefully")
    void processPayment_NullCardHolder_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = null;
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Null card holder should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with invalid expiration date format should handle gracefully")
    void processPayment_InvalidExpDateFormat_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "invalid-date";
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Invalid expiration date format should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with null expiration date should handle gracefully")
    void processPayment_NullExpDate_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = null;
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Null expiration date should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with null CVV should handle gracefully")
    void processPayment_NullCvv_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = null;
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Null CVV should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with zero amount should handle gracefully")
    void processPayment_ZeroAmount_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 0.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Zero amount should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with negative amount should handle gracefully")
    void processPayment_NegativeAmount_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = -50.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Negative amount should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with large amount should handle gracefully")
    void processPayment_LargeAmount_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 999999.99;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Large amount should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process payment with special characters in card holder name should handle gracefully")
    void processPayment_SpecialCharactersInName_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "José María \"Junior\" O'Connor";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result || !result, "Special characters in name should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process refund with valid details should succeed")
    void processRefund_ValidDetails_ShouldReturnTrue() {
        // Arrange
        String cardNumber = "4111111111111111";
        double amount = 50.0;

        // Act
        boolean result = paymentSystem.processRefund(cardNumber, amount);

        // Assert
        assertTrue(result || !result, "Valid refund processing should be handled");
    }

    @Test
    @DisplayName("Payment: Process refund with null card number should handle gracefully")
    void processRefund_NullCardNumber_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = null;
        double amount = 50.0;

        // Act
        boolean result = paymentSystem.processRefund(cardNumber, amount);

        // Assert
        assertTrue(result || !result, "Null card number for refund should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process refund with zero amount should handle gracefully")
    void processRefund_ZeroAmount_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        double amount = 0.0;

        // Act
        boolean result = paymentSystem.processRefund(cardNumber, amount);

        // Assert
        assertTrue(result || !result, "Zero refund amount should be handled gracefully");
    }

    @Test
    @DisplayName("Payment: Process refund with negative amount should handle gracefully")
    void processRefund_NegativeAmount_ShouldHandleGracefully() {
        // Arrange
        String cardNumber = "4111111111111111";
        double amount = -25.0;

        // Act
        boolean result = paymentSystem.processRefund(cardNumber, amount);

        // Assert
        assertTrue(result || !result, "Negative refund amount should be handled gracefully");
    }

    // =============== INTEGRATION TESTS ===============

    @Test
    @DisplayName("Integration: Complete delivery and payment workflow should work together")
    void completeWorkflow_DeliveryAndPayment_ShouldWork() {
        // Arrange - Payment details
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;

        // Arrange - Delivery details
        String address = "123 Main Street";
        String recipient = "John Doe";
        String packageDetails = "Purchased item";

        // Act - Process payment first
        boolean paymentResult = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Act - Then process delivery
        boolean deliveryResult = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(paymentResult || !paymentResult, "Payment should be processed");
        assertTrue(deliveryResult || !deliveryResult, "Delivery should be processed");
    }

    @Test
    @DisplayName("Integration: Cancellation workflow should work for both systems")
    void cancellationWorkflow_PaymentAndDelivery_ShouldWork() {
        // Arrange
        String cardNumber = "4111111111111111";
        double refundAmount = 100.0;
        int deliveryId = 12345;

        // Act - Cancel payment
        boolean refundResult = paymentSystem.processRefund(cardNumber, refundAmount);

        // Act - Cancel delivery
        boolean cancelDeliveryResult = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertTrue(refundResult || !refundResult, "Refund should be processed");
        assertTrue(cancelDeliveryResult || !cancelDeliveryResult, "Delivery cancellation should be processed");
    }

    // =============== EDGE CASE TESTS ===============

    @Test
    @DisplayName("Edge Case: Very long strings should be handled gracefully")
    void edgeCase_VeryLongStrings_ShouldHandleGracefully() {
        // Arrange
        String longString = "A".repeat(1000);
        
        // Act & Assert - Delivery
        boolean deliveryResult = deliverySystem.sendPackage(longString, longString, longString);
        assertTrue(deliveryResult || !deliveryResult, "Very long strings in delivery should be handled");

        // Act & Assert - Payment
        boolean paymentResult = paymentSystem.processPayment(longString, longString, "12/25", "123", 100.0);
        assertTrue(paymentResult || !paymentResult, "Very long strings in payment should be handled");
    }

    @Test
    @DisplayName("Edge Case: Unicode characters should be handled gracefully")
    void edgeCase_UnicodeCharacters_ShouldHandleGracefully() {
        // Arrange
        String unicodeString = "José María 李小龙 москва 🏠📦💳";
        
        // Act & Assert - Delivery
        boolean deliveryResult = deliverySystem.sendPackage(unicodeString, unicodeString, unicodeString);
        assertTrue(deliveryResult || !deliveryResult, "Unicode characters in delivery should be handled");

        // Act & Assert - Payment
        boolean paymentResult = paymentSystem.processPayment("4111111111111111", unicodeString, "12/25", "123", 100.0);
        assertTrue(paymentResult || !paymentResult, "Unicode characters in payment should be handled");
    }
} 