package DomainLayer.Model;

import java.util.ArrayList;
import java.util.List;

import ApplicationLayer.DTO.StoreProductDTO;

public class Basket {
    private int storeId;// maby just soreID?
    private List<StoreProductDTO> products;

    public Basket(int storeId) {
        this.storeId = storeId;
        this.products = new ArrayList<>();
    }
    public Basket(int storeId, List<StoreProductDTO> products) {
        this.storeId = storeId;
        this.products = products;
    }

    public void addProduct(StoreProductDTO product) {
        products.add(product);
    }

    public int getStoreID() {
        return storeId;
    }

    public List<StoreProductDTO> getProducts() {
        return products;
    }
}
