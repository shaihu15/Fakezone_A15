package DomainLayer.Model.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StoreMsg {
    @JsonProperty("storeId")
    int storeId;
    @JsonProperty("productId")
    int productId;
    @JsonProperty("msg")
    String msg;
    @JsonCreator
    public StoreMsg(int storeId, int productId, String msg) {
        this.storeId = storeId;
        this.productId = productId;
        this.msg = msg;
    }
    public int getStoreId() {
        return storeId;
    }
    public int getProductId() {
        return productId;
    }
    public String getMessage() {
        return msg;
    }
}
