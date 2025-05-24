package DomainLayer.Model;

import DomainLayer.Interfaces.IDiscountScope;

import java.util.Map;

public class StoreDiscountScope implements IDiscountScope {
    private int storeId;
    private Map<Integer, StoreProduct> storeProducts;

    public StoreDiscountScope(int storeId, Map<Integer, StoreProduct> storeProducts) {
        this.storeId = storeId;
        this.storeProducts = storeProducts;
    }

    @Override
    public double getEligibleAmount(Cart cart) {
        double eligibleAmount = 0;
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : cart.getAllProducts().entrySet()) {
            if (entry.getKey() == storeId) {
                for (Map.Entry<Integer, Integer> product : entry.getValue().entrySet()) {
                    eligibleAmount += product.getValue() * storeProducts.get(product.getKey()).getBasePrice();
                }
            }
        }
        return eligibleAmount;
    }
}
