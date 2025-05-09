package ApplicationLayer.DTO;

import java.util.List;
import java.util.Map;

public class BasketDTO {

    private final Integer storeId;
    private final Map<StoreProductDTO, Integer> products;

    public BasketDTO(Integer storeId, Map<StoreProductDTO, Integer> products) {
        this.storeId = storeId;
        this.products = products;
    }

    public Integer getStoreId() {
        return storeId;
    }
    public Map<StoreProductDTO, Integer> getProducts() {
        return products;
    }
}
