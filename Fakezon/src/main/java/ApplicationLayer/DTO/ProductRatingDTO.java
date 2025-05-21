package ApplicationLayer.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductRatingDTO {
    private String userEmail;
    private double rating;
    private String comment;

    @JsonCreator
    public ProductRatingDTO(@JsonProperty("rating") double rating,
                            @JsonProperty("comment") String comment, 
                            @JsonProperty("email") String email) {
        this.userEmail = email;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public double getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

}
