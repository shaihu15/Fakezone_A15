package ApplicationLayer.DTO;

import java.util.Map;

public class BasketDTO {

    private final Integer storeId;
    private final Map<StoreProductDTO, Boolean> products;

    public BasketDTO(Integer storeId, Map<StoreProductDTO, Boolean> products) {
        this.storeId = storeId;
        this.products = products;
    }

    public Integer getStoreId() {
        return storeId;
    }
    public Map<StoreProductDTO, Boolean> getProducts() {
        return products;
    }
}
