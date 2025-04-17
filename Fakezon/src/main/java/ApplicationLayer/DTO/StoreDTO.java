package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Map;


public class StoreDTO {

    @JsonProperty("storeId")
    private final int storeId;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("founderId")
    private final int founderId;

    @JsonProperty("isOpen")
    private final boolean isOpen;

    @JsonProperty("storeProducts")
    private final Collection<StoreProductDTO> storeProducts;

    @JsonProperty("storeOwners")
    private final Collection<Integer> storeOwners; // Collection of Owner IDs

    @JsonProperty("storeManagers")
    private final Collection<Integer> storeManagers; // Collection of Manager User IDs

    @JsonProperty("ratings")
    private final Map<Integer, Double> ratings; // Map of userId -> rating (no DTO)


    public StoreDTO(int storeId, String name, int founderId, boolean isOpen,
                    Collection<StoreProductDTO> storeProducts,
                    Collection<Integer> storeOwners, Collection<Integer> storeManagers,
                    Map<Integer, Double> ratings) {
        this.storeId = storeId;
        this.name = name;
        this.founderId = founderId;
        this.isOpen = isOpen;
        this.storeProducts = storeProducts;
        this.storeOwners = storeOwners;
        this.storeManagers = storeManagers;
        this.ratings = ratings;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public int getFounderId() {
        return founderId;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Collection<StoreProductDTO> getStoreProducts() {
        return storeProducts;
    }

    public Collection<Integer> getStoreOwners() {
        return storeOwners;
    }

    public Collection<Integer> getStoreManagers() {
        return storeManagers;
    }

    public Map<Integer, Double> getRatings() {
        return ratings;
    }
}
