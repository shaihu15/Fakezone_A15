package DomainLayer.IRepository;
import DomainLayer.Model.Store;
import java.util.Collection;

public interface IStoreRepository {
    Store findById(int storeID);
    Collection<Store> getAllStores();
    void addStore(Store store);
    void delete(int storeID);
    //void addStoreOwner(int storeId, int appointor, int appointee);
}
