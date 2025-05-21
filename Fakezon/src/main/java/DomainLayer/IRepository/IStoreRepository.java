package DomainLayer.IRepository;
import DomainLayer.Model.Store;
import java.util.Collection;

public interface IStoreRepository {
    Store findById(int storeID);
    Store findByName(String storeName);
    Collection<Store> getAllStores();
    void addStore(Store store);
    void delete(int storeID);
    Collection<Store> getTop10Stores();
        void clearAllData();

}
