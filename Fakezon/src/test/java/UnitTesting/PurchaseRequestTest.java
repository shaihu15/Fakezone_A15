package UnitTesting;

import DomainLayer.Enums.PaymentMethod;
import ApplicationLayer.RequestDataTypes.PurchaseRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseRequestTest {

    @Test
    void testConstructorAndGetters() {
        int userId = 1;
        String country = "Israel";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        String deliveryMethod = "Express";
        String cardNumber = "1234567890123456";
        String cardHolder = "John Doe";
        String expDate = "12/25";
        String cvv = "123";
        String address = "123 Main St";
        String recipient = "Jane Doe";
        String packageDetails = "Box";

        PurchaseRequest request = new PurchaseRequest(
                userId, country, dob, paymentMethod, deliveryMethod,
                cardNumber, cardHolder, expDate, cvv, address, recipient, packageDetails
        );

        assertEquals(userId, request.getUserId());
        assertEquals(country, request.getCountry());
        assertEquals(dob, request.getDob());
        assertEquals(paymentMethod, request.getPaymentMethod());
        assertEquals(deliveryMethod, request.getDeliveryMethod());
        assertEquals(cardNumber, request.getCardNumber());
        assertEquals(cardHolder, request.getCardHolder());
        assertEquals(expDate, request.getExpDate());
        assertEquals(cvv, request.getCvv());
        assertEquals(address, request.getAddress());
        assertEquals(recipient, request.getRecipient());
        assertEquals(packageDetails, request.getPackageDetails());
    }

    @Test
    void testSetters() {
        PurchaseRequest request = new PurchaseRequest(
                0, null, null, null, null,
                null, null, null, null, null, null, null
        );

        request.setUserId(2);
        assertEquals(2, request.getUserId());

        request.setCountry("USA");
        assertEquals("USA", request.getCountry());

        LocalDate newDob = LocalDate.of(1990, 5, 15);
        request.setDob(newDob);
        assertEquals(newDob, request.getDob());

        request.setPaymentMethod(PaymentMethod.PAYPAL);
        assertEquals(PaymentMethod.PAYPAL, request.getPaymentMethod());

        request.setDeliveryMethod("Standard");
        assertEquals("Standard", request.getDeliveryMethod());

        request.setCardNumber("6543210987654321");
        assertEquals("6543210987654321", request.getCardNumber());

        request.setCardHolder("Alice Smith");
        assertEquals("Alice Smith", request.getCardHolder());

        request.setExpDate("11/30");
        assertEquals("11/30", request.getExpDate());

        request.setCvv("999");
        assertEquals("999", request.getCvv());

        request.setAddress("456 Elm St");
        assertEquals("456 Elm St", request.getAddress());

        request.setRecipient("Bob Smith");
        assertEquals("Bob Smith", request.getRecipient());

        request.setPackageDetails("Envelope");
        assertEquals("Envelope", request.getPackageDetails());
    }
}