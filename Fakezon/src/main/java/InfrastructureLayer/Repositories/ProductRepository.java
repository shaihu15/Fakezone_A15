package InfrastructureLayer.Repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;
import jakarta.transaction.Transactional;



@Repository
@Primary
//@Transactional
public class ProductRepository implements IProductRepository {
    
    private ProductJpaRepository productJpaRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    //private final HashMap<Integer, IProduct> products;
    @Autowired
    public ProductRepository(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    public ProductRepository() {
        throw new UnsupportedOperationException("ProductRepository requires productJpaRepository. Use @SpringBootTest for integration tests.");
    }

    @Override
    public void addProduct(IProduct product) {
        if (product instanceof Product) {
            productJpaRepository.save((Product) product);
        } else {
            throw new IllegalArgumentException("Product must be an instance of Product class");
        }
    }

    @Override
    public void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        Product currentProduct = productJpaRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        if(productName != null && !productName.isEmpty()){
            currentProduct.setName(productName);
        }
        if(productDescription != null){
            currentProduct.setDescription(productDescription);
        }
        if(storesIds != null){
            for(Integer storeId : storesIds){
                if(!currentProduct.getStoresIds().contains(storeId)){
                    currentProduct.addStore(storeId);
                }
                else{
                    currentProduct.removeStore(storeId);
                }
            }
        }
        productJpaRepository.save(currentProduct);
    }

    @Override
    public void deleteProduct(int productId) {
        if(!productJpaRepository.existsById(productId)){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        productJpaRepository.deleteById(productId);    
    }

    @Override
    public IProduct getProductById(int productId) {
        IProduct currentProduct = productJpaRepository.findById(productId).orElse(null);
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        return currentProduct;    
    }

    @Override
    public Collection<IProduct> getAllProducts() {
        return productJpaRepository.findAll().stream()
                .map(product -> (IProduct) product)
                .collect(Collectors.toList());
    }
    
    @Override
    public Collection<IProduct> searchProducts(String keyword){
        return productJpaRepository.searchByKeyword(keyword).stream()
        .map(product -> (IProduct) product)
        .toList();
    }

    @Override
    public Collection<IProduct> getProductsByCategory(PCategory category) {
        return productJpaRepository.findByCategory(category).stream()
        .map(product -> (IProduct) product)
        .toList();
    } 


    @Override
    public Collection<IProduct> searchProductsByName(String name) {
        return productJpaRepository.findByNameContainingIgnoreCase(name).stream()
        .map(product -> (IProduct) product)
        .toList();
    }
    private void init(){
        logger.info("product repo init");
        productJpaRepository.save(new Product("Product1001", "description1001", PCategory.BOOKS, 1001));
        productJpaRepository.save(new Product("Product1002", "description1002", PCategory.MUSIC, 1002));
        productJpaRepository.findById(1001).orElse(null).addStore(1001);
        productJpaRepository.findById(1002).orElse(null).addStore(1001);
    }

    @Override
    public void clearAllData() {
        productJpaRepository.deleteAll();
    }
}
