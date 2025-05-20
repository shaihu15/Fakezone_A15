package InfrastructureLayer.Repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.StoreService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Product;

public class ProductRepository implements IProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);
    private final HashMap<Integer, IProduct> products;

    public ProductRepository( HashMap<Integer, IProduct> products) {
        this.products = products;
    }

    public ProductRepository() {
        this.products =  new HashMap<>();

        //FOR UI PUT IN COMMENT IF NOT NEEDED!
        init();
    }

    @Override
    public void addProduct(IProduct product) {
        products.put(product.getId(), product);
    }

    @Override
    public void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        IProduct currentProduct = products.get(productId);
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

        products.put(productId, currentProduct);
    }

    @Override
    public void deleteProduct(int productId) {
        IProduct currentProduct = products.get(productId);
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        products.remove(productId);    
    }

    @Override
    public IProduct getProductById(int productId) {
        IProduct currentProduct = products.get(productId);
        if(currentProduct == null){
            throw new IllegalArgumentException("Product not found in the repository.");
        }
        return currentProduct;    
    }

    @Override
    public Collection<IProduct> getAllProducts() {
        return products.values();    
    }
    
    @Override
    public Collection<IProduct> searchProducts(String keyword){
        Collection<IProduct> result = new ArrayList<>();
        for(IProduct product : products.values()){
            if(product.getName().toLowerCase().contains(keyword.toLowerCase()) || 
               product.getDescription().toLowerCase().contains(keyword.toLowerCase())){
                result.add(product);
            }
        }
        return result;
    }

    @Override
    public Collection<IProduct> getProductsByCategory(PCategory category) {
        Collection<IProduct> result = new ArrayList<>();
        for(IProduct product : products.values()){
            if(product.getCategory() == category){
                result.add(product);
            }
        }
        return result;
    } 


    private void init(){
        logger.info("product repo init");
        products.put(1001, new Product("Product1001", "description1001", PCategory.BOOKS, 1001));
        products.put(1002, new Product("Product1002", "description1002", PCategory.MUSIC, 1002));
        products.get(1001).addStore(1001);
        products.get(1002).addStore(1001);
    }

}
