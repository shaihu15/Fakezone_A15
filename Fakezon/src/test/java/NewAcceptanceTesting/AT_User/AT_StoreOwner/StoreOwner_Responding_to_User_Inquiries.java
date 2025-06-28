package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;


@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")
public class StoreOwner_Responding_to_User_Inquiries {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int storeOwnerId;
    private int customerId;
    private int storeId;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() {
        // Clean up: remove store and delete users
        Response<String> closeStoreResp = systemService.closeStoreByFounder(storeId, storeOwnerId);
        assertTrue(closeStoreResp.isSuccess());

        Response<Boolean> deleteCustomerResp = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteCustomerResp.isSuccess());

        Response<Boolean> deleteStoreOwnerResp = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteStoreOwnerResp.isSuccess());
        // Optionally, you can also delete any additional users created in tests
        Response<Boolean> deleteFifthUserResp = systemService.deleteUser(testHelper.validEmail5());
        if (!deleteFifthUserResp.isSuccess()) {
            String msg = deleteFifthUserResp.getMessage();
            assertTrue(
                msg.equals("User not found") || msg.equals("Error during deleting user"),
                "Unexpected delete user message: " + msg
            );
        }
        // remove store if it exists
        Response<Void> removeStoreResp = systemService.removeStore(storeId, storeOwnerId);
        if (!removeStoreResp.isSuccess()) {
            String msg = removeStoreResp.getMessage();
            assertTrue(
                msg.equals("Store not found") || msg.equals("Error during removing store"),
                "Unexpected remove store message: " + msg
            );
        }
    }

    @Test
    void testRespondingtoUserInquiries_Success() {
        // Store owner sends reply to customer
        String replyMessage = "Yes, the product is 100% vegan.";
        Response<Void> replyResp = systemService.sendMessageToUser(storeOwnerId, storeId, customerId, replyMessage);

        assertTrue(replyResp.isSuccess());
        assertEquals("Message sent successfully", replyResp.getMessage());

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