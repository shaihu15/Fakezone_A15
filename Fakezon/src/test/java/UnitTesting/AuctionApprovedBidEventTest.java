package UnitTesting;


import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import DomainLayer.Model.helpers.AuctionEvents.AuctionApprovedBidEvent;

class AuctionApprovedBidEventTest {

    @Test
    void testConstructorAndGetters() {
        int storeId = 1;
        int productID = 2;
        int userIDHighestBid = 3;
        double currentHighestBid = 150.5;
        StoreProductDTO storeProductDTO = new StoreProductDTO(
                productID, "TestProduct", 99.99, 5, 4.5, storeId, PCategory.ELECTRONICS
        );

        AuctionApprovedBidEvent event = new AuctionApprovedBidEvent(
                storeId, productID, userIDHighestBid, currentHighestBid, storeProductDTO
        );

        assertEquals(storeId, event.getStoreId());
        assertEquals(productID, event.getProductID());
        assertEquals(userIDHighestBid, event.getUserIDHighestBid());
        assertEquals(currentHighestBid, event.getCurrentHighestBid());
        assertEquals(storeProductDTO, event.getStoreProductDTO());
    }
}