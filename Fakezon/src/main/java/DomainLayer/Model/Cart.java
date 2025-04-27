package DomainLayer.Model;

import java.util.ArrayList;
import java.util.List;
import ApplicationLayer.DTO.StoreProductDTO;

public class Cart {
    private List<Basket> baskets;

    public Cart() {
        this.baskets = new ArrayList<>();
    }

    public void clear() {
        baskets.clear();
    }

    public boolean addProduct(int storeID, StoreProductDTO product) {
        for (Basket basket : baskets) {
            if (basket.getStoreID() == storeID) {
                basket.addProduct(product);
                return true;
            }
        }
        Basket newBasket = new Basket(storeID);
        newBasket.addProduct(product);
        baskets.add(newBasket);
        return true;
    }
}
