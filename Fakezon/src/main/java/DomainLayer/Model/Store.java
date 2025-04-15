package DomainLayer.Model;
//just basic store class to prevent errors will be change in the issue of store model
public class Store {
    private String id;
    private String name;


    public Store(String id, String name) {
        this.id = id;
  
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
