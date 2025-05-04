package ApplicationLayer.RequestDataTypes;

import java.util.Collection;

public class RequestOrderDataType {
    Integer orderId;
    Integer userId;
    Integer storeId;
    String address;
    String paymentMethod;
    Collection<Integer> productsIds;

    public RequestOrderDataType(Integer orderId, Integer userId, Integer storeId, String address, String paymentMethod, Collection<Integer> productsIds) {
        this.orderId = orderId;
        this.userId = userId;
        this.storeId = storeId;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.productsIds = productsIds;
    }

    public Integer getOrderId() {
        return orderId;
    }
    public Integer getUserId() {
        return userId;
    }
    public Integer getStoreId() {
        return storeId;
    }
    public String getAddress() {
        return address;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public Collection<Integer> getProductsIds() {
        return productsIds;
    }
}
