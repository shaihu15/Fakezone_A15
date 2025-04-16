package DomainLayer.Model;
import DomainLayer.Interfaces.IProduct;


public class Product implements IProduct {
    
    private int id;
    private String name;
    private String description;

    public Product(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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

    @Override
    public String getDescription() {
        return description;    
    }

    @Override
    public void setDescription(String description) {
        this.description = description;    
    }

}
