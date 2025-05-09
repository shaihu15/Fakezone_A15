package DomainLayer.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;

public class Cart {

    private List<Basket> baskets;

    public Cart() {
        this.baskets = new ArrayList<>();
    }

    public void clear() {
        baskets.clear();
    }

    public List<Basket> getBaskets() {
        return baskets;
    }
    public void addProduct(int storeID, int productId, int quantity) {
        Basket basket = getBasket(storeID);
        if (basket != null) {
            basket.addProduct(productId, quantity);
            return;
        }
        Basket newBasket = new Basket(storeID);
        newBasket.addProduct(productId, quantity);
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

    public Map<Integer,Map<Integer,Integer>> getAllProducts() {//returns a map of storeID to productID to quantity
        Map<Integer,Map<Integer,Integer>> allProducts = new HashMap<>();
        for (Basket basket : baskets) {
            allProducts.put(basket.getStoreID(), basket.getProducts());
        }
        return allProducts;
    }
}
