package UnitTesting;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    void givenNoOrder_whenGetOrders_returnEmpty() {
        assertNotNull(registeredUser.getOrders(), "Orders should not be null");
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
        assertEquals(message, registeredUser.getMessagesFromUser().get(storeID),
                "Message should be received successfully");
    }

    @Test
    void givenOrderInOrders_whenGetOrders_returnTrue() {
        int orderID = 1;
        OrderDTO order = mock(OrderDTO.class);
        when(order.getOrderId()).thenReturn(orderID);
        registeredUser.saveOrder(order);
        assertEquals(order.getOrderId(), registeredUser.getOrders().get(orderID).getOrderId(),
                "Order should be retrieved successfully");
    }
    @Test
    void givenNoOrderInOrders_whenGetOrders_returnTrue() {
        assertEquals(0, registeredUser.getOrders().size(),
                "Orders should be empty initially");
    }
    @Test
    void givenValidOrder_whenSaveOrder_returnTrue() {
        OrderDTO order = mock(OrderDTO.class);
        int orderID = 1;
        when(order.getOrderId()).thenReturn(orderID);
        registeredUser.saveOrder(order);
        assertEquals(order, registeredUser.getOrders().get(orderID),
                "Order should be saved successfully");
    }
    @Test
    public void testSendMessageToStore_Sucssses() {
        registeredUser.sendMessageToStore(1, "Hello Store!");
        HashMap<Integer, String> messages = registeredUser.getMessagesFromUser();
        assertEquals("Hello Store!", messages.get(1));
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
    public void testSaveOrderAndGetOrders() {
        OrderDTO order = mock(OrderDTO.class);
        when(order.getOrderId()).thenReturn(123);
        registeredUser.saveOrder(order);
        assertEquals(order, registeredUser.getOrders().get(123));
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
        registeredUser.addMessageFromStore(new SimpleEntry<>(storeID, message));
        assertEquals(message, registeredUser.getMessagesFromStore().get(storeID));
    }

    @Test
    void testAddAuctionEndedMessageAndGetAuctionEndedMessages() {
        int storeID = 7;
        String message = "Auction ended!";
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(storeID, message));
        assertEquals(message, registeredUser.getAuctionEndedMessages().get(storeID));
    }

    @Test
    void testAssignmentMessagesAndGetAssignmentMessages() {
        int storeID = 3;
        String message = "Assignment!";
        registeredUser.AssignmentMessages(new SimpleEntry<>(storeID, message));
        assertEquals(message, registeredUser.getAssignmentMessages().get(storeID));
    }

    @Test
    void testGetAllMessagesCombinesAll() {
        int storeID1 = 1, storeID2 = 2, storeID3 = 3;
        registeredUser.addMessageFromStore(new SimpleEntry<>(storeID1, "store"));
        registeredUser.AssignmentMessages(new SimpleEntry<>(storeID2, "assign"));
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(storeID3, "auction"));
        var all = registeredUser.getAllMessages();
        assertEquals("store", all.get(storeID1));
        assertEquals("assign", all.get(storeID2));
        assertEquals("auction", all.get(storeID3));
    }

    @Test
    void testGetPasswordReturnsPassword() {
        assertEquals(password, registeredUser.getPassword());
    }
    @Test
    void testAddMultipleMessagesFromStore() {
        registeredUser.addMessageFromStore(new SimpleEntry<>(1, "msg1"));
        registeredUser.addMessageFromStore(new SimpleEntry<>(2, "msg2"));
        HashMap<Integer, String> messages = registeredUser.getMessagesFromStore();
        assertEquals("msg1", messages.get(1));
        assertEquals("msg2", messages.get(2));
    }

    @Test
    void testAddMultipleAuctionEndedMessages() {
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(1, "auction1"));
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(2, "auction2"));
        HashMap<Integer, String> messages = registeredUser.getAuctionEndedMessages();
        assertEquals("auction1", messages.get(1));
        assertEquals("auction2", messages.get(2));
    }
    @Test
    void testGetMessagesFromStore() {
        registeredUser.addMessageFromStore(new SimpleEntry<>(1, "store1"));
        registeredUser.addMessageFromStore(new SimpleEntry<>(2, "store2"));
        HashMap<Integer, String> messages = registeredUser.getMessagesFromStore();
        assertEquals(2, messages.size());
        assertEquals("store1", messages.get(1));
        assertEquals("store2", messages.get(2));
    }
    
    @Test
    void testGetAssignmentMessages() {
        registeredUser.AssignmentMessages(new SimpleEntry<>(10, "assign1"));
        registeredUser.AssignmentMessages(new SimpleEntry<>(20, "assign2"));
        HashMap<Integer, String> messages = registeredUser.getAssignmentMessages();
        assertEquals(2, messages.size());
        assertEquals("assign1", messages.get(10));
        assertEquals("assign2", messages.get(20));
    }
    
    @Test
    void testGetAuctionEndedMessages() {
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(100, "auction1"));
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(200, "auction2"));
        HashMap<Integer, String> messages = registeredUser.getAuctionEndedMessages();
        assertEquals(2, messages.size());
        assertEquals("auction1", messages.get(100));
        assertEquals("auction2", messages.get(200));
    }
    
    @Test
    void testGetAllMessagesCombinesAllTypes() {
        registeredUser.addMessageFromStore(new SimpleEntry<>(1, "storeMsg"));
        registeredUser.AssignmentMessages(new SimpleEntry<>(2, "assignMsg"));
        registeredUser.addAuctionEndedMessage(new SimpleEntry<>(3, "auctionMsg"));
        HashMap<Integer, String> all = registeredUser.getAllMessages();
        assertEquals(3, all.size());
        assertEquals("storeMsg", all.get(1));
        assertEquals("assignMsg", all.get(2));
        assertEquals("auctionMsg", all.get(3));
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
