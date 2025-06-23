package DomainLayer.Model;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Interfaces.IProduct;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product implements IProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PCategory category; 
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_stores", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "store_id")
    private Set<Integer> storesIds; // List of store IDs where the product is available
    
    // Default constructor for JPA
    public Product() {
        this.storesIds = new HashSet<>();
    }

    public Product(String name, String description,PCategory category) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;

    }

    public Product(int id, String name, String description,PCategory category, Set<Integer> storesIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
       if (storesIds == null) {
            this.storesIds = new HashSet<>();
        } else {
            this.storesIds = storesIds;
        }
    }

     /**
     * **********DO NOT USE - JUST FOR UI PURPOSES**********
     **/
    public Product(String name, String description,PCategory category, int productId) {
        this();
        this.id = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        
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

    @Override
    public List<Integer> getStoresIds() {
        return storesIds.stream().toList();
    }

    @Override
    public void addStore(Integer storesId) {
        if (storesIds == null) {
            storesIds = new HashSet<>();
        }
        storesIds.add(storesId);
    }

    @Override
    public void removeStore(Integer storesIds) {
        if (this.storesIds != null) {
            this.storesIds.remove(storesIds);
        }
        
    }
    @Override
    public PCategory getCategory() {
        return category;
    }

}
