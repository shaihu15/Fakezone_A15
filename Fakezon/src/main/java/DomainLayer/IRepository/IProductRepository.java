package DomainLayer.IRepository;

import java.util.Collection;
import java.util.Set;

import DomainLayer.Interfaces.IProduct;

public interface IProductRepository {
    void addProduct(IProduct product);
    void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds);
    void deleteProduct(int productId);
    IProduct getProductById(int productId);
    Collection<IProduct> getAllProducts(); 
    Collection<IProduct> searchProducts(String keyword); 
}
