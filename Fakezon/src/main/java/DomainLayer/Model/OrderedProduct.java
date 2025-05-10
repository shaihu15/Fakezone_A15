package DomainLayer.Model;

import ApplicationLayer.DTO.StoreProductDTO;

public class OrderedProduct {
    private final int productId;
    private final String name;
    private final double price;
    private final int quantity;

    public OrderedProduct(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    public int getProductId() {
        return productId;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderedProduct(StoreProductDTO storeProduct, int quantity) {
        this.productId = storeProduct.getProductId();
        this.name = storeProduct.getName();
        this.price = storeProduct.getBasePrice();
        this.quantity = quantity;
    }

}
