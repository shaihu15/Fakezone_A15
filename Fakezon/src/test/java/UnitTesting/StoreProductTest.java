package UnitTesting;


import ApplicationLayer.Enums.PCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.StoreProduct;
class StoreProductTest {

    @Test
    void testConstructorAndGetters() {
        StoreProduct product = new StoreProduct(1, 10, "TestProduct", 99.99, 5, PCategory.ELECTRONICS);

        assertEquals(1, product.getSproductID());
        assertEquals(10, product.getStoreId());
        assertEquals("TestProduct", product.getName());
        assertEquals(99.99, product.getBasePrice());
        assertEquals(5, product.getQuantity());
        assertEquals(PCategory.ELECTRONICS, product.getCategory());
    }

    @Test
    void testCopyConstructor() {
        StoreProduct original = new StoreProduct(2, 20, "CopyProduct", 49.99, 3, PCategory.ELECTRONICS);
        StoreProduct copy = new StoreProduct(original);

        assertEquals(original.getStoreId(), copy.getStoreId());
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getBasePrice(), copy.getBasePrice());
        assertEquals(original.getQuantity(), copy.getQuantity());
        assertEquals(original.getCategory() ,copy.getCategory());
    }

    @Test
    void testAddRatingAndGetRatingByUser() {
        StoreProduct product = new StoreProduct(3, 30, "RatedProduct", 19.99, 2, PCategory.ELECTRONICS );

        // Add new rating
        product.addRating(100, 4.0, "Good");
        ProductRating rating = product.getRatingByUser(100);
        assertEquals(100, rating.getUserID());
        assertEquals(4.0, rating.getRating());
        assertEquals("Good", rating.getComment());

        // Update rating
        product.addRating(100, 5.0, "Excellent");
        ProductRating updatedRating = product.getRatingByUser(100);
        assertEquals(5.0, updatedRating.getRating());
        assertEquals("Excellent", updatedRating.getComment());
    }

    @Test
    void testGetRatingByUserThrows() {
        StoreProduct product = new StoreProduct(4, 40, "NoRatingProduct", 29.99, 1, PCategory.ELECTRONICS);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> product.getRatingByUser(200));
        assertTrue(exception.getMessage().contains("User with ID: 200 has not rated the product with ID: 4"));
    }

    @Test
    void testGetAverageRating() {
        StoreProduct product = new StoreProduct(5, 50, "AvgProduct", 59.99, 4, PCategory.ELECTRONICS);

        // No ratings: should throw ArithmeticException (division by zero)
        //assertThrows(ArithmeticException.class, product::getAverageRating);

        // Add ratings
        product.addRating(1, 4.0, "Nice");
        product.addRating(2, 2.0, "Bad");
        product.addRating(3, 5.0, "Great");

        double avg = product.getAverageRating();
        assertEquals((4.0 + 2.0 + 5.0) / 3, avg);
    }

    @Test
    void testSetQuantity() {
        StoreProduct product = new StoreProduct(6, 60, "QuantityProduct", 10.0, 7, PCategory.ELECTRONICS);

        product.setQuantity(15);
        assertEquals(15, product.getQuantity());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> product.setQuantity(-1));
        assertEquals("Quantity cannot be negative", exception.getMessage());
    }
}