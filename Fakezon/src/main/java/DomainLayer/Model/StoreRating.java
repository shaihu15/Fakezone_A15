package DomainLayer.Model;

public class StoreRating {
    private int userID;
    private double rating;
    private String comment;

    public StoreRating(int userID, double rating, String comment) {
        this.userID = userID;
        this.rating = rating;
        this.comment = comment;
    }

    public int getUserID() {
        return userID;
    }

    public double getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public void updateRating(double rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
    
    
}
