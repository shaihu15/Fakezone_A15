package ApplicationLayer.Services;

import DomainLayer.IRepository.IStoreRepository;

public class StoreService {
    IStoreRepository storeRepository;
    public void addStoreOwner(int storeId, int requesterId, int newOwnerId){
        try{
        storeRepository.addStoreOwner(storeId, requesterId, newOwnerId);
        }catch (Exception e){
            System.out.println("Error doring add store owner: " + e.getMessage());
        }
    }


}
