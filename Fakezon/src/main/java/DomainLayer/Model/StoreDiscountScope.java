package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountScope;
import jakarta.persistence.*;

import java.util.Map;

@Entity
@DiscriminatorValue("STORE")
public class StoreDiscountScope extends BaseDiscountScope {
    
    @Transient
    private Map<StoreProductKey, StoreProduct> storeProducts;
    
    // Default constructor for JPA
    protected StoreDiscountScope() {
        super();
    }

    public StoreDiscountScope(int storeId, Map<StoreProductKey, StoreProduct> storeProducts) {
        super(storeId);
        this.storeProducts = storeProducts;
    }

    @Override
    public double getEligibleAmount(Cart cart) {
        double eligibleAmount = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : cart.getAllProducts().entrySet()) {
            if (entry.getKey() == getStoreId()) {
                for (Map.Entry<Integer, Integer> product : entry.getValue().entrySet()) {
                    eligibleAmount += product.getValue() * storeProducts.get(new StoreProductKey(getStoreId(), product.getKey())).getBasePrice();
                }
            }
        }
        return eligibleAmount;
    }
    
    public void setStoreProducts(Map<StoreProductKey, StoreProduct> storeProducts) {
        this.storeProducts = storeProducts;
    }
}
