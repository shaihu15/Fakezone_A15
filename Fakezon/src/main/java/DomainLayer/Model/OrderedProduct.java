package DomainLayer.Model;

import ApplicationLayer.DTO.StoreProductDTO;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ordered_products")
public class OrderedProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private int productId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "price", nullable = false)
    private double price;
    
    @Column(name = "quantity", nullable = false)
    private int quantity;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Default constructor for JPA
    protected OrderedProduct() {
        // JPA will populate fields
    }

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

    // Getters and setters for JPA relationship
    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderedProduct that = (OrderedProduct) obj;
        return productId == that.productId &&
               Double.compare(that.price, price) == 0 &&
               quantity == that.quantity &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, price, quantity);
    }

    @Override
    public String toString() {
        return "OrderedProduct{" +
                "id=" + id +
                ", productId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }

}
