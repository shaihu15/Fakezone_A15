package DomainLayer.Model;

import java.util.function.Predicate;

public class DiscountCondition {
    // public static Predicate<Cart> totalAbove(double threshold) {
    //     return cart -> cart.getTotalPrice() > threshold;
    // }

    public static Predicate<Cart> containsProduct(int productId) {
        return cart -> cart.containsProduct(productId);
    }

    public static Predicate<Cart> alwaysTrue() {
        return cart -> true;
    }
}
