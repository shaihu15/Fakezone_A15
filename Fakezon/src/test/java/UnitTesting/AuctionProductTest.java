package UnitTesting;

import DomainLayer.Model.AuctionProduct;
import DomainLayer.Model.StoreProduct;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionProductTest {

    private AuctionProduct auctionProduct;

    @BeforeEach
    void setUp() {
        // Initialize test data
        StoreProduct storeProduct = new StoreProduct(1, 10, "p1", 10, 4, PCategory.ELECTRONICS);
        auctionProduct = new AuctionProduct(storeProduct, 50.0, 7);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(auctionProduct, "AuctionProduct object should be created");
        assertEquals(50.0, auctionProduct.getCurrentHighestBid(), "Initial highest bid should match base price");
        assertEquals(7, auctionProduct.getMinutesToEnd(), "Minutes to end should match");
        assertEquals(1, auctionProduct.getProductID(), "Product ID should match");
        assertEquals(-1, auctionProduct.getUserIDHighestBid(), "No bids should be placed initially");
    }

    @Test
    void addBidFirst_HigherBid_ShouldSucceed() {
        int result = auctionProduct.addBid(101, 60.0);
        assertTrue(result == -1, "Bid should be accepted");
        assertEquals(60.0, auctionProduct.getCurrentHighestBid(), "Highest bid should be updated");
        assertEquals(101, auctionProduct.getUserIDHighestBid(), "User ID of highest bidder should match");
    }

    
    @Test
    void addBidNotFirst_HigherBid_ShouldSucceed() {
        int userFirst =101;
        int userNotFirst = 102;
        auctionProduct.addBid(userFirst, 60.0);
        int result = auctionProduct.addBid(userNotFirst, 65.0);

        assertTrue(result == userFirst, "Bid should be accepted");
        assertEquals(65.0, auctionProduct.getCurrentHighestBid(), "Highest bid should be updated");
        assertEquals(userNotFirst, auctionProduct.getUserIDHighestBid(), "User ID of highest bidder should match");
    }

    @Test
    void addBid_LowerBid_ShouldFail() {
        int userId = 101;
        int result = auctionProduct.addBid(userId, 40.0);

        assertTrue(result == userId, "Bid should be rejected");
        assertEquals(50.0, auctionProduct.getCurrentHighestBid(), "Highest bid should remain unchanged");
        assertEquals(-1, auctionProduct.getUserIDHighestBid(), "No highest bidder should be set");
    }

    @Test
    void setOwnersToApprove_ValidOwners_ShouldInitializeApproval() {
        auctionProduct.setOwnersToApprove(Arrays.asList(1, 2, 3));
        assertFalse(auctionProduct.isApprovedByAllOwners(), "Not all owners should approve initially");
    }

    @Test
    void setBidApprovedByOwners_AllOwnersApprove_ShouldSucceed() {
        auctionProduct.setOwnersToApprove(Arrays.asList(1, 2, 3));
        auctionProduct.setBidApprovedByOwners(1, true);
        auctionProduct.setBidApprovedByOwners(2, true);
        auctionProduct.setBidApprovedByOwners(3, true);

        assertTrue(auctionProduct.isApprovedByAllOwners(), "All owners should approve the bid");
    }

    @Test
    void setBidApprovedByOwners_NotAllOwnersApprove_ShouldFail() {
        auctionProduct.setOwnersToApprove(Arrays.asList(1, 2, 3));
        auctionProduct.setBidApprovedByOwners(1, true);
        auctionProduct.setBidApprovedByOwners(2, false);

        assertFalse(auctionProduct.isApprovedByAllOwners(), "Not all owners have approved the bid");
    }

    @Test
    void addMinutes_ValidMinutes_ShouldIncreaseMinutesToEnd() {
        auctionProduct.addMinutes(3);
        assertEquals(10, auctionProduct.getMinutesToEnd(), "Minutes to end should be increased");
    }

    @Test
    void addMinutes_InvalidMinutes_ShouldThrowException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> auctionProduct.addMinutes(0),
                "Expected addMinutes to throw for invalid input"
        );
        assertTrue(thrown.getMessage().contains("greater than 0"), "Exception message should match");
    }

    @Test
    void toDTO_ShouldReturnCorrectDTO() {
        StoreProductDTO dto = auctionProduct.toDTO(1001);
        assertNotNull(dto, "DTO object should be created");
        assertEquals(1, dto.getProductId(), "Product ID should match");
        assertEquals("p1", dto.getName(), "Product name should match");
        assertEquals(10.0, dto.getBasePrice(), "Base price should match");
        assertEquals(4, dto.getQuantity(), "Quantity should match");
        assertEquals(1001, dto.getStoreId(), "Store ID should match");
        
    }
}