package DomainLayer.Model;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;

public class Order implements IOrder{
    private final int orderId;
    private final int userId;
    private Basket basket;
    private OrderState orderState;
    private String address;
    private PaymentMethod paymentMethod;
    private static AtomicInteger idCounter = new AtomicInteger(0);

    public Order(int userId, OrderState orderState, Basket basket, String address, PaymentMethod paymentMethod) {
        this.orderId = idCounter.incrementAndGet();
        this.userId = userId;
        this.orderState = orderState;
        this.basket = basket;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }
    
    public Order(int id, int userId, OrderState orderState, Basket basket, String address, PaymentMethod paymentMethod) {
        this.orderId = id;
        this.userId = userId;
        this.orderState = orderState;
        this.basket = basket;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public int getId() {
        return orderId;
    }

    @Override
    public int getUserId() {
        return userId;
    }


    @Override
    public OrderState getState() {
        return orderState;
    }

    @Override
    public void setState(OrderState orderState) {
        this.orderState = orderState;
    }


    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        if(this.paymentMethod != PaymentMethod.CASH_ON_DELIVERY) {
            throw new IllegalArgumentException("Payment method can only be changed if it is CASH_ON_DELIVERY.");
        }
        this.paymentMethod = paymentMethod;
    }

    @Override
    public Basket getBasket() {
        return basket;
    }

    @Override
    public int getStoreId() {
        return basket.getStoreID();
    }

    @Override
    public Collection<Integer> getProductIds() {
        return basket.getProducts().stream().map(product->product.getProductId()).toList();
    }



}
