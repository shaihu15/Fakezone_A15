package DomainLayer.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ApplicationLayer.DTO.StoreProductDTO;
import jakarta.persistence.*;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<Basket> baskets = new ArrayList<>(); // storeID -> Basket

    public Cart() {
        // JPA default constructor
    }

    public void clear() {
        baskets.clear();
    }

    public List<Basket> getBaskets() {
        return baskets;
    }

    private Basket findBasket(int storeId) {
        for (Basket basket : baskets) {
            if (basket.getStoreID() == storeId) {
                return basket;
            }
        }
        return null;
    }

    public void addProduct(int storeID, int productId, int quantity) {
        Basket basket = findBasket(storeID);
        if (basket != null) {
            basket.addProduct(productId, quantity);
        } else {
            Basket newBasket = new Basket(storeID);
            newBasket.addProduct(productId, quantity);
            baskets.add(newBasket);
        }
    }

    public Basket getBasket(int storeID) {
        Basket basket = findBasket(storeID);
        if (basket != null) {
            return basket;
        } else {
            throw new IllegalArgumentException("No basket found for store ID: " + storeID);
        }
    }

    public Map<Integer,Map<Integer,Integer>> getAllProducts() {//returns a map of storeID to productID to quantity
        Map<Integer,Map<Integer,Integer>> allProducts = new HashMap<>();
        for (Basket basket : baskets) {
            int storeID = basket.getStoreID();
            Map<Integer, Integer> products = basket.getProducts(); // productID to quantity
            allProducts.put(storeID, products);
        }
        return allProducts;
    }

    public void setProduct(int storeId, int productId, int quantity){
        Basket basket = findBasket(storeId);
        if (basket != null) {
            basket.setProduct(productId, quantity);
        } else {
            Basket newBasket = new Basket(storeId);
            newBasket.setProduct(productId, quantity);
            baskets.add(newBasket);
        }
    }

    public void removeItem(int storeId, int productId){
        Basket basket = findBasket(storeId);
        if (basket != null) {
            basket.removeItem(productId);
        }
    }

    public boolean containsProduct(int productId) {
        for (Basket basket : baskets) {
            if (basket.containsProduct(productId)) {
                return true;
            }
        }
        return false;
    }

    public void addProductQuantity(int storeID, int productId, int quantity) {
        Basket basket = findBasket(storeID);
        if (basket != null) {
            basket.addProductQuantity(productId, quantity);
        } else {
            Basket newBasket = new Basket(storeID);
            newBasket.addProductQuantity(productId, quantity);
            baskets.add(newBasket);
        }
    }
    

}
