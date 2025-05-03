package UnitTesting;

import java.time.LocalDate;

import DomainLayer.Model.Order;
import DomainLayer.Model.Registered;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.OrderDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class RegisteredTest {
    private Registered registeredUser;
    private String email = "email@com";
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


}
