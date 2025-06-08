package UnitTesting;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.UserDTO;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.Model.Registered;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.helpers.StoreMsg;

public class RegisteredTest {
    private Registered registeredUser;
    private String email = "email@gmail.com";
    private String password = "password1234";
    private int userID;
    private String country = "IL";

    @BeforeEach
    void setUp() {
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1); // Example date of birth
        registeredUser = new Registered(email, password, dateOfBirth,country);
        userID = registeredUser.getUserId();
    }

    @Test
    void givenValidUser_whenIsRegistered_returnTrue() {
        assertTrue(registeredUser.isRegistered(), "Registered user should be registered");
    }

    @Test
    void givenValidUser_whenLogout_returnTrue() {
        assertTrue(registeredUser.logout(), "Logout should be successful");
        assertFalse(registeredUser.isLoggedIn(), "User should not be logged in after logout");
    }

    @Test
    void givenValidUser_whenGetEmail_returnTrue() {
        assertEquals(email, registeredUser.getEmail(), "Email should match the one set during registration");
    }

    @Test
    void givenNonExistingPurchaseStore_whenDidPurchaseStore_ShouldFalse() {
        assertFalse(registeredUser.didPurchaseStore(1), "User should not have purchased any store initially");
    }

    @Test
    void givenValidStoreID_whenAddRole_ThenRolesAreUpdated() {
        int storeID = 1;
        StoreManager role = mock(StoreManager.class);
        registeredUser.addRole(storeID, role);
        assertNotNull(registeredUser.getRoleByStoreID(storeID), "Role should be added successfully");
    }

    @Test
    void givenValidStoreID_whenRemoveRole_ShouldSucceed() {
        int storeID = 1;
        StoreManager role = mock(StoreManager.class);
        registeredUser.addRole(storeID, role);
        registeredUser.removeRole(storeID);
        assertNull(registeredUser.getRoleByStoreID(storeID), "Role should be removed successfully");
    }

    @Test
    void givenInvalidStoreID_whenRemoveRole_ShouldThrow() {
        int storeID = 1;
        StoreManager role = mock(StoreManager.class);
        registeredUser.addRole(storeID, role);
        registeredUser.removeRole(storeID);
        assertThrows(IllegalArgumentException.class, () -> registeredUser.removeRole(storeID),
                "Should throw exception for non-existing role");
    }

    @Test
    void givenRoles_whenGetRoleByStoreID_returnRole() {
        int storeID = 1;
        StoreManager role = mock(StoreManager.class);
        registeredUser.addRole(storeID, role);
        assertEquals(role, registeredUser.getRoleByStoreID(storeID),
                "Should return the correct role for the given store ID");
    }

    @Test
    void givenRoles_whenGetAllRoles_returnRoles() {
        int storeID1 = 1;
        int storeID2 = 2;
        StoreManager role1 = mock(StoreManager.class);
        StoreOwner role2 = mock(StoreOwner.class);
        registeredUser.addRole(storeID1, role1);
        registeredUser.addRole(storeID2, role2);
        assertEquals(2, registeredUser.getAllRoles().size(), "Should return all roles");
    }

    @Test
    void givenLogedoutUser_isLoggedIn_returnFalse() {
        registeredUser.login();
        assertTrue(registeredUser.isLoggedIn(), "User should be logged in initially");
        registeredUser.logout();
        assertFalse(registeredUser.isLoggedIn(), "User should not be logged in after logout");
    }

    @Test
    void givenNonExistingPurchaseProduct_whenDidPurchaseProduct_ReturnFalse() {
        int storeID = 1;
        int productID = 1;
        assertFalse(registeredUser.didPurchaseProduct(storeID, productID),
                "User should not have purchased any product initially");
    }

    @Test
    void givenValidPurchaseProduct_whenDidPurchaseProduct_ReturnTrue() {
        int storeID = 1;
        int productID = 1;
        List<Integer> purchasedProducts = List.of(productID);
        registeredUser.setproductsPurchase(storeID, purchasedProducts);
        assertTrue(registeredUser.didPurchaseProduct(storeID, productID), "User should have purchased the product");
    }


    @Test
    void givenValidMessageFromUser_whenGetMessagesFromUser_returnTrue() {
        int storeID = 1;
        String message = "Hello, this is a test message.";
        registeredUser.sendMessageToStore(storeID, message);
        assertEquals(message, registeredUser.getMessagesFromUser().get(0).getMessage(),
                "Message should be received successfully");
    }


    @Test
    public void testSendMessageToStore_Success() {
        registeredUser.sendMessageToStore(1, "Hello Store!");
        List<StoreMsg> messages = registeredUser.getMessagesFromUser();
        assertEquals("Hello Store!", messages.get(0).getMessage());
    }
    @Test
    public void testAddRoleAndGetRole() {
        IRegisteredRole role = mock(IRegisteredRole.class);
        registeredUser.addRole(1, role);
        assertEquals(role, registeredUser.getRoleByStoreID(1));
    }
        @Test
    public void testRemoveRole() {
        IRegisteredRole role = mock(IRegisteredRole.class);
        registeredUser.addRole(1, role);
        registeredUser.removeRole(1);
        assertNull(registeredUser.getRoleByStoreID(1));
    }

    @Test
    public void testRemoveRoleThrowsIfMissing() {
        assertThrows(IllegalArgumentException.class, () -> {
            registeredUser.removeRole(999);
        });
    }

    @Test
    public void testDidPurchaseProductAndStore() {
        registeredUser.setproductsPurchase(1, List.of(101, 102));
        assertTrue(registeredUser.didPurchaseStore(1));
        assertTrue(registeredUser.didPurchaseProduct(1, 101));
        assertFalse(registeredUser.didPurchaseProduct(1, 999));
    }

    @Test
    public void testToDTO() {
        UserDTO dto = registeredUser.toDTO();
        assertEquals("email@gmail.com", dto.getUserEmail());
    }
    @Test
    void testAddMessageFromStoreAndGetMessagesFromStore() {
        int storeID = 5;
        String message = "Store message!";
        int messageId = registeredUser.addMessageFromStore(new StoreMsg(storeID, -1, message));
        assertEquals(message, registeredUser.getMessagesFromStore().get(messageId).getMessage());
    }

    @Test
    void testAddAuctionEndedMessageAndGetAuctionEndedMessages() {
        int storeID = 7;
        String message = "Auction ended!";
        int messageId = registeredUser.addOfferMessage(new StoreMsg(storeID, -1, message));
        assertEquals(message, registeredUser.getOffersMessages().get(messageId).getMessage());
    }

    @Test
    void testAssignmentMessagesAndGetAssignmentMessages() {
        int storeID = 3;
        String message = "Assignment!";
        int messageId = registeredUser.addAssignmentMessage(new StoreMsg(storeID, -1, message));
        assertEquals(message, registeredUser.getAssignmentMessages().get(messageId).getMessage());
    }

    @Test
    void testGetAllMessagesCombinesAll() {
        int storeID1 = 1, storeID2 = 2, storeID3 = 3;
        int messageId1 = registeredUser.addMessageFromStore(new StoreMsg(storeID1, -1, "store"));
        int messageId2 = registeredUser.addAssignmentMessage(new StoreMsg(storeID2, -1, "assign"));
        int messageId3 = registeredUser.addOfferMessage(new StoreMsg(storeID3, -1, "auction"));
        var all = registeredUser.getAllMessages();
        assertEquals("store", all.get(messageId1).getMessage());
        assertEquals("assign", all.get(messageId2).getMessage());
        assertEquals("auction", all.get(messageId3).getMessage());
    }

    @Test
    void testGetPasswordReturnsPassword() {
        assertEquals(password, registeredUser.getPassword());
    }
    @Test
    void testAddMultipleMessagesFromStore() {
        int messageId1 = registeredUser.addMessageFromStore(new StoreMsg(1, -1, "msg1"));
        int messageId2 = registeredUser.addMessageFromStore(new StoreMsg(2, -1, "msg2"));
       Map<Integer, StoreMsg> messages = registeredUser.getMessagesFromStore();
        assertEquals("msg1", messages.get(messageId1).getMessage());
        assertEquals("msg2", messages.get(messageId2).getMessage());
    }

    @Test
    void testAddMultipleAuctionEndedMessages() {
        int messageId1 = registeredUser.addOfferMessage(new StoreMsg(1, -1, "auction1"));
        int messageId2 = registeredUser.addOfferMessage(new StoreMsg(2, -1, "auction2"));
        Map<Integer, StoreMsg> messages = registeredUser.getOffersMessages();
        assertEquals("auction1", messages.get(messageId1).getMessage());
        assertEquals("auction2", messages.get(messageId2).getMessage());
    }
    @Test
    void testGetMessagesFromStore() {
        int messageId1 = registeredUser.addMessageFromStore(new StoreMsg(1, -1, "store1"));
        int messageId2 = registeredUser.addMessageFromStore(new StoreMsg(2, -1, "store2"));
        Map<Integer, StoreMsg> messages = registeredUser.getMessagesFromStore();
        assertEquals(2, messages.size());
        assertEquals("store1", messages.get(messageId1).getMessage());
        assertEquals("store2", messages.get(messageId2).getMessage());
    }
    
    @Test
    void testGetAssignmentMessages() {
        int messageId1 = registeredUser.addAssignmentMessage(new StoreMsg(10, -1, "assign1"));
        int messageId2 = registeredUser.addAssignmentMessage(new StoreMsg(20, -1, "assign2"));
        Map<Integer, StoreMsg> messages = registeredUser.getAssignmentMessages();
        assertEquals(2, messages.size());
        assertEquals("assign1", messages.get(messageId1).getMessage());
        assertEquals("assign2", messages.get(messageId2).getMessage());
    }
    
    @Test
    void testGetAuctionEndedMessages() {
        int messageId1 = registeredUser.addOfferMessage(new StoreMsg(100, -1, "auction1"));
        int messageId2 = registeredUser.addOfferMessage(new StoreMsg(200, -1, "auction2"));
        Map<Integer, StoreMsg> messages = registeredUser.getOffersMessages();
        assertEquals(2, messages.size());
        assertEquals("auction1", messages.get(messageId1).getMessage());
        assertEquals("auction2", messages.get(messageId2).getMessage());
    }
    
    @Test
    void testGetAllMessagesCombinesAllTypes() {
        int messageId1 = registeredUser.addMessageFromStore(new StoreMsg(1, -1, "storeMsg"));
        int messageId2 = registeredUser.addAssignmentMessage(new StoreMsg(2, -1, "assignMsg"));
        int messageId3 = registeredUser.addOfferMessage(new StoreMsg(3, -1, "auctionMsg"));
        Map<Integer, StoreMsg> all = registeredUser.getAllMessages();
        assertEquals(3, all.size());
        assertEquals("storeMsg", all.get(messageId1).getMessage());
        assertEquals("assignMsg", all.get(messageId2).getMessage());
        assertEquals("auctionMsg", all.get(messageId3).getMessage());
    }
    @Test
    void testSaveCartOrderAndDeleteIt_SingleStoreSingleProduct() {
        // Arrange
        int storeId = 1;
        int productId = 101;
        int quantity = 2;

        // Add product to the user's actual cart using addToBasket
        registeredUser.addToBasket(storeId, productId, quantity);

        // Act
        registeredUser.saveCartOrderAndDeleteIt();

        // Assert
        assertTrue(registeredUser.didPurchaseStore(storeId), "User should have purchased from this store after saving cart.");
        assertTrue(registeredUser.didPurchaseProduct(storeId, productId), "User should have purchased this product after saving cart.");
        // Verify cart is empty. Assuming Registered has a getCart() method that exposes the internal Cart.
        assertTrue(registeredUser.getCart().getAllProducts().isEmpty(), "Cart should be empty after save and delete.");
    }

    @Test
    void testSaveCartOrderAndDeleteIt_MultipleStoresMultipleProducts() {
        // Arrange
        int storeId1 = 1;
        int productId1_1 = 101;
        int productId1_2 = 102;
        int quantity1_1 = 1;
        int quantity1_2 = 3;

        int storeId2 = 2;
        int productId2_1 = 201;
        int quantity2_1 = 5;

        // Add products to the user's actual cart using addToBasket
        registeredUser.addToBasket(storeId1, productId1_1, quantity1_1);
        registeredUser.addToBasket(storeId1, productId1_2, quantity1_2);
        registeredUser.addToBasket(storeId2, productId2_1, quantity2_1);

        // Act
        registeredUser.saveCartOrderAndDeleteIt();

        // Assert
        assertTrue(registeredUser.didPurchaseStore(storeId1), "User should have purchased from store 1.");
        assertTrue(registeredUser.didPurchaseProduct(storeId1, productId1_1), "User should have purchased product 1.1.");
        assertTrue(registeredUser.didPurchaseProduct(storeId1, productId1_2), "User should have purchased product 1.2.");

        assertTrue(registeredUser.didPurchaseStore(storeId2), "User should have purchased from store 2.");
        assertTrue(registeredUser.didPurchaseProduct(storeId2, productId2_1), "User should have purchased product 2.1.");

        assertTrue(registeredUser.getCart().getAllProducts().isEmpty(), "Cart should be empty after save and delete.");
    }

    @Test
    void testSaveCartOrderAndDeleteIt_EmptyCart() {
        // Arrange - Cart is already empty by default in setUp
        // No products added to cart

        // Act
        registeredUser.saveCartOrderAndDeleteIt();

        // Assert
        assertFalse(registeredUser.didPurchaseStore(1), "User should not have purchased store 1 if cart was empty.");
        assertFalse(registeredUser.didPurchaseProduct(1, 101), "User should not have purchased product 101 if cart was empty.");
        assertTrue(registeredUser.getCart().getAllProducts().isEmpty(), "Cart should still be empty if it started empty.");
    }

    @Test
    void testSaveCartOrderAndDeleteIt_StoreAlreadyHasPurchases() {
        // Arrange
        int existingStoreId = 5;
        int existingProductId = 505;
        // Directly set a previous purchase to ensure it's not overwritten
        registeredUser.setproductsPurchase(existingStoreId, new ArrayList<>(List.of(existingProductId)));

        int newStoreId = 1;
        int newProductId = 101;
        int quantity = 1;

        // Add new product to the user's actual cart using addToBasket
        registeredUser.addToBasket(newStoreId, newProductId, quantity);

        // Act
        registeredUser.saveCartOrderAndDeleteIt();

        // Assert
        assertTrue(registeredUser.didPurchaseStore(existingStoreId), "Existing store purchase should remain.");
        assertTrue(registeredUser.didPurchaseProduct(existingStoreId, existingProductId), "Existing product purchase should remain.");

        assertTrue(registeredUser.didPurchaseStore(newStoreId), "New store purchase should be added.");
        assertTrue(registeredUser.didPurchaseProduct(newStoreId, newProductId), "New product purchase should be added.");

        assertTrue(registeredUser.getCart().getAllProducts().isEmpty(), "Cart should be empty after save and delete.");
    }
}
