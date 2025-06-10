package UnitTesting;
import InfrastructureLayer.Adapters.ExternalPaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class ExternalPaymentSystemTest {
    private ExternalPaymentSystem paymentSystem;
    private int result =-1; // Initialize result to -1
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
        result = paymentSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, 123456789);
        // Assert
        assertTrue(result!= -1, "Payment should be processed successfully and return a valid transaction ID");
        System.out.println("Payment processed successfully with transaction ID: " + result);
    }

    @Test
    void processRefund_ShouldReturnTrue_ForValidInput() {
        // Arrange
        String cardNumber = "4111111111111111";
        double amount = 75.0;

        // Act
        int result = paymentSystem.processRefund(this.result);

        // Assert
        assertTrue(result==1, "Refund should be processed successfully and return 1");
    }
}