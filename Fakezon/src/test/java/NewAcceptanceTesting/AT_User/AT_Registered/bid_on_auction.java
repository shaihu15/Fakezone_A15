package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class bid_on_auction {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int storeOwnerId;
    private int storeId;
    private int auctionProductId;
    private double initialBasePrice = 100.0;
    private int buyer1Id;
    private int buyer2Id;
    private int otherRegisteredUserId;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        testHelper = new TestHelper(systemService);

        Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login owner");
        storeOwnerId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(storeOwnerId, "AuctionTestStore");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        Response<StoreProductDTO> regularProductRes = systemService.addProductToStore(storeId, storeOwnerId, "Auction Item", "Item for auction", 10.0, 1, PCategory.AUTOMOTIVE.toString());
        assertTrue(regularProductRes.isSuccess(), "Failed to add regular product for auction setup");
        int regularProductId = regularProductRes.getData().getProductId();

        Response<Void> addAuctionProdRes = systemService.addAuctionProductToStore(storeId, storeOwnerId, regularProductId, initialBasePrice, 60);
        assertTrue(addAuctionProdRes.isSuccess(), "Failed to add auction product");
        
        auctionProductId = regularProductId;

        Response<UserDTO> buyer1Res = testHelper.register_and_login2();
        assertTrue(buyer1Res.isSuccess(), "Failed to register and login buyer1");
        buyer1Id = buyer1Res.getData().getUserId();

        Response<UserDTO> buyer2Res = testHelper.register_and_login3();
        assertTrue(buyer2Res.isSuccess(), "Failed to register and login buyer2");
        buyer2Id = buyer2Res.getData().getUserId();

        Response<UserDTO> otherUserRes = testHelper.register_and_login4();
        assertTrue(otherUserRes.isSuccess(), "Failed to register and login other user");
        otherRegisteredUserId = otherUserRes.getData().getUserId();
    }

    @Test
    void testAddBid_Success_FirstBid() {
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, bidAmount);

        assertTrue(response.isSuccess(), "Expected first bid to succeed");
        assertEquals("Bid added successfully", response.getMessage());
    }

    @Test
    void testAddBid_Failure_BidTooLow_Initial() {
        double bidAmount = initialBasePrice - 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail as it's too low initially");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for too low bid");
    }

    @Test
    void testAddBid_Success_HigherBid() {
        double bid1Amount = initialBasePrice + 10.0;
        double bid2Amount = bid1Amount + 5.0;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, bid1Amount);
        assertTrue(response1.isSuccess(), "Expected first bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId, bid2Amount);

        assertTrue(response2.isSuccess(), "Expected higher bid to succeed");
        assertEquals("Bid added successfully", response2.getMessage());
    }

    @Test
    void testAddBid_Failure_LowerBidThanCurrentHighest() {
        double bid1Amount = initialBasePrice + 20.0;
        double bid2Amount = bid1Amount - 5.0;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, bid1Amount);
        assertTrue(response1.isSuccess(), "Expected initial high bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId, bid2Amount);

        assertFalse(response2.isSuccess(), "Expected bid to fail as it's lower than current highest");
        assertEquals(ErrorType.INTERNAL_ERROR, response2.getErrorType());
        assertTrue(response2.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for lower bid");
    }

    @Test
    void testAddBid_Failure_EqualBidToCurrentHighest() {
        double bid1Amount = initialBasePrice + 20.0;
        double bid2Amount = bid1Amount;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, bid1Amount);
        assertTrue(response1.isSuccess(), "Expected first bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId, bid2Amount);

        assertFalse(response2.isSuccess(), "Expected bid to fail as it's equal to current highest");
        assertEquals(ErrorType.INTERNAL_ERROR, response2.getErrorType());
        assertTrue(response2.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for equal bid");
    }

    @Test
    void testAddBid_Failure_InvalidProductId() {
        int invalidProductId = -1;
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, invalidProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail for invalid product ID");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for invalid product ID");
    }

    @Test
    void testAddBid_Failure_InvalidStoreId() {
        int invalidStoreId = -1;
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(invalidStoreId, buyer1Id, auctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail for invalid store ID");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for invalid store ID");
    }

    @Test
    void testAddBid_Failure_RequesterNotLoggedIn() {
        int notLoggedInUserId = otherRegisteredUserId;
        systemService.userLogout(notLoggedInUserId);
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, notLoggedInUserId, auctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail for a user not logged in");
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddBid_Failure_ProductIsNotAuctionProduct() {
        Response<StoreProductDTO> regularProductRes = systemService.addProductToStore(storeId, storeOwnerId, "Regular Item", "Just a regular item", 50.0, 10, PCategory.ELECTRONICS.toString());
        assertTrue(regularProductRes.isSuccess(), "Failed to add regular product for test setup");
        int regularProductId = regularProductRes.getData().getProductId();

        double bidAmount = 60.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, regularProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail because product is not an auction product");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message when product is not an auction type");
    }

    @Test
    void testAddBid_Failure_AuctionEnded() {
        systemService.addAuctionProductToStore(storeId, storeOwnerId, 2, initialBasePrice, 0); 
        int expiredAuctionProductId = 2;

        double bidAmount = initialBasePrice + 10.0;
        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, expiredAuctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail because auction has ended");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"), 
                   "Expected error message for ended auction");
    }
}