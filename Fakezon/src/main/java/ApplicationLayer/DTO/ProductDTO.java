package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ApplicationLayer.Enums.PCategory;
import DomainLayer.Interfaces.IProduct;

import java.util.Set;



public class ProductDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private int id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("storeIds")
    private Set<Integer> storeIds;

    @JsonProperty("category")
    private PCategory category;

    // Add no-args constructor
    public ProductDTO() {
    // Default constructor
    }
    @JsonCreator
    public ProductDTO(String name, String description, int id,PCategory category, Set<Integer> storeIds) {
        this.name = name;
        this.description = description;
        this.storeIds = storeIds;
        this.id = id;
        this.category = category;
    }
    public ProductDTO(String name, String description, int id,PCategory category) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.storeIds = null;
        this.id = id;

    }
    public ProductDTO(IProduct product, Set<Integer> storeIds) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.storeIds = storeIds;
        this.id = product.getId();
        this.category = product.getCategory();
    }

    public Set<Integer> getStoreIds() {
        return storeIds;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }

    public String setDescription(String description) {
        this.description = description;
        return description;
    }
    public PCategory getCategory() {
        return category;
    }


}
