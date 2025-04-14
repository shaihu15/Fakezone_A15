package DomainLayer.Model;
import DomainLayer.Interfaces.IProduct;


public class Product implements IProduct {
    
    private int id;
    private String name;
    private int stockQuantity;

    public Product(int id, String name, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
    }

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
        this.stockQuantity = 0;
    }

    @Override
    public int getId() {
        return id;    
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getStockQuantity() {
        return stockQuantity;    
    }

    @Override
    public void setId(int id) {
        this.id = id;    
    }

    @Override
    public void setName(String name) {
        this.name = name;    
    }

    @Override
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;    
    }
}
