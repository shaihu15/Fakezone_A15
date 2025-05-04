package DomainLayer.Interfaces;

import java.util.Collection;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Basket;

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
    Basket getBasket();
}
