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
    public int pay(String cardNumber, String cardHolder, String expDate, String cvv, double amount, int userId) {
        logger.info("Attempting payment for " + cardHolder + ", amount: " + amount);
        if (cardNumber == null || cardHolder == null || expDate == null || cvv == null) {
            logger.error("Payment failed due to missing information for " + cardHolder);
            return -1;
        }
        int transactionId = externalSystem.processPayment(cardNumber, cardHolder, expDate, cvv, amount, userId);
        if (transactionId != -1) {
            logger.info("Payment succeeded for " + cardHolder);
            return transactionId;
        } else {
            logger.error("Payment failed for " + cardHolder);
            return -1;
        }
    }

    @Override
    public int refund(int transactionId) {
        logger.info("Attempting refund for payment with the transactionID: " + transactionId);
        int result = externalSystem.processRefund(transactionId);
        if (result==1) {
            logger.info("Refund succeeded for payment with the transaction ID: " + transactionId);
            return 1;
        } else {
            logger.error("Refund failed for payment with the transaction ID: " + transactionId);
            return -1;
        }
    }
}


