package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;

public class Basket {
    private int storeId;// maby just soreID?
    private Map<Integer, Integer> productQuantities; // productID -> quantity

    public Basket(int storeId) {
        this.storeId = storeId;
        this.productQuantities = new HashMap<>();
    }
    public Basket(int storeId, Map<Integer, Integer> products) {
        this.storeId = storeId;
        this.productQuantities = products;
    }

    public void addProduct(int productId, int quantity) {
        if (productQuantities.containsKey(productId)) {
            productQuantities.put(productId, productQuantities.get(productId) + quantity);
        } else {
            productQuantities.put(productId, quantity);
        }
    }

    public int getStoreID() {
        return storeId;
    }

    public Map<Integer,Integer> getProducts() {
        return productQuantities;
    }

    public boolean containsProduct(int productId) {
        return productQuantities.containsKey(productId);
    }
}
