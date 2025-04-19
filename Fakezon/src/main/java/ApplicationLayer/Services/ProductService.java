package ApplicationLayer.Services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Interfaces.IProductService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductService implements IProductService {

    private final IProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);



    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository; 
    }

    @Override
    public int addProduct(String productName, String productDescription) {
        try {
            
            IProduct productToAdd = new Product(productName, productDescription);
            productRepository.addProduct(productToAdd);
            return productToAdd.getId();
            

        }catch (Exception e) {
            logger.error("An error occurred while adding the product: {}", e.getMessage(), e);
            throw e;
        }
    }    

    @Override
    public void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        try {
            IProduct product = new Product(productId, productName, productDescription, storesIds);
            productRepository.updateProduct(product);
            
        } catch (IllegalArgumentException e) {
            logger.error("While trying to update, recived error {}", e);
            throw e;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            productRepository.deleteProduct(productId);
        } catch (IllegalArgumentException e) {
            logger.error("While trying to delete, recived error {}", e);
            throw e;
        }

    }

    @Override
    public ProductDTO viewProduct(int productId) {
        try {
            IProduct product = productRepository.getProductById(productId);
            return new ProductDTO(product.getName(), product.getDescription());
        } catch (IllegalArgumentException e) {
            logger.error("While trying to view, recived error {}", e);
            throw e;
        } finally {
            logger.info("Product with id {} was viewed", productId);
        }
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        try {
            Collection<IProduct> products = productRepository.searchProducts(keyword);
            List<ProductDTO> productDTOs = products.stream()
                .map(product -> new ProductDTO(product.getName(), product.getDescription()))
                .toList();
            return productDTOs;
        } catch (Exception e) {
            logger.error("While trying to search, recived error {}", e);
            throw e;
        } finally {
            logger.info("Product with keyword {} was searched", keyword);
        } 
    }

  
}
