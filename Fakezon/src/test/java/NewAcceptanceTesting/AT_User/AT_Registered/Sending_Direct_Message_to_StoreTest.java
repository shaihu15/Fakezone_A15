package NewAcceptanceTesting.AT_User.AT_Registered;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.helpers.UserMsg;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")
public class Sending_Direct_Message_to_StoreTest {
    //Use-case: 3.5 Sending a Direct Message to a Store

     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int registeredId;
    int storeId;
    int productId;
    int storeOwnerId;

    @BeforeEach
   //@Test
    void setUp() {

        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);
        Response<UserDTO> storeOwner = testHelper.register_and_login();
        this.storeOwnerId = storeOwner.getData().getUserId();

        String storeName = "Test Store";
        Response<Integer> resultAddStore = systemService.addStore(storeOwnerId, storeName);
        assertTrue(resultAddStore.isSuccess());
        storeId = resultAddStore.getData();

        Response<UserDTO> registered = testHelper.register_and_login2();
        registeredId = registered.getData().getUserId();

    }

    @Test
    @Transactional
    void testSendDirectMessageToStore_validArguments_Success() {
        String message = "Hello, this is a test message!";
        Response<Void> response = systemService.sendMessageToStore(registeredId, storeId, message);
        assertTrue(response.isSuccess());
        assertEquals("Message sent successfully", response.getMessage());

        // Verify that the message was sent
        Response<Map<Integer,UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("Messages retrieved successfully", messagesResponse.getMessage());
        assertTrue(messagesResponse.isSuccess());
        Map<Integer,UserMsg> messages = messagesResponse.getData();
        assertTrue(messages.entrySet().stream()
                .anyMatch(entry -> entry.getValue().getMsg().equals(message) && entry.getValue().getUserId() == registeredId));
    }

    @Test
    @Transactional
    void testSendDirectMessageToStore_emptyMessage_Failure() {
        String message = "";
        Response<Void> response = systemService.sendMessageToStore(registeredId, storeId, message);
        assertFalse(response.isSuccess());
        assertEquals("Message cannot be empty", response.getMessage());

        Response<Map<Integer, UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("No messages found", messagesResponse.getMessage());
        assertFalse(messagesResponse.isSuccess());
    }

    @Test
    @Transactional    void testSendDirectMessageToStore_nullMessage_Failure() {
        Response<Void> response = systemService.sendMessageToStore(registeredId, storeId, null);
        assertFalse(response.isSuccess());
        assertEquals("Message cannot be empty", response.getMessage());

        Response<Map<Integer,UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("No messages found", messagesResponse.getMessage());
        assertFalse(messagesResponse.isSuccess());
    }
    @Test
    @Transactional
    void testSendDirectMessageToStore_invalidStoreId_Failure() {
        String message = "Hello, this is a test message!";
        int invalidStoreId = 9999; // Assuming this store ID does not exist
        Response<Void> response = systemService.sendMessageToStore(registeredId, invalidStoreId, message);
        assertFalse(response.isSuccess());
        assertEquals("Error during sending message to store: Store not found", response.getMessage());

        Response<Map<Integer, UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("No messages found", messagesResponse.getMessage());
        assertFalse(messagesResponse.isSuccess());
    }

    @Test
    @Transactional
    void testSendDirectMessageToStore_userNotRegistered_Failure() {
        String message = "Hello, this is a test message!";
        int unregisteredUserId = 9999; // Assuming this user ID does not exist
        Response<Void> response = systemService.sendMessageToStore(unregisteredUserId, storeId, message);
        assertFalse(response.isSuccess());
        assertEquals("Error during sending message to store: User not found", response.getMessage());

        Response<Map<Integer, UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("No messages found", messagesResponse.getMessage());
        assertFalse(messagesResponse.isSuccess());
    }
/* 
    @Test
    void testSendDirectMessageToStore_userDidntPurchesStore_Success() {
        Response<UserDTO> unregisteredUser = testHelper.register_and_login3();
        int newUserId = unregisteredUser.getData().getUserId();
        String message = "Hello, this is a test message!";
        Response<Void> response = systemService.sendMessageToStore(newUserId, storeId, message);
        assertTrue(response.isSuccess());
        assertEquals("Message sent successfully", response.getMessage());

                // Verify that the message was sent
        Response<Map<Integer, UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("Messages retrieved successfully", messagesResponse.getMessage());
        assertTrue(messagesResponse.isSuccess());
        Map<Integer, UserMsg> messages = messagesResponse.getData();
        assertTrue(messages.entrySet().stream()
                .anyMatch(entry -> entry.getValue().getMsg().equals(message) && entry.getValue().getUserId() == newUserId));
    }
*/
}
