package UnitTesting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.AuctionEvents.*;

class AuctionFailedToOwnersEventTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 10;
        int productID = 20;
        double basePrice = 99.99;
        String message = "Auction failed for owners";

        AuctionFailedToOwnersEvent event = new AuctionFailedToOwnersEvent(storeId, productID, basePrice, message);

        assertEquals(storeId, event.getStoreId());
        assertEquals(productID, event.getProductID());
        assertEquals(basePrice, event.getBasePrice());
        assertEquals(message, event.getMessage());
    }
}