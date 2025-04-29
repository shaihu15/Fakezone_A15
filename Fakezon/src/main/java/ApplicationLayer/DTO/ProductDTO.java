package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;

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


    public ProductDTO(String name, String description, int id, Set<Integer> storeIds) {
        this.name = name;
        this.description = description;
        this.storeIds = storeIds;
        this.id = id;
    }
    public ProductDTO(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.storeIds = null;
        this.id = id;

    }

    public Set<Integer> getStoresIds() {
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


}
