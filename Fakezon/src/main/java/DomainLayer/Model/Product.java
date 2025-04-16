package DomainLayer.Model;
import DomainLayer.Interfaces.IProduct;


public class Product implements IProduct {
    
    private int id;
    private String name;

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
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
    public void setId(int id) {
        this.id = id;    
    }

    @Override
    public void setName(String name) {
        this.name = name;    
    }

}
