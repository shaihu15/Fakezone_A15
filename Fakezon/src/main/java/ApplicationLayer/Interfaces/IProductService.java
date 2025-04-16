package ApplicationLayer.Interfaces;

import java.util.List;

import ApplicationLayer.DTO.ProductDTO;

public interface IProductService {
    void addProduct(String productName, String productDescription); // other parameters can be added as needed
    void updateProduct(int productId, String productName, String productDescription); // other parameters can be added as needed
    void deleteProduct(int productId);
    ProductDTO viewProduct(int productId); 
    List<ProductDTO> searchProducts(String keyword);
}
