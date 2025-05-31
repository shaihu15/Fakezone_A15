package DomainLayer.Model.helpers;

public class UserMsg {
    int userId;
    String msg;

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
