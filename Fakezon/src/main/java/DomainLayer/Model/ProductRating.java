package DomainLayer.Model;
public class ProductRating {
    private String userID;
    private double rating;
    private String comment;

    public ProductRating(String userID, double rating, String comment) {
        this.userID = userID;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUserID() {
        return userID;
    }

    public double getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

}
