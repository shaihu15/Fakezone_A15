package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
@DiscriminatorValue("PRODUCTS")
public class ProductsDiscountScope extends BaseDiscountScope {
    
    @ElementCollection
    @CollectionTable(name = "discount_scope_products", joinColumns = @JoinColumn(name = "scope_id"))
    @Column(name = "product_id")
    private List<Integer> productIds;
    
    @Transient
    private Map<StoreProductKey, StoreProduct> storeProducts;
    
    // Default constructor for JPA
    protected ProductsDiscountScope() {
        super();
    }
    
    public ProductsDiscountScope(List<Integer> productIds, int storeId, Map<StoreProductKey, StoreProduct> storeProducts) {
        super(storeId);
        this.productIds = productIds;
        this.storeProducts = storeProducts;
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    @Override
    public double getEligibleAmount(Cart cart) {
        Map<Integer,Map<Integer,Integer>> allProducts = cart.getAllProducts();
        double eligibleAmount = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allProducts.entrySet()) {
            int currentStoreId = entry.getKey();
            Map<Integer, Integer> products = entry.getValue();
            
            // Check if this is the correct store
            if (currentStoreId == getStoreId()) {
                for (Map.Entry<Integer, Integer> product : products.entrySet()) {
                    int productId = product.getKey();
                    int quantity = product.getValue();
                    
                    // Check if this product is in the eligible products list
                    if (productIds.contains(productId)) {
                        eligibleAmount += quantity * storeProducts.get(new StoreProductKey(getStoreId(), productId)).getBasePrice();
                    }
                }
            }
        }
        return eligibleAmount;
    }
    
    public void setStoreProducts(Map<StoreProductKey, StoreProduct> storeProducts) {
        this.storeProducts = storeProducts;
    }
}