package ApplicationLayer.Interfaces;

import java.util.List;
import java.util.Set;

import ApplicationLayer.DTO.ProductDTO;

public interface IProductService {
    int addProduct(String productName, String productDescription); // other parameters can be added as needed
    void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds); // other parameters can be added as needed
    void deleteProduct(int productId);
    ProductDTO viewProduct(int productId); 
    List<ProductDTO> searchProducts(String keyword);
}
