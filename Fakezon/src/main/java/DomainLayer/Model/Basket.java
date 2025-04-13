package DomainLayer.Model;

import java.util.ArrayList;
import java.util.List;

public class Basket {
    private Store store;// maby just soreID?
    private List<Product> products;

    public Basket(Store store) {
        this.store = store;
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public Store getStore() {
        return store;
    }

    public List<Product> getProducts() {
        return products;
    }
}
