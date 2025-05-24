package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;

public class Cart {

    private Map<Integer,Basket> baskets; // storeID -> Basket

    public Cart() {
        this.baskets = new HashMap<>();
    }

    public void clear() {
        baskets.clear();
    }

    public Map<Integer,Basket> getBaskets() {
        return baskets;
    }
    public void addProduct(int storeID, int productId, int quantity) {
        if (baskets.containsKey(storeID)) {
            baskets.get(storeID).addProduct(productId, quantity);
        } else {
            Basket newBasket = new Basket(storeID);
            newBasket.addProduct(productId, quantity);
            baskets.put(storeID, newBasket);
        }
    }

    public Basket getBasket(int storeID) {
        if(baskets.containsKey(storeID)) {
            return baskets.get(storeID);
        } else {
            throw new IllegalArgumentException("No basket found for store ID: " + storeID);
        }
    }

    public Map<Integer,Map<Integer,Integer>> getAllProducts() {//returns a map of storeID to productID to quantity
        Map<Integer,Map<Integer,Integer>> allProducts = new HashMap<>();
        for (Map.Entry<Integer, Basket> entry : baskets.entrySet()) {
            int storeID = entry.getKey();
            Basket basket = entry.getValue();
            Map<Integer, Integer> products = basket.getProducts(); // productID to quantity
            allProducts.put(storeID, products);
        }
        return allProducts;
    }

    public boolean containsProduct(int productId) {
        for (Basket basket : baskets.values()) {
            if (basket.containsProduct(productId)) {
                return true;
            }
        }
        return false;
    }
}
