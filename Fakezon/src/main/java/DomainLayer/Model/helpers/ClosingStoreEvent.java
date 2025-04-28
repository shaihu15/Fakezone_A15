package DomainLayer.Model.helpers;

public class ClosingStoreEvent {
    private final int storeId;

    public ClosingStoreEvent(int storeId){
        this.storeId = storeId;
    }

    public int getId(){
        return this.storeId;
    }

}
