package DomainLayer.Model.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_messages")
public class StoreMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("msgId")
    int msgId;
    
    @JsonProperty("userId")
    int userId;

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
    public StoreMsg(int storeId, int productId, String msg, Integer offeredBy, int userId) {
        this.storeId = storeId;
        this.productId = productId;
        this.msg = msg;
        this.offeredBy = offeredBy;
        this.userId = userId;
    }
    
    // Default constructor for JPA
    public StoreMsg() {
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
    public int getMsgId() {
        return msgId;
    }
    
    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Integer getOfferedBy(){
        return offeredBy;
    }

    public void setCounterOffer(){
        isCounterOffer = true;
    }

    public boolean isCounterOffer(){
        return isCounterOffer;
    }
}
