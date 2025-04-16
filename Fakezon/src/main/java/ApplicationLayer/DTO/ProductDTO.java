package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;


    public ProductDTO(String name, String description) {
        this.name = name;
        this.description = description;
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
