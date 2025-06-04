package UnitTesting;

import InfrastructureLayer.Adapters.ExternalPaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalPaymentSystemTest {

    private ExternalPaymentSystem paymentSystem;

    @BeforeEach
    void setUp() {
        paymentSystem = new ExternalPaymentSystem();
    }

    @Test
    void processPayment_ShouldReturnTrue_ForValidInput() {
        // Arrange
        String cardNumber = "4111111111111111";
        String cardHolder = "John Doe";
        String expDate = "12/30";
        String cvv = "123";
        double amount = 150.0;

        // Act
        boolean result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);

        // Assert
        assertTrue(result);
    }

}
