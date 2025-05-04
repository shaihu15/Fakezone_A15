package ApplicationLayer.RequestDataTypes;

import ApplicationLayer.DTO.BasketDTO;

import java.util.Collection;

public class RequestOrderDataType {
    Integer orderId;
    Integer userId;
    String address;
    String paymentMethod;
    BasketDTO basket;

    public RequestOrderDataType(Integer orderId, Integer userId, String address, String paymentMethod, BasketDTO basket) {
        this.orderId = orderId;
        this.userId = userId;
        this.basket = basket;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    public Integer getOrderId() {
        return orderId;
    }
    public Integer getUserId() {
        return userId;
    }
    public String getAddress() {
        return address;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public BasketDTO getBasket() {
        return basket;
    }
}
