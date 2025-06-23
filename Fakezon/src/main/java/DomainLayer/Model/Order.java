package DomainLayer.Model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Objects;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order implements IOrder{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;
    
    @Column(name = "user_id", nullable = false)
    private int userId;
    
    @Column(name = "store_id", nullable = false)
    private int storeId;
    
    @Column(name = "total_price", nullable = false)
    private double totalPrice;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderedProduct> products;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    private OrderState orderState;
    
    @Column(name = "address", nullable = false)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_transaction_id")
    private int paymentTransactionId;
    
    @Column(name = "delivery_transaction_id")
    private int deliveryTransactionId;

    // Default constructor for JPA
    protected Order() {
        // JPA will populate fields
    }

    public Order(int userId,int storeId, OrderState orderState, List<OrderedProduct> products, String address, PaymentMethod paymentMethod, double totalPrice, int paymentTransactionId, int deliveryTransactionId) {
        this.storeId = storeId;
        this.userId = userId;
        this.orderState = orderState;
        this.products = products;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.paymentTransactionId = paymentTransactionId;
        this.deliveryTransactionId = deliveryTransactionId;
        // Set up bidirectional relationship
        if (products != null) {
            for (OrderedProduct product : products) {
                product.setOrder(this);
            }
        }
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
        // Set up bidirectional relationship
        if (products != null) {
            for (OrderedProduct product : products) {
                product.setOrder(this);
            }
        }
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Order order = (Order) obj;
        return orderId == order.orderId &&
               userId == order.userId &&
               storeId == order.storeId &&
               Double.compare(order.totalPrice, totalPrice) == 0 &&
               paymentTransactionId == order.paymentTransactionId &&
               deliveryTransactionId == order.deliveryTransactionId &&
               orderState == order.orderState &&
               Objects.equals(address, order.address) &&
               paymentMethod == order.paymentMethod &&
               Objects.equals(products, order.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userId, storeId, totalPrice, orderState, address, 
                          paymentMethod, paymentTransactionId, deliveryTransactionId, products);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", storeId=" + storeId +
                ", totalPrice=" + totalPrice +
                ", orderState=" + orderState +
                ", address='" + address + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", paymentTransactionId=" + paymentTransactionId +
                ", deliveryTransactionId=" + deliveryTransactionId +
                ", products=" + products +
                '}';
    }

}
