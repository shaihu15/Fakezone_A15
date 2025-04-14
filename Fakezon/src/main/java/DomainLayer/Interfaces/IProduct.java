package DomainLayer.Interfaces;

public interface IProduct {
    int getId();
    String getName();
    int getStockQuantity();
    void setId(int id);
    void setName(String name);
    void setStockQuantity(int stockQuantity); 
}