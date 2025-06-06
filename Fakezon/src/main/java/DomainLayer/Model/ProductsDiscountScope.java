package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountScope;

import java.util.List;
import java.util.Map;

public class ProductsDiscountScope implements IDiscountScope {
    private List<Integer> productIds;
    private int storeId;
    private Map<Integer, StoreProduct> storeProducts;
    
    public ProductsDiscountScope(List<Integer> productIds, int storeId, Map<Integer, StoreProduct> storeProducts) {
        this.productIds = productIds;
        this.storeId = storeId;
        this.storeProducts = storeProducts;
    }


    public List<Integer> getProductIds() {
        return productIds;
    }

    public int getStoreId() {
        return storeId;
    }

    @Override
    public double getEligibleAmount(Cart cart) {
        Map<Integer,Map<Integer,Integer>> allProducts = cart.getAllProducts();
        double eligibleAmount = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : allProducts.entrySet()) {
            int currentStoreId = entry.getKey();
            Map<Integer, Integer> products = entry.getValue();
            
            // Check if this is the correct store
            if (currentStoreId == storeId) {
                for (Map.Entry<Integer, Integer> product : products.entrySet()) {
                    int productId = product.getKey();
                    int quantity = product.getValue();
                    
                    // Check if this product is in the eligible products list
                    if (productIds.contains(productId)) {
                        eligibleAmount += quantity * storeProducts.get(productId).getBasePrice();
                    }
                }
            }
        }
        return eligibleAmount;
    }
}