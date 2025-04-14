package DomainLayer.IRepository;
import DomainLayer.Model.Store;
import java.util.Collection;

public interface IStoreRepository {
    Store findById(String storeID);
    Collection<Store> getAllStores();
    void addStore(Store store);
    void delete(String storeID);
}
