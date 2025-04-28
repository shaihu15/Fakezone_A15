package DomainLayer.Model.helpers;

public class ResponseFromStoreEvent {
    private final int storeId;
    private final int userId;
    private final String message;

    public ResponseFromStoreEvent(int storeId, int userId, String message){
        this.storeId = storeId;
        this.userId = userId;
        this.message = message;
    }

    public int getStoreId(){
        return this.storeId;
    }
    public int getUserId(){
        return this.userId;
    }
    public String getMessage(){
        return this.message;
    }
}
