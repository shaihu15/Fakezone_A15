package UnitTesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Model.StoreRating;

class StoreRatingTest {

    @Test
    void testConstructorAndGetters() {
        int userId = 101;
        double rating = 4.2;
        String comment = "Nice store!";

        StoreRating storeRating = new StoreRating(userId, rating, comment);

        assertEquals(userId, storeRating.getUserID());
        assertEquals(rating, storeRating.getRating());
        assertEquals(comment, storeRating.getComment());
    }

    @Test
    void testUpdateRating() {
        StoreRating storeRating = new StoreRating(1, 3.0, "Ok");
        storeRating.updateRating(5.0, "Excellent!");

        assertEquals(5.0, storeRating.getRating());
        assertEquals("Excellent!", storeRating.getComment());
    }
}