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
        DomainLayer.Model.StoreProduct mockStoreProduct = mock(DomainLayer.Model.StoreProduct.class);
        when(mockStoreProduct.getSproductID()).thenReturn(10);
        when(mockStoreProduct.getName()).thenReturn("Auction Item");
        when(mockStoreProduct.getBasePrice()).thenReturn(100.0);
        when(mockStoreProduct.getAverageRating()).thenReturn(4.7);
        when(mockStoreProduct.getCategory()).thenReturn(ApplicationLayer.Enums.PCategory.ELECTRONICS);
        when(mockAuctionProduct.getStoreProduct()).thenReturn(mockStoreProduct);
        when(mockAuctionProduct.getCurrentHighestBid()).thenReturn(150.0);
        when(mockAuctionProduct.isDone()).thenReturn(false);

        AuctionProductDTO dto = new AuctionProductDTO(mockAuctionProduct);

        assertEquals(10, dto.getProductId());
        assertEquals("Auction Item", dto.getName());
        assertEquals(100.0, dto.getBasePrice());
        assertEquals(150.0, dto.getCurrentHighestBid());
        assertEquals(4.7, dto.getAverageRating());
        assertEquals(ApplicationLayer.Enums.PCategory.ELECTRONICS, dto.getCategory());
        assertFalse(dto.isDone());
    }
    
}