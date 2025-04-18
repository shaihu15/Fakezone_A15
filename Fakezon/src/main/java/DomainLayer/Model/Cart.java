package DomainLayer.Model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Basket> baskets;

    public Cart() {
        this.baskets = new ArrayList<>();
    }

    public void clear() {
        baskets.clear();
    }
    public List<Basket> getBaskets() {
        return baskets;
    }
    public void addBasket(Basket basket) {
        baskets.add(basket);
    }


}
