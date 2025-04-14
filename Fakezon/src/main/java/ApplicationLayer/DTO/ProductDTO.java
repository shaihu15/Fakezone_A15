package ApplicationLayer.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("stockQuantity")
    private int stockQuantity;

    public ProductDTO(String name, int stockQuantity) {
        this.name = name;
        this.stockQuantity = stockQuantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
