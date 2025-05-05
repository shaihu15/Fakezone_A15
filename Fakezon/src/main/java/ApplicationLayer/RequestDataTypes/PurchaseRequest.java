package ApplicationLayer.RequestDataTypes;

import java.time.LocalDate;
import DomainLayer.Enums.PaymentMethod;

public class PurchaseRequest {
    private int userId;
    private String country;
    private LocalDate dob;
    private PaymentMethod paymentMethod;
    private String deliveryMethod;
    private String cardNumber;
    private String cardHolder;
    private String expDate;
    private String cvv;
    private String address;
    private String recipient;
    private String packageDetails;

    // Constructor
    public PurchaseRequest(int userId, String country, LocalDate dob, PaymentMethod paymentMethod,
                           String deliveryMethod, String cardNumber, String cardHolder,
                           String expDate, String cvv, String address, String recipient,
                           String packageDetails) {
        this.userId = userId;
        this.country = country;
        this.dob = dob;
        this.paymentMethod = paymentMethod;
        this.deliveryMethod = deliveryMethod;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expDate = expDate;
        this.cvv = cvv;
        this.address = address;
        this.recipient = recipient;
        this.packageDetails = packageDetails;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(String packageDetails) {
        this.packageDetails = packageDetails;
    }
}