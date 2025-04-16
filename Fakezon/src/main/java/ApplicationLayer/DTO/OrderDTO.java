package ApplicationLayer.DTO;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;



public class OrderDTO {

    @JsonProperty("orderId")
    private final int orderId;

    @JsonProperty("userName")
    private final String userName;

    @JsonProperty("storeName")
    private final String storeName;

    @JsonProperty("products")
    private final Collection<ProductDTO> products;

    @JsonProperty("orderState")
    private final String orderState;

    @JsonProperty("address")
    private final String address;

    @JsonProperty("paymentMethod")
    private final String paymentMethod;

    public OrderDTO(int orderId, String userName, String storeName, Collection<ProductDTO> products, String orderState, String address, String paymentMethod) {
        this.orderId = orderId;
        this.userName = userName;
        this.storeName = storeName;
        this.products = products;
        this.orderState = orderState;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }
    public int getOrderId() {
        return orderId;
    }
    public String getUserName() {
        return userName;
    }
    public String getStoreName() {
        return storeName;
    }
    public Collection<ProductDTO> getProducts() {
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
    
    
    
}
