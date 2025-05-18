package UnitTesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.AuctionEvents.AuctionDeclinedBidEvent;

class AuctionDeclinedBidEventTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 1;
        int productID = 2;
        int userIDHighestBid = 3;
        double currentHighestBid = 99.99;

        AuctionDeclinedBidEvent event = new AuctionDeclinedBidEvent(storeId, productID, userIDHighestBid, currentHighestBid);

        assertEquals(storeId, event.getStoreId());
        assertEquals(productID, event.getProductID());
        assertEquals(userIDHighestBid, event.getUserIDHighestBid());
        assertEquals(currentHighestBid, event.getCurrentHighestBid());
    }
}