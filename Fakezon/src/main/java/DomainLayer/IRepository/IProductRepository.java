package DomainLayer.IRepository;

import java.util.List;

public interface IProductRepository {
    // Define the methods that the ProductRepository should implement
    void addProduct(String product); // TODO: change the parameter type to the IProduct object when the IProduct class is created
    void updateProduct(String product); // TODO: change the parameter type to the IProduct object when the IProduct class is created
    void deleteProduct(int productId);
    String getProductById(int productId); //TODO: change the return type to the IProduct object when the IProduct class is created
    List<String> getAllProducts(); // TODO: change the return type to List<IProduct> when the IProduct class is created
}
