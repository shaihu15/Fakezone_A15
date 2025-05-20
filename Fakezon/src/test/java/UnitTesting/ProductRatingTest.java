package UnitTesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.ProductRating;

class ProductRatingTest {

    @Test
    void testConstructorAndGetters() {
        int userId = 42;
        double rating = 4.5;
        String comment = "Great product!";

        ProductRating productRating = new ProductRating(userId, rating, comment);

        assertEquals(userId, productRating.getUserID());
        assertEquals(rating, productRating.getRating());
        assertEquals(comment, productRating.getComment());
    }

    @Test
    void testUpdateRating() {
        ProductRating productRating = new ProductRating(1, 3.0, "Ok");
        productRating.updateRating(5.0, "Excellent!");

        assertEquals(5.0, productRating.getRating());
        assertEquals("Excellent!", productRating.getComment());
    }
}