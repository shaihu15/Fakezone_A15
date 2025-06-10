package DomainLayer.Interfaces;

public interface IPayment {
    int pay(String cardNumber, String cardHolder, String expDate, String cvv, double amount, int userId);

    int refund(int transactionId);
}
