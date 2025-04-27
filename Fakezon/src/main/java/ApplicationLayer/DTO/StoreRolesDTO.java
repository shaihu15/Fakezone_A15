package ApplicationLayer.DTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Store;


public class StoreRolesDTO {
    @JsonProperty("storeOwners")
    private final Collection<Integer> storeOwners; // Collection of Owner IDs

    @JsonProperty("storeManagers")
    private final HashMap<Integer, List<StoreManagerPermission>> storeManagers; // Collection of Manager User IDs

    @JsonProperty("storeId")
    private final int storeId; // Store ID
    @JsonProperty("storeName")
    private final String storeName; // Store Name
    @JsonProperty("founderId")
    private final int founderId; // Founder ID

    public StoreRolesDTO(int storeId, String storeName, int founderId,
            Collection<Integer> storeOwners,
            HashMap<Integer, List<StoreManagerPermission>> storeManagers) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.founderId = founderId;
        this.storeOwners = storeOwners;
        this.storeManagers = storeManagers;
    }
    public StoreRolesDTO(Store store, int requesterId) {
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.founderId = store.getStoreFounderID();
        this.storeOwners = store.getStoreOwners(requesterId);
        this.storeManagers = store.getStoreManagers(requesterId);
    }
    public Collection<Integer> getStoreOwners() {
        return storeOwners;
    }
    public HashMap<Integer, List<StoreManagerPermission>> getStoreManagers() {
        return storeManagers;
    }
    public int getStoreId() {
        return storeId;
    }
    public String getStoreName() {
        return storeName;
    }
    public int getFounderId() {
        return founderId;
    }
    

}
