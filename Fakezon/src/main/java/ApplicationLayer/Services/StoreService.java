package ApplicationLayer.Services;

import DomainLayer.IRepository.IStoreRepository;

public class StoreService {
    IStoreRepository storeRepository;
    public void addStoreOwner(int storeId, int requesterId, int newOwnerId){
        storeRepository.addStoreOwner(storeId, requesterId, newOwnerId);
    }


}
