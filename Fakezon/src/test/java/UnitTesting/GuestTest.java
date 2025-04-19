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
    void givenNonValidUser_whenIsRegistered_returnFalse() {
        assertFalse(guestUser.isRegistered(), "Guest user should not be registered");
    }

    @Test
    void givenNonValidUser_whenLogout_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.logout();
        }, "Guest cannot be logged out");
    }
    @Test
    void givenNonValidUser_whenAddRole_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.addRole(1, mock(StoreOwner.class));
        }, "Guest cannot have role");
    }
    @Test
    void givenNonValidUser_whenRemoveRole_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.removeRole(1);
        }, "Guest cannot have role");
    }
    @Test
    void givenNonValidUser_whenGetRoleByStoreID_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getRoleByStoreID(1);
        }, "Guest cannot have role");
    }
    @Test
    void givenNonValidUser_whenGetAllRoles_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getAllRoles();
        }, "Guest cannot have role");
    }
    @Test
    void givenNonValidUser_whenGetUserID_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getUserID();
        }, "Guest cannot have user ID");
    }
    @Test
    void givenNonValidUser_whenGetEmail_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getEmail();
        }, "Guest cannot have email");
    }
    @Test
    void givenNonValidUser_whenDidPurchaseStore_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.didPurchaseStore(1);
        }, "Guest doesn't have purchase history");
    }
    @Test
    void givenNonValidUser_whenDidPurchaseProduct_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.didPurchaseProduct(1, 1);
        }, "Guest doesn't have purchase history");
    }
    @Test
    void givenNonValidUser_whenGetOrders_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.getOrders();
        }, "Guest doesn't have orders");
    }
    @Test
    void givenNonValidUser_whenIsLoggedIn_shouldThrow() {
        assertThrows(UnsupportedOperationException.class, () -> {
            guestUser.isLoggedIn();
        }, "Guest cannot be logged in");
    }


    
}
