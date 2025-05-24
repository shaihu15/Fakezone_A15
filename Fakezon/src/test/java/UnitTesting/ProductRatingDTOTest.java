package UnitTesting;

import ApplicationLayer.DTO.ProductRatingDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRatingDTOTest {

    @Test
    void testConstructorAndGetters() {
        double rating = 4.5;
        String comment = "Great product!";
        String email = "user@example.com";

        ProductRatingDTO dto = new ProductRatingDTO(rating, comment, email);

        assertEquals(rating, dto.getRating());
        assertEquals(comment, dto.getComment());
        assertEquals(email, dto.getUserEmail());
    }
}