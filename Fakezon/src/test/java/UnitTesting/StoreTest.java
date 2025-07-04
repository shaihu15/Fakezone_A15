package UnitTesting;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.Condition;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.AuctionProduct;
import DomainLayer.Model.PurchasePolicy;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreProduct;
import DomainLayer.Model.helpers.UserMsg;
import DomainLayer.Model.AndDiscount;

import DomainLayer.Model.Cart;
public class StoreTest {
    private Store store;
    private int founderId = 10;
    private int storeId = 1;
    private int managerId = 3;
    private ApplicationEventPublisher publisher;
    private int productId = 10;
    private int nonExistingProductId = 99;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        store = new Store("Test Store", founderId, publisher);

        StoreProductDTO storeProductDTO = store.addStoreProduct(founderId ,productId, "Test Product", 100.0, 5, PCategory.ELECTRONICS);

    }

    @Test
    void closeStore_AsFounder_ShouldSucceed() {
        assertTrue(store.isOpen());
        store.closeStore(founderId);

        assertFalse(store.isOpen(), "Store should be closed by founder");
    }

    @Test
    void closeStore_AlreadyClosed_ShouldThrow() {
        store.closeStore(founderId);
        assertFalse(store.isOpen());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.closeStore(founderId),
                "Expected closeStore to throw if the store is already closed");

        assertTrue(thrown.getMessage().contains("already closed"));
    }

    @Test
    void closeStore_NotFounder_ShouldThrow() {
        int nonFounderId = 99;

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.closeStore(nonFounderId),
                "Expected closeStore to throw, but it didn't");

        assertTrue(thrown.getMessage().contains("Requester ID: " + nonFounderId));
        assertTrue(store.isOpen(), "Store should still be open if close failed");
    }

    @Test
    void addRating_ValidRating_ShouldSucceed() {
        int userId = 1;
        double rating = 4.5;
        String comment = "Great product!";

        store.addRating(userId, rating, comment);

        assertEquals(rating, store.getStoreRatingByUser(userId).getRating(), "Rating should be added successfully");
    }

    @Test
    void addStoreProductRating_ValidRating_ShouldSucceed() {
        int userId = 1;
        int productId = 1;
        store.addStoreProduct(founderId, productId, "Test Product", 10.0, 100, PCategory.ELECTRONICS);
        double rating = 4.5;
        String comment = "Great product!";

        store.addStoreProductRating(userId, productId, rating, comment);

        assertEquals(rating, store.getStoreProductRating(userId, productId).getRating(),
                "Product rating should be added successfully");
    }

    @Test
    void addStoreProductRating_ProductNotFound_ShouldThrow() {
        int userId = 1;
        int productId = 99; // Assuming this product does not exist
        double rating = 4.5;
        String comment = "Great product!";

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.addStoreProductRating(userId, productId, rating, comment),
                "Expected addStoreProductRating to throw if the product is not found");

        assertThrows(
                IllegalArgumentException.class,
                () -> store.getStoreProductRating(userId, productId),
                "Expected getStoreProductRating to throw if the product is not found");
    }

    // Test receiving message from user
    @Test
    void receivingMessageFromUser_ValidMessage_ShouldSucceed() {
        int userId = 1;
        String message = "Hello, this is a test message.";

        store.receivingMessage(userId, message);
        Map<Integer,UserMsg> messages = store.getMessagesFromUsers(founderId);
        assertFalse(messages.isEmpty(), "Messages from users should not be empty");
        assertEquals(message, messages.entrySet().iterator().next().getValue().getMsg());
    }

    // Test sending message to user
    @Test
    void sendMessageToUser_ValidOwner_ShouldSucceed() {
        int userId = 1;
        String message = "Hello, this is a test message.";
        store.sendMessage(founderId, userId, message);

        // Verify that the publisher published the correct event
        org.mockito.Mockito.verify(publisher).publishEvent(
            org.mockito.Mockito.argThat((Object event) -> {
                if (!(event instanceof DomainLayer.Model.helpers.ResponseFromStoreEvent)) return false;
                DomainLayer.Model.helpers.ResponseFromStoreEvent resp = (DomainLayer.Model.helpers.ResponseFromStoreEvent) event;
                return resp.getStoreId() == store.getId() &&
                       resp.getUserId() == userId &&
                       resp.getMessage().equals(message);
            })
        );
    }

    @Test
    void sendMessageToUser_ValidManagerPermission_ShouldSucceed() {
        int userId = 1;
        int managerId = 2;
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.REQUESTS_REPLY));
        String message = "Hello, this is a test message.";
        store.acceptAssignment(managerId);
        store.sendMessage(managerId, userId, message);

        // Verify that the publisher published the correct event
        org.mockito.Mockito.verify(publisher).publishEvent(
            org.mockito.Mockito.argThat((Object event) -> {
                if (!(event instanceof DomainLayer.Model.helpers.ResponseFromStoreEvent)) return false;
                DomainLayer.Model.helpers.ResponseFromStoreEvent resp = (DomainLayer.Model.helpers.ResponseFromStoreEvent) event;
                return resp.getStoreId() == store.getId() &&
                       resp.getUserId() == userId &&
                       resp.getMessage().equals(message);
            })
        );
    }

    @Test
    void sendMessageToUser_InValidManagerPermission_ShouldThrow() {
        int userId = 1;
        int managerId = 2;
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.DISCOUNT_POLICY)); // Assuming null
                                                                                                      // permissions for
                                                                                                      // simplicity
        String message = "Hello, this is a test message.";

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.sendMessage(managerId, userId, message),
                "Expected sendMessage to throw if the owner is invalid");
    }

    @Test
    void sendMessageToUser_InvalidOwner_ShouldThrow() {
        int invalidOwnerId = 99;
        int userId = 1;
        String message = "Hello, this is a test message.";

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> store.sendMessage(invalidOwnerId, userId, message),
                "Expected sendMessage to throw if the owner is invalid");
    }

    @Test
    void addAuctionProduct_AsOwner_Success() {
        assertDoesNotThrow(() -> store.addAuctionProduct(founderId, productId, 50.0, 7));
    }

    @Test
    void addAuctionProduct_NonExistingProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, nonExistingProductId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("does not exist"));
    }

    @Test
    void addAuctionProduct_NotAuthorizedUser_Fails() {
        int randomUserId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(randomUserId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("insufficient permissions"));
    }

    @Test
    void addAuctionProduct_AsManagerWithInventoryPermission_Success() {
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerId);
        assertDoesNotThrow(() -> store.addAuctionProduct(managerId, productId, 50.0, 7));
    }

    @Test
    void addAuctionProduct_ManagerWithoutInventoryPermission_Fails() {
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.REQUESTS_REPLY));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(managerId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("insufficient permissions"));
    }

    @Test
    void addAuctionProduct_AlreadyExists_Fails() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("already"));
    }

    @Test
    void addAuctionProduct_ZeroMinutes_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, 0);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }

    @Test
    void addAuctionProduct_NegativeMinutes_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, 50.0, -1);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }

    @Test
    void addAuctionProduct_NegativeBasePrice_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addAuctionProduct(founderId, productId, -50.0, 7);
        });
        assertTrue(thrown.getMessage().contains("must be greater than 0"));
    }

    @Test
    void addBidOnAuctionProduct_SuccessfulBid() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        boolean success = store.addBidOnAuctionProduct(founderId, productId, 55.0);
        assertTrue(success);
    }

    @Test
    void addBidOnAuctionProduct_NonExistingAuctionProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.addBidOnAuctionProduct(founderId, nonExistingProductId, 60.0);
        });
        assertTrue(thrown.getMessage().contains("does not exist"));
    }

    @Test
    void isValidPurchaseAction_SuccessfulFlow() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        store.addBidOnAuctionProduct(founderId, productId, 60.0);
        assertDoesNotThrow(() -> store.isValidPurchaseAction(founderId, productId));
    }

    @Test
    void isValidPurchaseAction_WrongBidder_Fails() {
        store.addAuctionProduct(founderId, productId, 50.0, 7);
        store.addBidOnAuctionProduct(founderId, productId, 60.0);

        int otherUserId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.isValidPurchaseAction(otherUserId, productId);
        });
        assertTrue(thrown.getMessage().contains("is not the highest bidder"));
    }

    @Test
    void isValidPurchaseAction_NonAuctionProduct_Fails() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            store.isValidPurchaseAction(founderId, nonExistingProductId);
        });
        assertTrue(thrown.getMessage().contains("is not an auction product"));
    }

    @Test
    void addManagerPermissions_asFatherRequest_success() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(noPermsId);
        assertDoesNotThrow(() -> store.addManagerPermissions(founderId, noPermsId,
                List.of(StoreManagerPermission.REQUESTS_REPLY)));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId)
                .equals(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.REQUESTS_REPLY)));
    }

    @Test
    void addManagerPermissions_notOwnerRequest_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId, List.of(StoreManagerPermission.DISCOUNT_POLICY));
        store.acceptAssignment(noPermsId);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.addManagerPermissions(noPermsId, noPermsId, List.of(StoreManagerPermission.INVENTORY)));
        assertTrue(thrown.getMessage().contains("is not a valid store owner "));
        assertTrue(!store.getStoreManagers(founderId).get(noPermsId).contains(StoreManagerPermission.INVENTORY));
    }

    @Test
    void addManagerPermissions_notManagerYet_shouldThrow() {
        int noPermsId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.addManagerPermissions(founderId, noPermsId, List.of(StoreManagerPermission.INVENTORY)));
        assertTrue(thrown.getMessage().contains("is not a valid store manager "));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId) == null);
    }

    @Test
    void addManagerPermissions_notFatherRequest_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId, List.of(StoreManagerPermission.DISCOUNT_POLICY));
        store.acceptAssignment(noPermsId);
        int tmp_owner = 1010;
        store.addStoreOwner(founderId, tmp_owner);
        store.acceptAssignment(tmp_owner);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.addManagerPermissions(tmp_owner, noPermsId, List.of(StoreManagerPermission.INVENTORY)));
        assertTrue(thrown.getMessage().contains(" appointor can change/remove"));
        assertTrue(!store.getStoreManagers(founderId).get(noPermsId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId).contains(StoreManagerPermission.DISCOUNT_POLICY));

    }

    @Test
    void removeManagerPermissions_asFatherRequest_success() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId,
                List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.REQUESTS_REPLY));
        store.acceptAssignment(noPermsId);
        assertDoesNotThrow(() -> store.removeManagerPermissions(founderId, noPermsId,
                List.of(StoreManagerPermission.REQUESTS_REPLY)));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId).contains(StoreManagerPermission.INVENTORY));
        assertTrue(!store.getStoreManagers(founderId).get(noPermsId).contains(StoreManagerPermission.REQUESTS_REPLY));
    }

    @Test
    void removeManagerPermissions_notOwnerRequest_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId,
                List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.REQUESTS_REPLY));
        store.acceptAssignment(noPermsId);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.removeManagerPermissions(noPermsId, noPermsId,
                        List.of(StoreManagerPermission.REQUESTS_REPLY)));
        assertTrue(thrown.getMessage().contains("is not a valid store owner"));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId)
                .equals(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.REQUESTS_REPLY)));
    }

    @Test
    void removeManagerPermissions_notManagerYet_shouldThrow() {
        int noPermsId = 999;
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.removeManagerPermissions(founderId, noPermsId,
                        List.of(StoreManagerPermission.REQUESTS_REPLY)));
        assertTrue(thrown.getMessage().contains("is not a valid store manager"));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId) == null);
    }

    @Test
    void removeManagerPermissions_notFatherRequest_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId,
                List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        store.acceptAssignment(noPermsId);
        int tmp_owner = 1010;
        store.addStoreOwner(founderId, tmp_owner);
        store.acceptAssignment(tmp_owner);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.removeManagerPermissions(tmp_owner, noPermsId, List.of(StoreManagerPermission.INVENTORY)));
        assertTrue(thrown.getMessage().contains(" appointor can change/remove"));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId)
                .equals(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY)));

    }

    @Test
    void removeManagerPermissions_managerDoesNotHavePerm_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId,
                List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        store.acceptAssignment(noPermsId);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.removeManagerPermissions(founderId, noPermsId,
                        List.of(StoreManagerPermission.DISCOUNT_POLICY, StoreManagerPermission.PURCHASE_POLICY)));
        assertTrue(thrown.getMessage().contains("can not remove permission: PURCHASE_POLICY"));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId)
                .equals(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY)));
    }

    @Test
    void removeManagerPermissions_managersPermsLeftEmpty_shouldThrow() {
        int noPermsId = 999;
        store.addStoreManager(founderId, noPermsId,
                List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY));
        store.acceptAssignment(noPermsId);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> store.removeManagerPermissions(founderId, noPermsId,
                        List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY)));
        assertTrue(thrown.getMessage().contains("permissions can not be empty"));
        assertTrue(store.getStoreManagers(founderId).get(noPermsId)
                .equals(List.of(StoreManagerPermission.INVENTORY, StoreManagerPermission.DISCOUNT_POLICY)));
    }

    @Test
    void getStoreOwners_ValidRequest_ShouldSucceed() {
        int userId = 1;
        store.addStoreOwner(founderId, userId); // Assuming this method exists to add an owner
        store.acceptAssignment(userId);
        List<Integer> owners = store.getStoreOwners(founderId);

        assertTrue(owners.contains(userId), "User should be in the list of store owners");
    }

    @Test
    void returnProductsToStore_ValidProduct_ShouldIncreaseQuantity() {
        int userId = 1;
        int productId = 100;
        int originalQuantity = 10;
        int returnQuantity = 5;
    
        store.addStoreProduct(founderId, productId, "Test Product", 20.0, originalQuantity, PCategory.ELECTRONICS);
        
        Map<Integer, Integer> returnedProducts = new HashMap<>();
        returnedProducts.put(productId, returnQuantity);
    
        store.returnProductsToStore(userId, returnedProducts);
    
        StoreProduct updatedProduct = store.getStoreProduct(productId);
    
        assertEquals(originalQuantity + returnQuantity, updatedProduct.getQuantity(),
                "Product quantity should be increased after return");
    }
    
    @Test
    void decrementProductsInStore_ValidPurchase_ShouldSucceed() {
        int userId = 1;
        int productId = 101;
        int originalQuantity = 20;
        int purchaseQuantity = 5;
    
        store.addStoreProduct(founderId, productId, "Another Product", 15.0, originalQuantity, PCategory.ELECTRONICS);
        
        Map<Integer, Integer> toBuy = new HashMap<>();
        toBuy.put(productId, purchaseQuantity);
    
        Map<StoreProductDTO, Boolean> result = store.decrementProductsInStore(userId, toBuy);
    
        StoreProduct updatedProduct = store.getStoreProduct(productId);
    
        assertEquals(originalQuantity - purchaseQuantity, updatedProduct.getQuantity(),
                "Product quantity should decrease after purchase");
        
        StoreProductDTO dto = result.keySet().iterator().next();
        assertEquals(purchaseQuantity, dto.getQuantity(), "DTO should reflect purchase quantity");
        assertTrue(result.get(dto), "Should return true when full quantity was available and purchased");
    }
    
    @Test
    void decrementProductsInStore_ProductNotExist_ShouldThrowException() {
        int userId = 1;
        int nonExistentProductId = 999;
    
            Map<Integer, Integer> toBuy = new HashMap<>();
            toBuy.put(nonExistentProductId, 1);
        
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                store.decrementProductsInStore(userId, toBuy);
            });
        
            String expectedMessage = "Product with ID: " + nonExistentProductId + " does not exist";
            assertTrue(exception.getMessage().contains(expectedMessage), "Exception message should indicate missing product");
        }

    @Test
    void testStoreCtorWithId() {
        Store s = new Store("Another Store", founderId, publisher, 42);
        assertEquals(42, s.getId());
        assertEquals("Another Store", s.getName());
    }

    @Test
    void testEditStoreProduct() { 
        int productId = 200;
        store.addStoreProduct(founderId, productId, "Old", 10.0, 5, PCategory.ELECTRONICS);
        store.editStoreProduct(founderId, productId, "Old", 20.0, 10);
        StoreProduct prod = store.getStoreProduct(productId);
        assertEquals("Old", prod.getName()); // NAME IS IMMUTABLE
        assertEquals(20.0, prod.getBasePrice());
        assertEquals(10, prod.getQuantity());
    }

    @Test
    void testRemoveStoreProduct() {
        int productId = 201;
        store.addStoreProduct(founderId, productId, "ToRemove", 10.0, 5, PCategory.ELECTRONICS);
        store.removeStoreProduct(founderId, productId);
        assertThrows(IllegalArgumentException.class, () -> store.getStoreProduct(productId));
    }

    @Test
    void testAddPurchasePolicy() {
        PurchasePolicy policy = mock(PurchasePolicy.class);
        when(policy.getPolicyID()).thenReturn(1);
        store.addPurchasePolicy(founderId, policy);
        assertTrue(store.getPurchasePolicies().containsKey(1));
    }

    @Test
    void testGetStoreManagers() {
        int managerId = 1234;
        store.addStoreManager(founderId, managerId, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerId);
        Map<Integer, List<StoreManagerPermission>> managers = store.getStoreManagers(founderId);
        assertTrue(managers.containsKey(managerId));
    }

    @Test
    void testgetMessagesFromUsers() {
        int userId = 1;
        store.receivingMessage(userId, "msg");
        Map<Integer,UserMsg> messages = store.getMessagesFromUsers(founderId);
        assertFalse(messages.isEmpty(), "Messages from users should not be empty");
        assertEquals("msg", messages.entrySet().iterator().next().getValue().getMsg());
    }

    @Test
    void testGetMessagesFromUsers() {
        int userId = 1;
        store.receivingMessage(userId, "msg");
        assertFalse(store.getMessagesFromUsers(founderId).isEmpty());
    }

    @Test
    void testGetMessagesFromStore() {
        int userId = 1;
        String message = "msg";
        store.sendMessage(founderId, userId, message);
        // Verify that the publisher published the correct event
        org.mockito.Mockito.verify(publisher).publishEvent(
            org.mockito.Mockito.argThat((Object event) -> {
                if (!(event instanceof DomainLayer.Model.helpers.ResponseFromStoreEvent)) return false;
                DomainLayer.Model.helpers.ResponseFromStoreEvent resp = (DomainLayer.Model.helpers.ResponseFromStoreEvent) event;
                return resp.getStoreId() == store.getId() &&
                       resp.getUserId() == userId &&
                       resp.getMessage().equals(message);
            })
        );
    }

    @Test
    void testDeclineAssignment_NoPendingAssignment_ShouldThrow() {
        int userId = 9999;
        assertThrows(IllegalArgumentException.class, () -> store.declineAssignment(userId));
    }

    @Test
    void testAcceptAssignment_NoPendingAssignment_ShouldThrow() {
        int userId = 9999;
        assertThrows(IllegalArgumentException.class, () -> store.acceptAssignment(userId));
    }

    @Test
    void testGetStoreProductAllRatings() {
        int productId = 400;
        store.addStoreProduct(founderId, productId, "Rated", 10.0, 5, PCategory.ELECTRONICS);
        store.addStoreProductRating(1, productId, 5.0, "good");
        assertFalse(store.getStoreProductAllRatings(productId).isEmpty());
    }

    @Test
    void testGetStoreOwners() {
        List<Integer> owners = store.getStoreOwners(founderId);
        assertTrue(owners.contains(founderId));
    }

    @Test
    void testCheckIfProductsInStore() {
        int productId = 500;
        store.addStoreProduct(founderId, productId, "Check", 10.0, 5, PCategory.ELECTRONICS);
        Map<Integer, Integer> products = Map.of(productId, 2);
        Map<StoreProductDTO, Boolean> result = store.checkIfProductsInStore(founderId, products);
        assertFalse(result.isEmpty());
    }

    @Test
    void testAddRating() {
        int userId = 2;
        store.addRating(userId, 4.0, "Nice");
        assertEquals(4.0, store.getStoreRatingByUser(userId).getRating());
    }

    @Test
    void testGetPendingOwnersAndManagers() {
        int newOwner = 12345;
        store.addStoreOwner(founderId, newOwner);
        assertTrue(store.getPendingOwners(founderId).contains(newOwner));
        int newManager = 54321;
        store.addStoreManager(founderId, newManager, List.of(StoreManagerPermission.INVENTORY));
        assertTrue(store.getPendingManagers(founderId).contains(newManager));
    }

    @Test
    void testGetStoreRatingByUser_NotRated_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> store.getStoreRatingByUser(999));
    }

    @Test
    void testGetStoreProduct_NotExist_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> store.getStoreProduct(999));
    }

    @Test
    void testOpenStore() {
        store.closeStore(founderId);
        store.openStore(founderId);
        assertTrue(store.isOpen());
    }

    @Test
    void testCanViewOrders() {
        assertTrue(store.canViewOrders(founderId));
    }

    @Test
    void testHandleReceivedHigherBid() {
        int productId = 600;
        store.addStoreProduct(founderId, productId, "Auction", 10.0, 5, PCategory.ELECTRONICS);
        store.addAuctionProduct(founderId, productId, 10.0, 1);
        // This will publish an event, but we just want to ensure no exception
        store.addBidOnAuctionProduct(founderId, productId, 15.0);
    }

    @Test
    void testRemoveAllChildrenRoles() {
        // This is a private method, so test indirectly by removing an owner with children
        int owner2 = 2222;
        store.addStoreOwner(founderId, owner2);
        store.acceptAssignment(owner2);
        int manager = 3333;
        store.addStoreManager(owner2, manager, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(manager);
        // Now remove owner2, which should remove manager as well
        store.removeStoreOwner(founderId, owner2);
        assertFalse(store.getStoreOwners(founderId).contains(owner2));
    }

    @Test
    void testHandleAuctionEnd() {
        int productId = 700;
        store.addStoreProduct(founderId, productId, "Auction", 10.0, 5, PCategory.ELECTRONICS);
        store.addAuctionProduct(founderId, productId, 10.0, 1);
        // Simulate auction end (should publish event)
        // This is private, so you can test via public API or use reflection if needed
    }

    @Test
    void testDecrementProductsInStore() {
        int productId = 900;
        store.addStoreProduct(founderId, productId, "Decrement", 10.0, 5, PCategory.ELECTRONICS);
        Map<Integer, Integer> toBuy = Map.of(productId, 2);
        Map<StoreProductDTO, Boolean> result = store.decrementProductsInStore(founderId, toBuy);
        assertFalse(result.isEmpty());
    }
    
    @Test
    void testGetPendingOwners_PermissionChecks() {
        int newOwner = 12345;
        store.addStoreOwner(founderId, newOwner);
        // Founder can view pending owners
        assertTrue(store.getPendingOwners(founderId).contains(newOwner));

        // Add a manager with VIEW_ROLES permission
        int managerWithView = 11111;
        store.addStoreManager(founderId, managerWithView, List.of(StoreManagerPermission.VIEW_ROLES));
        store.acceptAssignment(managerWithView);
        assertTrue(store.getPendingOwners(managerWithView).contains(newOwner));

        // Add a manager without VIEW_ROLES permission
        int managerNoView = 22222;
        store.addStoreManager(founderId, managerNoView, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerNoView);
        assertThrows(IllegalArgumentException.class, () -> store.getPendingOwners(managerNoView));

        // Non-owner, non-manager should throw
        int randomUser = 33333;
        assertThrows(IllegalArgumentException.class, () -> store.getPendingOwners(randomUser));
    }

    @Test
    void testGetPendingOwners_EmptyList() {
        // No pending owners
        List<Integer> pending = store.getPendingOwners(founderId);
        assertTrue(pending.isEmpty());
    }
    
    @Test
    void testGetPendingManagers_PermissionChecks() {
        int newManager = 54321;
        store.addStoreManager(founderId, newManager, List.of(StoreManagerPermission.INVENTORY));
        // Founder can view pending managers
        assertTrue(store.getPendingManagers(founderId).contains(newManager));

        // Add a manager with VIEW_ROLES permission
        int managerWithView = 11111;
        store.addStoreManager(founderId, managerWithView, List.of(StoreManagerPermission.VIEW_ROLES));
        store.acceptAssignment(managerWithView);
        assertTrue(store.getPendingManagers(managerWithView).contains(newManager));

        // Add a manager without VIEW_ROLES permission
        int managerNoView = 22222;
        store.addStoreManager(founderId, managerNoView, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerNoView);
        assertThrows(IllegalArgumentException.class, () -> store.getPendingManagers(managerNoView));

        // Non-owner, non-manager should throw
        int randomUser = 33333;
        assertThrows(IllegalArgumentException.class, () -> store.getPendingManagers(randomUser));
    }

    @Test
    void testGetPendingManagers_EmptyList() {
        // No pending managers
        List<Integer> pending = store.getPendingManagers(founderId);
        assertTrue(pending.isEmpty());
    }
    
    @Test
    void testIsManagerAndGetPerms_ManagerExists_ReturnsCopy() throws Exception {
        // 1) Inject directly into the private storeManagers map via reflection
        Field managersField = Store.class.getDeclaredField("storeManagers");
        managersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, List<StoreManagerPermission>> managers =
            (Map<Integer, List<StoreManagerPermission>>) managersField.get(store);

        // Prepare and put a list for managerId
        List<StoreManagerPermission> originalPerms =
            new ArrayList<>(List.of(StoreManagerPermission.VIEW_PURCHASES, StoreManagerPermission.VIEW_ROLES));
        managers.put(managerId, originalPerms);

        // 2) Call the method under test
        List<StoreManagerPermission> returned = store.isManagerAndGetPerms(managerId);

        // 3) Verify we got back an equal list, but not the same instance
        assertNotNull(returned, "Should not return null when manager exists");
        assertEquals(originalPerms, returned, "Permissions should match what was stored");
        assertNotSame(originalPerms, returned, "Should return a defensive copy, not the same list");

        // 4) Mutating the returned list must NOT affect the internal map
        returned.clear();
        @SuppressWarnings("unchecked")
        List<StoreManagerPermission> stillStored = managers.get(managerId);
        assertFalse(stillStored.isEmpty(), "Clearing the returned list must not clear the internal one");
    }

    /**
     * When the user is not in the map, we should get back null.
     */
    @Test
    void testIsManagerAndGetPerms_NotAManager_ReturnsNull() {
        // ensure no entry for nonExistingManager
        int nonManager = 999;
        assertNull(store.isManagerAndGetPerms(nonManager), "Should return null if userId not a manager");
    }

    @Test
    void testAddAndDiscountWithProductsScope_AsManagerWithoutDiscountPolicy_NoEffect() {
        int managerNoPerm = 54321;
        store.addStoreManager(founderId, managerNoPerm, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerNoPerm);
    
        List<Integer> productIDs = List.of(productId);
        List<Predicate<Cart>> conditions = List.of(c -> true);
        double percentage = 20.0;
    
        int before = store.getPurchasePolicies().size();
        store.addAndDiscountWithProductsScope(managerNoPerm, productIDs, conditions, percentage);
        int after = store.getPurchasePolicies().size();
    
        assertEquals(before, after, "Should not add discount if manager lacks DISCOUNT_POLICY");
    }
    
    @Test
    void testAddAndDiscountWithProductsScope_NotOwnerOrManager_NoEffect() {
        int randomUser = 99999;
    
        List<Integer> productIDs = List.of(productId);
        List<Predicate<Cart>> conditions = List.of(c -> true);
        double percentage = 25.0;
    
        int before = store.getPurchasePolicies().size();
        store.addAndDiscountWithProductsScope(randomUser, productIDs, conditions, percentage);
        int after = store.getPurchasePolicies().size();
    
        assertEquals(before, after, "Should not add discount if not owner or manager");
    }

    
    @Test
    void testAddAndDiscountWithStoreScope_AsManagerWithoutDiscountPolicy_NoEffect() {
        int managerNoPerm = 54321;
        store.addStoreManager(founderId, managerNoPerm, List.of(StoreManagerPermission.INVENTORY));
        store.acceptAssignment(managerNoPerm);
    
        List<Predicate<Cart>> conditions = List.of(c -> true);
        double percentage = 20.0;
    
        int before = store.getPurchasePolicies().size();
        store.addAndDiscountWithStoreScope(managerNoPerm, conditions, percentage);
        int after = store.getPurchasePolicies().size();
    
        assertEquals(before, after, "Should not add discount if manager lacks DISCOUNT_POLICY");
    }
    
    @Test
    void testAddAndDiscountWithStoreScope_NotOwnerOrManager_NoEffect() {
        int randomUser = 99999;
    
        List<Predicate<Cart>> conditions = List.of(c -> true);
        double percentage = 25.0;
    
        int before = store.getPurchasePolicies().size();
        store.addAndDiscountWithStoreScope(randomUser, conditions, percentage);
        int after = store.getPurchasePolicies().size();
    
        assertEquals(before, after, "Should not add discount if not owner or manager");
    }
    
}
    
    
    

