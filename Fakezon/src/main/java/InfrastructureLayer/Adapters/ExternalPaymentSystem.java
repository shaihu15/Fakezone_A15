package InfrastructureLayer.Adapters;

public class ExternalPaymentSystem {
    public boolean processPayment(String cardNumber, String cardHolder, String expDate, String cvv, double amount) {
        // Simulate always successful payment
        return true;
    }

    public boolean processRefund(String cardNumber, double amount) {
        // Simulate always successful refund
        return true;
    }
}