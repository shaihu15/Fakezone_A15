package DomainLayer.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "store_ratings")
public class StoreRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int userID;
    
    @Column(nullable = false)
    private double rating;
    
    @Column(length = 1000)
    private String comment;

    // Default constructor for JPA
    public StoreRating() {}

    public StoreRating(int userID, double rating, String comment) {
        this.userID = userID;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
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
