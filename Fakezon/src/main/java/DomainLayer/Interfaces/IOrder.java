package DomainLayer.Interfaces;

import java.util.Collection;
import java.util.List;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Basket;
import DomainLayer.Model.OrderedProduct;

public interface IOrder {
    int getId(); 
    int getUserId();  
    int getStoreId(); 
    OrderState getState();
    String getAddress();
    void setAddress(String address);
    void setState(OrderState orderState);
    Collection<Integer> getProductIds();
    void setPaymentMethod(PaymentMethod paymentMethod);
    PaymentMethod getPaymentMethod();
    List<OrderedProduct> getProducts();
    double getTotalPrice();
    int getPaymentTransactionId();
    int getDeliveryTransactionId();
}
