package DomainLayer.Model.helpers;

public class StoreMsg {
    int storeId;
    int productId;
    String msg;
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
