package InfrastructureLayer.Repositories;

import java.util.Collection;
import java.util.HashMap;

import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;

public class ProductRepository implements IProductRepository {
    private final HashMap<Integer, IProduct> products;

    public ProductRepository( HashMap<Integer, IProduct> products) {
        this.products = products;
    }

    public ProductRepository() {
        this.products =  new HashMap<>();
    }

    @Override
    public void addProduct(IProduct product) {
        products.put(product.getId(), product);
    }

    @Override
    public void updateProduct(IProduct product) {
        IProduct currentProduct = products.get(product.getId());
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        currentProduct.setName(product.getName());
        products.put(product.getId(), currentProduct);
    }

    @Override
    public void deleteProduct(int productId) {
        IProduct currentProduct = products.get(productId);
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        products.remove(productId);    
    }

    @Override
    public IProduct getProductById(int productId) {
        IProduct currentProduct = products.get(productId);
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        return currentProduct;    
    }

    @Override
    public Collection<IProduct> getAllProducts() {
        return products.values();    
    }
}
