package DomainLayer.Model.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserMsg {
    @JsonProperty("userId")
    int userId;
    @JsonProperty("msg")
    String msg;
    
    @JsonCreator
    public UserMsg(int userId, String msg) {
        this.userId = userId;
        this.msg = msg;
    }
    public int getUserId() {
        return userId;
    }
    public String getMsg() {
        return msg;
    }
}
