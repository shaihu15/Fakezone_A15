package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_Responding_to_User_InquiriesTest {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int storeOwnerId;
    private int customerId;
    private int storeId;

    @BeforeEach
    void setUp() {
        systemService.clearAllData(); // Clear data before each test to ensure isolation
        testHelper = new TestHelper(systemService);

        // Register store owner
        Response<UserDTO> storeOwnerResp = testHelper.register_and_login();
        assertTrue(storeOwnerResp.isSuccess());
        storeOwnerId = storeOwnerResp.getData().getUserId();

        // Create store
        Response<Integer> storeResp = systemService.addStore(storeOwnerId, "StoreTest");
        assertTrue(storeResp.isSuccess());
        storeId = storeResp.getData();

        // Register another user
        Response<UserDTO> customerResp = testHelper.register_and_login2();
        assertTrue(customerResp.isSuccess());
        customerId = customerResp.getData().getUserId();

        // Customer sends inquiry
        Response<Void> sendMsgResp = systemService.sendMessageToStore(customerId, storeId, "Is this product vegan?");
        assertTrue(sendMsgResp.isSuccess());

    }
    
    @Test
    void testRespondingtoUserInquiries_Success() throws InterruptedException {
        // Store owner sends reply to customer
        String replyMessage = "Yes, the product is 100% vegan.";
        Response<Void> replyResp = systemService.sendMessageToUser(storeOwnerId, storeId, customerId, replyMessage);

        assertTrue(replyResp.isSuccess());
        assertEquals("Message sent successfully", replyResp.getMessage());

        TimeUnit.SECONDS.sleep(2); // Small buffer
        // Simulate customer checking messages
        Response<Map<Integer, StoreMsg>> inboxResp = systemService.getAllMessages(customerId);
        assertTrue(inboxResp.isSuccess());
        boolean found = inboxResp.getData().values().stream().anyMatch(m -> m.getMessage().contains("vegan"));
        assertTrue(found, "Expected reply to be found in customer inbox.");
    }

    @Test
    void testRespondingtoUserInquiries_emptyMessage_Failue() {
        // Store owner sends an empty message
        String emptyMessage = "   ";
        Response<Void> replyResp = systemService.sendMessageToUser(storeOwnerId, storeId, customerId, emptyMessage);

        assertFalse(replyResp.isSuccess());
        assertEquals("Message cannot be empty", replyResp.getMessage());
    }

    @Test
    void testRespondingtoUserInquiries_invalidStore_Failure() {
        // Store owner tries to send a message to a non-existent store
        int invalidStoreId = 9999; // Assuming this store ID does not exist
        String replyMessage = "This store does not exist.";
        Response<Void> replyResp = systemService.sendMessageToUser(storeOwnerId, invalidStoreId, customerId, replyMessage);

        assertFalse(replyResp.isSuccess());
        assertEquals("Error during sending message to user: Store not found", replyResp.getMessage());
    }

    @Test
    void testRespondingtoUserInquiries_invalidCustomer_Success() {
        // Store owner tries to send a message to a non-existent customer
        int invalidCustomerId = 9999; // Assuming this user ID does not exist
        String replyMessage = "This customer does not exist.";
        Response<Void> replyResp = systemService.sendMessageToUser(storeOwnerId, storeId, invalidCustomerId, replyMessage);

        assertTrue(replyResp.isSuccess());
        assertEquals("Message sent successfully", replyResp.getMessage());
    }

}
