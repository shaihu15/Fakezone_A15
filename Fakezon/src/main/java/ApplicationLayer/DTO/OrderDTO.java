package ApplicationLayer.DTO;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;



public class OrderDTO {

    @JsonProperty("orderId")
    private final int orderId;

    @JsonProperty("userId")
    private final int userId;

    @JsonProperty("storeId")
    private final int storeId;

    @JsonProperty("products")
    private final Collection<ProductDTO> products;

    @JsonProperty("orderState")
    private final String orderState;

    @JsonProperty("address")
    private final String address;

    @JsonProperty("paymentMethod")
    private final String paymentMethod;

    public OrderDTO(int orderId, int userId, int storeId, Collection<ProductDTO> products, String orderState, String address, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.storeId = storeId;
        this.products = products;
        this.orderState = orderState;
        this.address = address;
        this.paymentMethod = paymentMethod;
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
