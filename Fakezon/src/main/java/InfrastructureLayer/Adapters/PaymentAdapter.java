package InfrastructureLayer.Adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.ExternalPaymentSystem;

@Component

public class PaymentAdapter implements IPayment {
    private final ExternalPaymentSystem externalSystem;
    private final Logger logger = LoggerFactory.getLogger(PaymentAdapter.class);

    public PaymentAdapter() {
        this.externalSystem = new ExternalPaymentSystem();
    }
    public PaymentAdapter(ExternalPaymentSystem externalSystem) {
        this.externalSystem = externalSystem;
    }
    @Override
    public boolean pay(String cardNumber, String cardHolder, String expDate, String cvv, double amount) {
        logger.info("Attempting payment for " + cardHolder + ", amount: " + amount);
        if (cardNumber == null || cardHolder == null || expDate == null || cvv == null) {
            logger.error("Payment failed due to missing information for " + cardHolder);
            return false;
        }
        boolean result = externalSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount);
        if (result) {
            logger.info("Payment succeeded for " + cardHolder);
            return true;
        } else {
            logger.error("Payment failed for " + cardHolder);
            return false;
        }
    }

    @Override
    public boolean refund(String cardNumber, double amount) {
        logger.info("Attempting refund for payment cardNumber: " + cardNumber + ", amount: " + amount);
        boolean result = externalSystem.processRefund(cardNumber, amount);
        if (result) {
            logger.info("Refund succeeded for payment ID: " + cardNumber);
            return true;
        } else {
            logger.error("Refund failed for payment ID: " + cardNumber);
            return false;
        }
    }
}


