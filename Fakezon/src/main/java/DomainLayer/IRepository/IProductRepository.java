package DomainLayer.IRepository;

import java.util.Collection;

import DomainLayer.Interfaces.IProduct;

public interface IProductRepository {
    void addProduct(IProduct product);
    void updateProduct(IProduct product);
    void deleteProduct(int productId);
    IProduct getProductById(int productId);
    Collection<IProduct> getAllProducts(); 
    Collection<IProduct> searchProducts(String keyword); 
}
