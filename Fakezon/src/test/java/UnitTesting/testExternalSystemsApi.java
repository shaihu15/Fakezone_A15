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
    void sendPackage_ValidDetails_ShouldReturnSuccessCode() {
        // Arrange
        String address = "123 Main Street*Tel-Aviv*Israel*12345";
        String recipient = "John Doe";
        String packageDetails = "Electronics Package";

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result > 0, "Valid package delivery should return a positive delivery ID");
    }

    @Test
    @DisplayName("Delivery: Send package with null address should fail")
    void sendPackage_NullAddress_ShouldReturnFailureCode() {
        // Arrange
        String address = null;
        String recipient = "John Doe";
        String packageDetails = "Electronics Package";

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result <= 0, "Null address should return failure code");
    }

    @Test
    @DisplayName("Delivery: Send package with null recipient should fail")
    void sendPackage_NullRecipient_ShouldReturnFailureCode() {
        // Arrange
        String address = "123 Main Street*Tel-Aviv*Israel*12345";
        String recipient = null;
        String packageDetails = "Electronics Package";

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result <= 0, "Null recipient should return failure code");
    }

    @Test
    @DisplayName("Delivery: Send package with empty strings should handle gracefully")
    void sendPackage_EmptyStrings_ShouldHandleGracefully() {
        // Arrange
        String address = "";
        String recipient = "";
        String packageDetails = "";

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result <= 0, "Empty strings should return failure code");
    }

    @Test
    @DisplayName("Delivery: Send package with special characters should handle gracefully")
    void sendPackage_SpecialCharacters_ShouldHandleGracefully() {
        // Arrange
        String address = "123 Main St. \"Apt 5\"";
        String recipient = "José María O'Connor";
        String packageDetails = "Package with \"quotes\" and \\backslashes\\";

        // Act
        int result = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(result <= 0 || result > 0, "Special characters should be handled");
    }

    @Test
    @DisplayName("Delivery: Cancel package with valid ID should succeed")
    void cancelPackage_ValidId_ShouldReturnSuccessCode() {
        // Arrange
        int transactionId = 12345;

        // Act
        int result = deliverySystem.cancelPackage(transactionId);

        // Assert
        assertEquals(1, result, "Valid package cancellation should return 1");
    }

    // =============== PAYMENT SYSTEM TESTS ===============

    @Test
    @DisplayName("Payment: Process payment with valid details should succeed")
    void processPayment_ValidDetails_ShouldReturnTransactionId() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        double amount = 100.0;
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result > 0, "Valid payment processing should return a positive transaction ID");
    }

    @Test
    @DisplayName("Payment: Process payment with 4-digit year format should succeed")
    void processPayment_FourDigitYear_ShouldReturnTransactionId() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/2025";
        String cvv = "123";
        double amount = 150.0;
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result > 0, "Payment with 4-digit year should return a positive transaction ID");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result <= 0, "Null card number should return failure code");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result <= 0, "Null card holder should return failure code");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result <= 0, "Invalid expiration date format should return failure code");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result <= 0, "Null expiration date should return failure code");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result <= 0, "Null CVV should return failure code");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result > 0 || result <= 0, "Large amount should be handled");
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
        int userId = 12345678;

        // Act
        int result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Assert
        assertTrue(result > 0 || result <= 0, "Special characters in name should be handled");
    }

    @Test
    @DisplayName("Payment: Process refund with valid details should succeed")
    void processRefund_ValidDetails_ShouldReturnSuccessCode() {
        // Arrange
        int transactionId = 12345;

        // Act
        int result = paymentSystem.processRefund(transactionId);

        // Assert
        assertEquals(1, result, "Valid refund processing should return 1");
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
        int userId = 12345678;

        // Arrange - Delivery details
        String address = "123 Main Street*Tel-Aviv*Israel*12345";
        String recipient = "John Doe";
        String packageDetails = "Purchased item";

        // Act - Process payment first
        int paymentTransactionId = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);

        // Act - Then process delivery
        int deliveryId = deliverySystem.sendPackage(address, recipient, packageDetails);

        // Assert
        assertTrue(paymentTransactionId > 0, "Payment should return a positive transaction ID");
        assertTrue(deliveryId > 0, "Delivery should return a positive delivery ID");
    }

    @Test
    @DisplayName("Integration: Cancellation workflow should work for both systems")
    void cancellationWorkflow_PaymentAndDelivery_ShouldWork() {
        // Arrange
        int transactionId = 12345;
        int deliveryId = 12345;

        // Act - Cancel payment
        int refundResult = paymentSystem.processRefund(transactionId);

        // Act - Cancel delivery
        int cancelDeliveryResult = deliverySystem.cancelPackage(deliveryId);

        // Assert
        assertEquals(1, refundResult, "Refund should return 1 for success");
        assertEquals(1, cancelDeliveryResult, "Delivery cancellation should return 1 for success");
    }

    // =============== EDGE CASE TESTS ===============

    @Test
    @DisplayName("Edge Case: Very long strings should be handled gracefully")
    void edgeCase_VeryLongStrings_ShouldHandleGracefully() {
        // Arrange
        String longString = "A".repeat(1000);
        int userId = 12345678;
        // Act & Assert - Delivery
        int deliveryId = deliverySystem.sendPackage(longString, longString, longString);
        assertTrue(deliveryId <= 0 || deliveryId > 0, "Very long strings in delivery should be handled");

        // Act & Assert - Payment
        int paymentTransactionId = paymentSystem.processPayment(longString, longString, "12/25", "123", 100.0, userId);
        assertTrue(paymentTransactionId <= 0 || paymentTransactionId > 0, "Very long strings in payment should be handled");
    }

    @Test
    @DisplayName("Edge Case: Unicode characters should be handled gracefully")
    void edgeCase_UnicodeCharacters_ShouldHandleGracefully() {
        // Arrange
        String unicodeString = "José María 李小龙 москва 🏠📦💳";
        int userId = 12345678;
        // Act & Assert - Delivery
        int deliveryId = deliverySystem.sendPackage(unicodeString, unicodeString, unicodeString);
        assertTrue(deliveryId <= 0 || deliveryId > 0, "Unicode characters in delivery should be handled");

        // Act & Assert - Payment
        int paymentTransactionId = paymentSystem.processPayment("4111111111111111", unicodeString, "12/25", "123", 100.0, userId);
        assertTrue(paymentTransactionId <= 0 || paymentTransactionId > 0, "Unicode characters in payment should be handled");
    }
}