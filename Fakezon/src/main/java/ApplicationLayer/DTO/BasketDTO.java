package ApplicationLayer.DTO;

import java.util.List;

public class BasketDTO {

    private final Integer storeId;
    private final List<StoreProductDTO> products;

    public BasketDTO(Integer storeId, List<StoreProductDTO> products) {
        this.storeId = storeId;
        this.products = products;
    }

    public Integer getStoreId() {
        return storeId;
    }
    public List<StoreProductDTO> getProducts() {
        return products;
    }
}
