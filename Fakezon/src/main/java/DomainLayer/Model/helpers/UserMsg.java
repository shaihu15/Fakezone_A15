package DomainLayer.Model.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "user_messages_to_store")
public class UserMsg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("msgId")
    private int msgId;
    
    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private int userId;
    
    @JsonProperty("msg")
    @Column(name = "message", nullable = false, length = 1000)
    private String msg;
    
    @JsonCreator
    public UserMsg(int userId, String msg) {
        this.userId = userId;
        this.msg = msg;
    }
    
    // Default constructor for JPA
    public UserMsg() {
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
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
