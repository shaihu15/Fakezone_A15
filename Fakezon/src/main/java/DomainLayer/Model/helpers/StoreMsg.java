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
    @JsonProperty("offeredBy")
    Integer offeredBy;
    @JsonProperty("isCounterOffer")
    boolean isCounterOffer = false;

    @JsonCreator
    public StoreMsg(int storeId, int productId, String msg, Integer offeredBy) {
        this.storeId = storeId;
        this.productId = productId;
        this.msg = msg;
        this.offeredBy = offeredBy;
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
    
    public Integer getOfferedBy(){
        return offeredBy;
    }

    public void setCounterOffer(){
        isCounterOffer = true;
    }
}
