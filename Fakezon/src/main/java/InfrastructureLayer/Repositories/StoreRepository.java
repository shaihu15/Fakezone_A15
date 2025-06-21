package InfrastructureLayer.Repositories;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Model.Store;
import InfrastructureLayer.StoreJpaRepository;

@Repository
public class StoreRepository implements IStoreRepository {

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Override
    public Store findById(int storeID) {
        return storeJpaRepository.findById(storeID).orElse(null);
    }

    @Override
    public Store findByName(String storeName) {
        return storeJpaRepository.findByName(storeName).orElse(null);
    }

    @Override
    public Collection<Store> getAllStores() {
        return storeJpaRepository.findAll();
    }

    @Override
    public void addStore(Store store) {
        storeJpaRepository.save(store);
    }

    @Override
    public void delete(int storeID) {
        if (storeJpaRepository.existsById(storeID)) {
            storeJpaRepository.deleteById(storeID);
        }
    }

    @Override
    public Collection<Store> getTop10Stores() {
        return storeJpaRepository.findTop10StoresByRating();
    }

    @Override
    public void clearAllData() {
        storeJpaRepository.deleteAll();
    }

    // Additional helper methods
    public Collection<Store> getOpenStores() {
        return storeJpaRepository.findByIsOpenTrue();
    }

    public Collection<Store> getStoresByFounder(int founderId) {
        return storeJpaRepository.findByStoreFounderID(founderId);
    }

    public Collection<Store> searchStores(String keyword) {
        return storeJpaRepository.searchByKeyword(keyword);
    }

    public Collection<Store> getStoresByOwner(Integer ownerId) {
        return storeJpaRepository.findStoresByOwnerId(ownerId);
    }
}
