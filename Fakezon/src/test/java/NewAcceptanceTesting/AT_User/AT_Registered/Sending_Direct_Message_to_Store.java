package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ApplicationLayer.Response;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.helpers.UserMsg;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class Sending_Direct_Message_to_Store {
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
/* 
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        Response<StoreProductDTO> resultAddProduct = systemService.addProductToStore(storeId, storeOwnerId, productName, productDescription, 1,100, category);
        assertTrue(resultAddProduct.isSuccess());
        productId = resultAddProduct.getData().getProductId();
*/ 
        Response<UserDTO> registered = testHelper.register_and_login2();
        registeredId = registered.getData().getUserId();
/* 
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");

        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
        */
    }

    @Test
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
    void testSendDirectMessageToStore_nullMessage_Failure() {
        Response<Void> response = systemService.sendMessageToStore(registeredId, storeId, null);
        assertFalse(response.isSuccess());
        assertEquals("Message cannot be empty", response.getMessage());

        Response<Map<Integer,UserMsg>> messagesResponse = systemService.getMessagesFromUsers(storeId, storeOwnerId);
        assertEquals("No messages found", messagesResponse.getMessage());
        assertFalse(messagesResponse.isSuccess());
    }

    @Test
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
