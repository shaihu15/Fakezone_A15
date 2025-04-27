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

    public void addProduct(int storeID, StoreProductDTO product) {
        Basket basket = getBasket(storeID);
        if (basket != null) {
            basket.addProduct(product);
            return;
        }
        Basket newBasket = new Basket(storeID);
        newBasket.addProduct(product);
        baskets.add(newBasket);
    }

    public Basket getBasket(int storeID) {
        for (Basket basket : baskets) {
            if (basket.getStoreID() == storeID) {
                return basket;
            }
        }
        return null;
    }
}
