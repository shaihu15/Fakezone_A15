package ApplicationLayer.DTO;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;



public class OrderDTO {

    @JsonProperty("orderId")
    private final int orderId;

    @JsonProperty("userId")
    private final int userId;

    @JsonProperty("storeId")
    private final int storeId;

    @JsonProperty("products")
    private final Collection<OrderedProductDTO> products;

    @JsonProperty("orderState")
    private final String orderState;

    @JsonProperty("address")
    private final String address;

    @JsonProperty("paymentMethod")
    private final String paymentMethod;

    @JsonProperty("totalPrice")
    private final double totalPrice;

    @JsonCreator
    public OrderDTO(int orderId, int userId, int storeId, Collection<OrderedProductDTO> products, String orderState, String address, String paymentMethod, double totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.storeId = storeId;
        this.products = products;
        this.orderState = orderState;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }
    public int getOrderId() {
        return orderId;
    }
    public int getUserId() {
        return userId;
    }
    public int getStoreId() {
        return storeId;
    }
    public Collection<OrderedProductDTO> getProducts() {
        return products;
    }

    public String getOrderState() {
        return orderState;
    }
    public String getAddress() {
        return address;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    

    public double getTotalPrice(){
        return totalPrice;
    }
    
    
}
