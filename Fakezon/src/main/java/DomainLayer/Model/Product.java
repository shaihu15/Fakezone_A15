package DomainLayer.Model;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale.Category;
import java.util.concurrent.atomic.AtomicInteger;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Interfaces.IProduct;


public class Product implements IProduct {
    
    private int id;
    private String name;
    private String description;
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private PCategory category; 
    private Set<Integer> storesIds; // List of store IDs where the product is available

    public Product(String name, String description,PCategory category) {
        this.id = idCounter.incrementAndGet();
        this.name = name;
        this.description = description;
        this.category = category;
        this.storesIds = new HashSet<>();
        
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
        this.id = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.storesIds = new HashSet<>();
        
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
