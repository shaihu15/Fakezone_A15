package ApplicationLayer.DTO;

public class ProductSearchViewDTO {
    private final int id;
    private final String name;
    private final String description;
    private final String category;
    private final double storeAverageRating;
    private final double prductAverageRating;

    public ProductSearchViewDTO(int id, String name, String description, String category, double storeAverageRating, double productAverageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.storeAverageRating = storeAverageRating;
        this.prductAverageRating = productAverageRating;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getCategory() {
        return category;
    }
    public double getStoreAverageRating() {
        return storeAverageRating;
    }
    public double getProductAverageRating() {
        return prductAverageRating;
    }
}
