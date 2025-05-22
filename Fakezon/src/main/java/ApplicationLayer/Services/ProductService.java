package ApplicationLayer.Services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IProductService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;


public class ProductService implements IProductService {

    private final IProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);



    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository; 
    }

    @Override
    public int addProduct(String productName, String productDescription,PCategory category) {
        try {
            
            IProduct productToAdd = new Product(productName, productDescription,category);
            productRepository.addProduct(productToAdd);
            return productToAdd.getId();
            

        }catch (Exception e) {
            logger.error("An error occurred while adding the product: {}", e.getMessage(), e);
            throw e;
        }
    }    

    @Override
    public void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        IProduct existingProduct = null;
        try{
            existingProduct = productRepository.getProductById(productId);
            if (existingProduct == null) {
                throw new IllegalArgumentException("Product not found");
            }
        }
        catch (IllegalArgumentException e) {
            logger.error("While trying to update, recived error {}", e);
            throw e;
        }
        try {

            productRepository.updateProduct(productId, productName, productDescription, storesIds);
            
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
    public List<ProductDTO> getAllProducts() {
        try {
            Collection<IProduct> products = productRepository.getAllProducts();
            List<ProductDTO> productDTOs = products.stream()
                .map(product -> new ProductDTO(product.getName(), product.getDescription(), product.getId(),product.getCategory() ,new HashSet<>(product.getStoresIds())))
                .toList();
            return productDTOs;
        } catch (Exception e) {
            logger.error("While trying to get all products, received error {}", e);
            throw e;
        } finally {
            logger.info("All products were retrieved");
        }
    }

    @Override
    public ProductDTO viewProduct(int productId) {
        try {
            IProduct product = productRepository.getProductById(productId);
            Set<Integer> prodcutStoresIds = new HashSet<>(product.getStoresIds());
            return new ProductDTO(product, prodcutStoresIds);
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
                .map(product -> new ProductDTO(product.getName(), product.getDescription(), product.getId(),product.getCategory(), new HashSet<>(product.getStoresIds())))
                .toList();
            return productDTOs;
        } catch (Exception e) {
            logger.error("While trying to search, recived error {}", e);
            throw e;
        } finally {
            logger.info("Product with keyword {} was searched", keyword);
        } 
    }

    @Override
    public void addProductsToStore(int storeId, Collection<Integer> productsIds){
        try {
            for (Integer productId : productsIds) {
                IProduct product = productRepository.getProductById(productId);
                Set<Integer> storeSet = new HashSet<>();
                storeSet.add(storeId);
                productRepository.updateProduct(product.getId(), product.getName(), product.getDescription(), storeSet);
            }
        } catch (IllegalArgumentException e) {
            logger.error("While trying to add products to store, recived error {}", e);
            throw e;
        } finally {
            logger.info("Products with ids {} were added to store with id {}", productsIds, storeId);
        }
    }

    @Override
    public void removeStoreFromProducts(int storeId, Collection<Integer> productIds){
        try {
            for (Integer productId : productIds) {
                IProduct product = productRepository.getProductById(productId);
                product.removeStore(storeId);
                productRepository.updateProduct(product.getId(), product.getName(), product.getDescription(), new HashSet<>(product.getStoresIds()));
                List<Integer> storesIds = product.getStoresIds();
                if (storesIds.isEmpty()) {
                    productRepository.deleteProduct(productId);
                }
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("While trying to remove products to store, recived error {}", e);
            throw e;
        } finally {
            logger.info("Products with ids {} were added to store with id {}", productIds, storeId);
        }
    }


    @Override
    public List<ProductDTO> getProductsByCategory(PCategory category) {
        try {
            Collection<IProduct> products = productRepository.getProductsByCategory(category);
            List<ProductDTO> productDTOs = products.stream()
                .map(product -> new ProductDTO(product.getName(), product.getDescription(), product.getId(),product.getCategory(), new HashSet<>(product.getStoresIds())))
                .toList();
            return productDTOs;
        } catch (Exception e) {
            logger.error("While trying to get products by category, recived error {}", e);
            throw e;
        } finally {
            logger.info("Products with category {} were retrieved", category);
        }
    }
    @Override
    public IProduct getProduct(int productId) {
        try {
            return productRepository.getProductById(productId);
        } catch (IllegalArgumentException e) {
            logger.error("While trying to get product, recived error {}", e);
            throw e;
        }
    }
    @Override
    public List<ProductDTO> searchProductsByName(String name) {
        try {
            Collection<IProduct> products = productRepository.searchProductsByName(name);
            List<ProductDTO> productDTOs = products.stream()
                .map(product -> new ProductDTO(product.getName(), product.getDescription(), product.getId(),product.getCategory(), new HashSet<>(product.getStoresIds())))
                .toList();
            return productDTOs;
        } catch (Exception e) {
            logger.error("While trying to search, recived error {}", e);
            throw e;
        } finally {
            logger.info("Product with keyword {} was searched", name);
        }
    }
  
}
