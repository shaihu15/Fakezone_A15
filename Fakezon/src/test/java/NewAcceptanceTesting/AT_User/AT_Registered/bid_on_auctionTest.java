package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;
import java.util.concurrent.TimeUnit;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;


@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class bid_on_auctionTest {

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
        testHelper = new TestHelper(systemService);

        Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login owner");
        storeOwnerId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(storeOwnerId, "AuctionTestStore");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        Response<StoreProductDTO> regularProductRes = systemService.addProductToStore(storeId, storeOwnerId,
                "Auction Item", "Item for auction", 10.0, 1, PCategory.AUTOMOTIVE.toString());
        assertTrue(regularProductRes.isSuccess(), "Failed to add regular product for auction setup");
        int regularProductId = regularProductRes.getData().getProductId();

        Response<Void> addAuctionProdRes = systemService.addAuctionProductToStore(storeId, storeOwnerId,
                regularProductId, initialBasePrice, 60);
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

    @AfterEach
    void tearDown() {
        // Remove auction product
        Response<Void> removeAuctionRes = systemService.removeProductFromStore(storeId, storeOwnerId, auctionProductId);
        assertTrue(removeAuctionRes.isSuccess(), "Failed to remove auction product");

        // Close store
        Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, storeOwnerId);
        assertTrue(closeStoreRes.isSuccess(), "Failed to close store");

        // Delete users
        Response<Boolean> deleteBuyer1Res = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteBuyer1Res.isSuccess(), "Failed to delete buyer1");

        Response<Boolean> deleteBuyer2Res = systemService.deleteUser(testHelper.validEmail3());
        assertTrue(deleteBuyer2Res.isSuccess(), "Failed to delete buyer2");

        Response<Boolean> deleteOtherUserRes = systemService.deleteUser(testHelper.validEmail4());
        assertTrue(deleteOtherUserRes.isSuccess(), "Failed to delete other user");

        Response<Boolean> deleteOwnerRes = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteOwnerRes.isSuccess(), "Failed to delete store owner");

        // Remove store
        Response<Void> removeStoreRes = systemService.removeStore(storeId, storeOwnerId);
        assertTrue(removeStoreRes.isSuccess(), "Failed to remove store");
    }

    @Test
    void testAddBid_Success_FirstBid() {
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                bidAmount);

        assertTrue(response.isSuccess(), "Expected first bid to succeed");
        assertEquals("Bid added successfully", response.getMessage());
    }

    @Test
    void testAddBid_Failure_BidTooLow_Initial() {
        double bidAmount = initialBasePrice - 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail as it's too low initially");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"),
                "Expected error message for too low bid");
    }

    @Test
    void testAddBid_Success_HigherBid() {
        double bid1Amount = initialBasePrice + 10.0;
        double bid2Amount = bid1Amount + 5.0;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                bid1Amount);
        assertTrue(response1.isSuccess(), "Expected first bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId,
                bid2Amount);

        assertTrue(response2.isSuccess(), "Expected higher bid to succeed");
        assertEquals("Bid added successfully", response2.getMessage());
    }

    @Test
    void testAddBid_Failure_LowerBidThanCurrentHighest() {
        double bid1Amount = initialBasePrice + 20.0;
        double bid2Amount = bid1Amount - 5.0;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                bid1Amount);
        assertTrue(response1.isSuccess(), "Expected initial high bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId,
                bid2Amount);

        assertFalse(response2.isSuccess(), "Expected bid to fail as it's lower than current highest");
        assertEquals(ErrorType.INTERNAL_ERROR, response2.getErrorType());
        assertTrue(response2.getMessage().contains("Error during adding bid to auction product in store"),
                "Expected error message for lower bid");
    }

    @Test
    void testAddBid_Failure_EqualBidToCurrentHighest() {
        double bid1Amount = initialBasePrice + 20.0;
        double bid2Amount = bid1Amount;

        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                bid1Amount);
        assertTrue(response1.isSuccess(), "Expected first bid to succeed");

        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId,
                bid2Amount);

        assertFalse(response2.isSuccess(), "Expected bid to fail as it's equal to current highest");
        assertEquals(ErrorType.INTERNAL_ERROR, response2.getErrorType());
        assertTrue(response2.getMessage().contains("Error during adding bid to auction product in store"),
                "Expected error message for equal bid");
    }

    @Test
    void testAddBid_Failure_InvalidProductId() {
        int invalidProductId = -1;
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, invalidProductId,
                bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail for invalid product ID");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"),
                "Expected error message for invalid product ID");
    }

    @Test
    void testAddBid_Failure_InvalidStoreId() {
        int invalidStoreId = -1;
        double bidAmount = initialBasePrice + 10.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(invalidStoreId, buyer1Id,
                auctionProductId, bidAmount);

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

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, notLoggedInUserId,
                auctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail for a user not logged in");
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddBid_Failure_ProductIsNotAuctionProduct() {
        Response<StoreProductDTO> regularProductRes = systemService.addProductToStore(storeId, storeOwnerId,
                "Regular Item", "Just a regular item", 50.0, 10, PCategory.ELECTRONICS.toString());
        assertTrue(regularProductRes.isSuccess(), "Failed to add regular product for test setup");
        int regularProductId = regularProductRes.getData().getProductId();

        double bidAmount = 60.0;

        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, regularProductId,
                bidAmount);

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
        Response<Void> response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id,
                expiredAuctionProductId, bidAmount);

        assertFalse(response.isSuccess(), "Expected bid to fail because auction has ended");
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding bid to auction product in store"),
                "Expected error message for ended auction");
    }

    @Test
    void testAddBid_Success_User2BidsHigherThanUser1(){
        double user1Bid = initialBasePrice + 15.0;
        double user2Bid = user1Bid + 10.0;

        // User1 places the first bid
        Response<Void> response1 = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId,
                user1Bid);
        assertTrue(response1.isSuccess(), "Expected user1's bid to succeed");
        assertEquals("Bid added successfully", response1.getMessage());

        // User2 places a higher bid
        Response<Void> response2 = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId,
                user2Bid);
        assertTrue(response2.isSuccess(), "Expected user2's higher bid to succeed");
        assertEquals("Bid added successfully", response2.getMessage());
        
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Sleep was interrupted");
        }

        Response<Map<Integer, StoreMsg>> messagesResponse = systemService.getAllMessages(buyer1Id);
        assertTrue(messagesResponse.isSuccess(), "Expected messages retrieval to succeed");
        assertTrue(messagesResponse.getData() != null && !messagesResponse.getData().isEmpty(),
                "Expected messages for user1 to be non-empty");
        Map<Integer, StoreMsg> messages = messagesResponse.getData();
        assertFalse(messages.isEmpty());
        assertTrue(messages.values().stream().anyMatch(msg -> msg.getStoreId() == storeId && msg.getProductId() == auctionProductId),
                "Expected messages to contain auction-related messages for user1");
        Optional<String> maybeOutbidMsg = messages.values().stream()
                .filter(msg -> msg.getStoreId() == storeId && msg.getProductId() == auctionProductId)
                .map(StoreMsg::getMessage)
                .filter(msg -> msg.contains("rejected due to a higher bid"))
                .findFirst();

        assertTrue(maybeOutbidMsg.isPresent(), "Expected outbid message to mention rejection due to a higher bid");
    }

    @Test
    void testAddBid_Concurrency_SameBidAmount() throws InterruptedException, ExecutionException {
        // Arrange
        double concurrentBidAmount = initialBasePrice + 50.0; // A high bid amount for both
        
        // Create a thread pool for concurrent execution
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Define tasks for each buyer to place the same bid
        Callable<Response<Void>> buyer1Task = () -> systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, concurrentBidAmount);
        Callable<Response<Void>> buyer2Task = () -> systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId, concurrentBidAmount);

        // Submit tasks
        Future<Response<Void>> future1 = executor.submit(buyer1Task);
        Future<Response<Void>> future2 = executor.submit(buyer2Task);

        // Get results
        Response<Void> result1 = future1.get();
        Response<Void> result2 = future2.get();

        // Shutdown the executor
        executor.shutdown();

        // Assert that exactly one of the bids succeeded
        // The expectation is that due to concurrency, one bid will be processed first,
        // making the second identical bid fail (as it's not higher than the now-current highest).
        assertTrue(result1.isSuccess() ^ result2.isSuccess(), "Exactly one of the concurrent bids should succeed.");

        // Identify which buyer succeeded
        int winningBuyerId = result1.isSuccess() ? buyer1Id : buyer2Id;
        int losingBuyerId = result1.isSuccess() ? buyer2Id : buyer1Id;

        // Verify the losing bid's error type and message
        Response<Void> losingResponse = result1.isSuccess() ? result2 : result1;
        assertFalse(losingResponse.isSuccess());
        // The error type might be INTERNAL_ERROR or INVALID_INPUT depending on the exact
        // implementation of how the system handles bids not being strictly higher.
        // Based on testAddBid_Failure_EqualBidToCurrentHighest, it's INTERNAL_ERROR.
        assertEquals(ErrorType.INTERNAL_ERROR, losingResponse.getErrorType());
        assertTrue(losingResponse.getMessage().contains("Error during adding bid to auction product in store"),
                   "Expected error message for the losing concurrent bid.");
    }
}