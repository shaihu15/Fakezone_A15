package DomainLayer.Model;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.*;

@Entity
@Table(name = "baskets")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int storeId;

    @ElementCollection
    @CollectionTable(name = "basket_products", joinColumns = @JoinColumn(name = "basket_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Integer, Integer> productQuantities = new HashMap<>();

    public Basket() {
        // JPA default constructor
    }

    public Basket(int storeId) {
        this.storeId = storeId;
    }
    public Basket(int storeId, Map<Integer, Integer> products) {
        this.storeId = storeId;
        this.productQuantities = products;
    }

    public void addProduct(int productId, int quantity) {
        productQuantities.put(productId, quantity);
    }

    public int getStoreID() {
        return storeId;
    }

    public Map<Integer,Integer> getProducts() {
        return productQuantities;
    }

    public void setProduct(int productId, int quantity){
        productQuantities.put(productId, quantity);
    }

    public void removeItem(int productId){
        productQuantities.remove(productId);
    }

    public boolean containsProduct(int productId) {
        return productQuantities.containsKey(productId);
    }

    public void addProductQuantity(int productId, int quantity) {
        if(!productQuantities.containsKey(productId))
            addProduct(productId, quantity);
        else{
            int oldQuantity = productQuantities.get(productId);
            productQuantities.put(productId, oldQuantity + quantity);
        }
    }

}
