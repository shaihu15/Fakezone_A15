package UnitTesting;

import DomainLayer.Model.Guest;
import DomainLayer.Model.StoreOwner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GuestTest {
    private Guest guestUser;

    @BeforeEach
    void setUp() {
        guestUser = new Guest();
    }

    @Test
    void testIsRegistered() {
        assertFalse(guestUser.isRegistered(), "Guest user should not be registered");
    }

    @Test
    void testLogout() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.logout();
        }, "Guest cannot be logged out");
    }
    @Test
    void testAddRole() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.addRole(1, mock(StoreOwner.class));
        }, "Guest cannot have role");
    }
    @Test
    void testRemoveRole() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.removeRole(1);
        }, "Guest cannot have role");
    }
    @Test
    void testGetRoleByStoreID() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getRoleByStoreID(1);
        }, "Guest cannot have role");
    }
    @Test
    void testGetAllRoles() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getAllRoles();
        }, "Guest cannot have role");
    }
    @Test
    void testGetUserID() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getUserID();
        }, "Guest cannot have user ID");
    }
    @Test
    void testGetEmail() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getEmail();
        }, "Guest cannot have email");
    }
    @Test
    void testDidPurchaseStore() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.didPurchaseStore(1);
        }, "Guest doesn't have purchase history");
    }
    @Test
    void testDidPurchaseProduct() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.didPurchaseProduct(1, 1);
        }, "Guest doesn't have purchase history");
    }
    @Test
    void testGetOrders() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getOrders();
        }, "Guest doesn't have orders");
    }
    @Test
    void testIsLoggedIn() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.isLoggedIn();
        }, "Guest cannot be logged in");
    }


    
}
