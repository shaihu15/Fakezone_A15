package DomainLayer.Model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;

public class Order implements IOrder{
    private final int orderId;
    private final int userId;
    private final int storeId;
    private final double totalPrice;
    private List<OrderedProduct> products;
    private OrderState orderState;
    private String address;
    private PaymentMethod paymentMethod;
    private static AtomicInteger idCounter = new AtomicInteger(0);
    private int paymentTransactionId;
    private int deliveryTransactionId;

    public Order(int userId,int storeId, OrderState orderState, List<OrderedProduct> products, String address, PaymentMethod paymentMethod, double totalPrice, int paymentTransactionId, int deliveryTransactionId) {
        this.orderId = idCounter.incrementAndGet();
        this.storeId = storeId;
        this.userId = userId;
        this.orderState = orderState;
        this.products = products;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.paymentTransactionId = paymentTransactionId;
        this.deliveryTransactionId = deliveryTransactionId;
    }
    
    public Order(int id, int userId,int storeId, OrderState orderState, List<OrderedProduct> products, String address, PaymentMethod paymentMethod, double totalPrice, int paymentTransactionId, int deliveryTransactionId) {
        this.orderId = id;
        this.userId = userId;
        this.storeId = storeId;
        this.orderState = orderState;
        this.products = products;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.paymentTransactionId = paymentTransactionId;
        this.deliveryTransactionId = deliveryTransactionId;
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
    public List<OrderedProduct> getProducts() {
        return products;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    @Override
    public Collection<Integer> getProductIds() {
        return products.stream()
                .map(OrderedProduct::getProductId)
                .toList();
    }
    @Override
    public double getTotalPrice() {
        return totalPrice;
    }
    @Override
    public int getPaymentTransactionId() {
        return paymentTransactionId;
    }
    @Override
    public int getDeliveryTransactionId() {
        return deliveryTransactionId;
    }



}
