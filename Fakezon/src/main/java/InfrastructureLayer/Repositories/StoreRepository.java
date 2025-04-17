package InfrastructureLayer.Repositories;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;


public class StoreRepository implements IStoreRepository {
    private Map<Integer, Store> stores = new HashMap<>();
    

    @Override
    public Store findById(int storeID) {
        // Implementation to find a store by its ID
        return stores.get(storeID); // Placeholder return statement
    }

    @Override
    public Collection<Store> getAllStores() {
      return new ArrayList<>(stores.values());
    }

    @Override
    public void addStore(Store store) {
        stores.put(store.getId(), store);
    }


    @Override
    public void delete(int storeID) {
        stores.remove(storeID);
    }

    @Override
    public void addStoreOwner(int storeId, int appointor, int appointee){
        findById(storeId).addStoreOwner(appointor, appointee);
    }

}
