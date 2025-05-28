package UnitTesting;

import ApplicationLayer.DTO.AuctionProductDTO;
import DomainLayer.Model.AuctionProduct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuctionProductDTOTest {

    @Test
    void testConstructorAndGetters() {
        AuctionProduct mockAuctionProduct = mock(AuctionProduct.class);
        when(mockAuctionProduct.getSproductID()).thenReturn(10);
        when(mockAuctionProduct.getName()).thenReturn("Auction Item");
        when(mockAuctionProduct.getBasePrice()).thenReturn(100.0);
        when(mockAuctionProduct.getCurrentHighestBid()).thenReturn(150.0);
        when(mockAuctionProduct.getAverageRating()).thenReturn(4.7);

        AuctionProductDTO dto = new AuctionProductDTO(mockAuctionProduct);

        assertEquals(10, dto.getProductId());
        assertEquals("Auction Item", dto.getName());
        assertEquals(100.0, dto.getBasePrice());
        assertEquals(150.0, dto.getCurrentHighestBid());
        assertEquals(4.7, dto.getAverageRating());
    }
    
}