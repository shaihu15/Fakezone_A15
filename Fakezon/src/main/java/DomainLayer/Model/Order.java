package DomainLayer.Model;

import java.util.Collection;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;

public class Order {
    private final int orderId;
    private final int userId;
    private final int storeId;
    private final Collection<Integer> productIds;
    private OrderState orderState;
    private String address;
    private PaymentMethod paymentMethod;

    public Order(int orderId, int userId, OrderState orderState, Collection<Integer> productIds, int storeId, String address, PaymentMethod paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderState = orderState;
        this.productIds = productIds;
        this.storeId = storeId;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    public int getId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public int getStoreId() {
        return storeId;
    }

    public OrderState getState() {
        return orderState;
    }

    public void setState(OrderState orderState) {
        this.orderState = orderState;
    }

    public Collection<Integer> getProductIds() {
        return productIds;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        if(this.paymentMethod != PaymentMethod.CASH_ON_DELIVERY) {
            throw new IllegalArgumentException("Payment method can only be changed if it is CASH_ON_DELIVERY.");
        }
        this.paymentMethod = paymentMethod;
    }



}
