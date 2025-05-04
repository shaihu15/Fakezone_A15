package ApplicationLayer.Interfaces;

import java.util.List;
import java.util.Set;
import java.util.Collection;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.Interfaces.IProduct;

public interface IProductService {
    int addProduct(String productName, String productDescription); // other parameters can be added as needed
    void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds); // other parameters can be added as needed
    void deleteProduct(int productId);
    ProductDTO viewProduct(int productId); 
    List<ProductDTO> searchProducts(String keyword);
    void addProductsToStore(int storeId, Collection<Integer> productsIds);
    void removeStoreFromProducts(int storeId, Collection<Integer> productIds);
    IProduct getProduct(int productId);
}
