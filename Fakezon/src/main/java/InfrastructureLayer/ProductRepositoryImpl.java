package InfrastructureLayer;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;

@Repository
@Primary
public class ProductRepositoryImpl implements IProductRepository {

    @Autowired
    private ProductJpaRepository productJpaRepository;

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
        Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        product.setName(productName);
        product.setDescription(productDescription);
        
        // Update store IDs - need to properly handle the collection
        if (storesIds != null) {
            // Clear existing stores by removing them one by one
            List<Integer> currentStoreIds = new ArrayList<>(product.getStoresIds());
            for (Integer storeId : currentStoreIds) {
                product.removeStore(storeId);
            }
            // Add new stores
            for (Integer storeId : storesIds) {
                product.addStore(storeId);
            }
        }
        
        productJpaRepository.save(product);
    }

    @Override
    public void deleteProduct(int productId) {
        productJpaRepository.deleteById(productId);
    }

    @Override
    public IProduct getProductById(int productId) {
        return productJpaRepository.findById(productId).orElse(null);
    }

    @Override
    public Collection<IProduct> getAllProducts() {
        return productJpaRepository.findAll().stream()
                .map(product -> (IProduct) product)
                .toList();
    }

    @Override
    public Collection<IProduct> searchProducts(String keyword) {
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

    @Override
    public void clearAllData() {
        productJpaRepository.deleteAll();
    }
} 