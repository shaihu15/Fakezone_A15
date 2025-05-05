package InfrastructureLayer.Repositories;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;


public class StoreRepository implements IStoreRepository {
    private Map<Integer, Store> stores;
    
    public StoreRepository(Map<Integer, Store> stores) {
        this.stores = stores;
    }
    public StoreRepository() {
        this.stores = new HashMap<>();
    }
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
    public Store findByName(String storeName) {
        for (Store store : stores.values()) {
            if (store.getName().equals(storeName)) {
                return store;
            }
        }
        return null; // Store not found
    }

    @Override
    public Collection<Store> getTop10Stores() {
       return stores.values().stream().sorted((s1, s2) -> Double.compare(s2.getAverageRating(), s1.getAverageRating())).limit(10).collect(Collectors.toList());
    }

}
