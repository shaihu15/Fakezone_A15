package DomainLayer.Interfaces;

import DomainLayer.Model.Cart;

public interface IDiscountScope {
    double getEligibleAmount(Cart cart);
}
