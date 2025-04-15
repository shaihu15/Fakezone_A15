package DomainLayer.Interfaces;

public interface IPayment {
    boolean pay(String cardNumber, String cardHolder, String expDate, String cvv, double amount);
    boolean refund(int paymentId, double amount);
}
