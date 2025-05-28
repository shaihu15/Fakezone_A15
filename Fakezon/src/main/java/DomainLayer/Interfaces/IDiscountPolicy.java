package DomainLayer.Interfaces;

import DomainLayer.Model.Cart;

public interface IDiscountPolicy {
    int getPolicyID();
    double apply(Cart cart); // Returns discount amount (not percentage)
    boolean isApplicable(Cart cart);
}
