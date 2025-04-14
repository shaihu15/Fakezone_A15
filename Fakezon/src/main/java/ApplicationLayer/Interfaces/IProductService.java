package ApplicationLayer.Interfaces;

import java.util.List;

public interface IProductService {
    void addProduct(String productName); // other parameters can be added as needed
    void updateProduct(int productId, String productName); // other parameters can be added as needed
    void deleteProduct(int productId);
    String viewProduct(int productId); // TODO: when the IProdectDTO interface is created, change the return type to IProductDTO
    List<String> searchProducts(String keyword);// TODO: when the IProdectDTO interface is created, change the return type to List<IProductDTO>
}
