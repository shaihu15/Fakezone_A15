package ApplicationLayer.DTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

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

    // Default constructor with empty fields
    public StoreRolesDTO() {
        this.storeId = 0;
        this.storeName = null;
        this.founderId = 0;
        this.storeOwners = null; // Will be replaced by Jackson if present in JSON
        this.storeManagers = null; // Will be replaced by Jackson if present in JSON
    }

    @JsonCreator
    public StoreRolesDTO(
            @JsonProperty("storeId") int storeId,
            @JsonProperty("storeName") String storeName,
            @JsonProperty("founderId") int founderId,
            @JsonProperty("storeOwners") Collection<Integer> storeOwners,
            @JsonProperty("storeManagers") HashMap<Integer, List<StoreManagerPermission>> storeManagers) {
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
