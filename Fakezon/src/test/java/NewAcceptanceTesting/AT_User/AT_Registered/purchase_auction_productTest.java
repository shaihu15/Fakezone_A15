package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class purchase_auction_productTest {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int storeOwnerId;
    private int storeId;
    private int auctionProductId;
    private double initialBasePrice = 100.0;
    private int buyer1Id;
    private int buyer2Id;
    private int regularProductId; // To be used for auction setup

    @BeforeEach
    void setUp() {
        systemService.clearAllData();

        testHelper = new TestHelper(systemService);

        Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login owner");
        storeOwnerId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(storeOwnerId, "AuctionPurchaseTestStore");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        Response<StoreProductDTO> regularProductRes = systemService.addProductToStore(storeId, storeOwnerId,
                "Auction Item for Purchase", "Item for auction purchase", 10.0, 1, PCategory.AUTOMOTIVE.toString());
        assertTrue(regularProductRes.isSuccess(), "Failed to add regular product for auction setup");
        regularProductId = regularProductRes.getData().getProductId();

        // Convert regular product to auction product with 1 minute duration
        Response<Void> addAuctionProdRes = systemService.addAuctionProductToStore(storeId, storeOwnerId,
                regularProductId, initialBasePrice, 1); // 1 minute duration
        assertTrue(addAuctionProdRes.isSuccess(), "Failed to add auction product");

        auctionProductId = regularProductId; // The product is now an auction product

        Response<UserDTO> buyer1Res = testHelper.register_and_login2();
        assertTrue(buyer1Res.isSuccess(), "Failed to register and login buyer1");
        buyer1Id = buyer1Res.getData().getUserId();

        Response<UserDTO> buyer2Res = testHelper.register_and_login3();
        assertTrue(buyer2Res.isSuccess(), "Failed to register and login buyer2");
        buyer2Id = buyer2Res.getData().getUserId();
    }


    @Test
    void testPurchaseAuctionProduct_SuccessScenario() throws InterruptedException {
        double buyer1Bid = initialBasePrice + 10.0;
        double buyer2Bid = buyer1Bid + 20.0; // Higher bid

        // User1 places a bid and logs out
        Response<Void> bid1Response = systemService.addBidOnAuctionProductInStore(storeId, buyer1Id, auctionProductId, buyer1Bid);
        assertTrue(bid1Response.isSuccess(), "Buyer1's bid should succeed");
        systemService.userLogout(buyer1Id); // User1 logs out

        // User2 places a higher bid
        Response<Void> bid2Response = systemService.addBidOnAuctionProductInStore(storeId, buyer2Id, auctionProductId, buyer2Bid);
        assertTrue(bid2Response.isSuccess(), "Buyer2's higher bid should succeed");

        // Wait for the auction to end (1 minute + buffer)
        TimeUnit.MINUTES.sleep(1);
        TimeUnit.SECONDS.sleep(10); // Small buffer
        
        // Check ended auction message for store owner
        Response<Map<Integer, StoreMsg>> ownerMessages = systemService.getAllMessages(storeOwnerId);
        assertTrue(ownerMessages.isSuccess(), "Store owner should be able to retrieve messages");
        assertFalse(ownerMessages.getData().isEmpty(), "Store owner should have messages");
        assertTrue(ownerMessages.getData().values().stream().anyMatch(msg -> msg.getMessage().contains("Auction ended for product " + auctionProductId + ". Highest bid was " + buyer2Bid +
                            " by user " + buyer2Id + ".")),
                "Store owner should receive a message indicating the auction has ended");
       
        // Store owner accepts the winning bid
        //assertEquals("Auction response sent successfully", ownerAcceptResponse.getMessage());

        // User1 logs back in and checks messages - should see that they lost
        Response<UserDTO> buyer1LoginResponse = testHelper.login2();
        Response<Map<Integer, StoreMsg>> buyer1Messages = systemService.getAllMessages(buyer1Id);
        assertTrue(buyer1Messages.isSuccess(), "Buyer1 should be able to retrieve messages");
        assertFalse(buyer1Messages.getData().isEmpty(), "Buyer1 should have messages");
        assertTrue(buyer1Messages.getData().values().stream().anyMatch(msg -> msg.getMessage().contains("rejected due to a higher bid")),
                "Buyer1 should receive a message indicating their bid was rejected due to a higher bid");

        // User2 checks messages - should see that they won and need to purchase
        Response<Map<Integer, StoreMsg>> buyer2Messages = systemService.getAllMessages(buyer2Id);
        assertTrue(buyer2Messages.isSuccess(), "Buyer2 should be able to retrieve messages");
        assertFalse(buyer2Messages.getData().isEmpty(), "Buyer2 should have messages");
        assertTrue(buyer2Messages.getData().values().stream().anyMatch(msg -> msg.getMessage().contains("We are pleased to inform you that your bid has won the auction on product: "+auctionProductId)),
                "Buyer2 should receive a message indicating they won the auction and need to purchase");

        // User2 purchases the product from their cart
        Response<List<CartItemInfoDTO>> buyer2Cart = systemService.viewCart(buyer2Id);
        assertTrue(buyer2Cart.isSuccess(), "Buyer2 should be able to view their cart");
        assertFalse(buyer2Cart.getData().isEmpty(), "Buyer2's cart should not be empty after winning auction");
        assertEquals(1, buyer2Cart.getData().size(), "Buyer2's cart should contain one item (the auction product)");
        assertEquals(auctionProductId, buyer2Cart.getData().get(0).getProductId(), "The product in cart should be the auction product");

        Response<String> purchaseResponse = systemService.purchaseCart(buyer2Id, testHelper.validCountry(),
                LocalDate.of(1990, 1, 1), PaymentMethod.CREDIT_CARD, "Standard Delivery",
                "1234567890123456", "John Doe", "12/25", "123", "123 Main St* City* Country* 0000",
                "Jane Doe", "Auction Product Delivery");
        assertTrue(purchaseResponse.isSuccess(), "Buyer2 should be able to purchase the won auction product");

        // Verify that the order exists for buyer2
        Response<List<OrderDTO>> buyer2Orders = systemService.getOrdersByUserId(buyer2Id);
        assertTrue(buyer2Orders.isSuccess(), "Buyer2 should have an order record");
        assertFalse(buyer2Orders.getData().isEmpty(), "Buyer2's order list should not be empty");
        OrderDTO order = buyer2Orders.getData().get(0);
        order.getProducts().forEach(product -> {
        assertTrue(product.getProductId() == auctionProductId, "Order should contain the auction product");
        });
        assertEquals(storeId, order.getStoreId(), "Order should be from the correct store");
}

}
