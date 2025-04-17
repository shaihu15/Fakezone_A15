package DomainLayer.Model;

import java.util.ArrayList;
import java.util.List;

import DomainLayer.Interfaces.IProduct;

public class Basket {
    private Store store;// maby just soreID?
    private List<IProduct> products;

    public Basket(Store store) {
        this.store = store;
        this.products = new ArrayList<>();
    }

    public void addProduct(IProduct product) {
        products.add(product);
    }

    public Store getStore() {
        return store;
    }

    public List<IProduct> getProducts() {
        return products;
    }
}
