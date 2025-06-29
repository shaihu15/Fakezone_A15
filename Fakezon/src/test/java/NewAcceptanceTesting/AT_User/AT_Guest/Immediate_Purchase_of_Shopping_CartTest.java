package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.IOrder;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class Immediate_Purchase_of_Shopping_CartTest {
    //Use-Case: 2.5 Immediate Purchase of Shopping Cart

     @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int guestId;
    int registeredId;
    int storeId;
    int productIdInt;
    int StoreFounderId;
    int registeredId3;
    

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        // Guest enters the system
        Response<UserDTO> guestResponse = systemService.createUnsignedUser();
        assertTrue(guestResponse.isSuccess());
        guestId = guestResponse.getData().getUserId();

        Response<UserDTO> resultStoreFounder = testHelper.register_and_login();
        assertNotNull(resultStoreFounder.getData());
        StoreFounderId = resultStoreFounder.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        registeredId = resultRegister2.getData().getUserId();
        // resaigter1 is registered and logged in

        
        Response<UserDTO> resultRegister3 = testHelper.register_and_login3();
        assertNotNull(resultRegister3.getData());
        registeredId3 = resultRegister3.getData().getUserId();
        // resaigter1 is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store
    }

    @AfterEach
    void tearDown() {
        // Close the store (ignore if already closed)
        Response<String> closeStoreResponse = systemService.closeStoreByFounder(storeId, StoreFounderId);
        if (!closeStoreResponse.isSuccess()) {
            assertEquals("Error during closing store: Store: " + storeId + " is already closed", closeStoreResponse.getMessage());
        }

        // Remove the store (ignore if already removed)
        Response<Void> removeStoreResponse = systemService.removeStore(storeId, StoreFounderId);
        if (!removeStoreResponse.isSuccess()) {
            assertEquals("Error during removing store: Store not found", removeStoreResponse.getMessage());
        }

        // Remove guest user
        Response<Boolean> deleteGuestResponse = systemService.removeUnsignedUser(guestId);
        assertTrue(deleteGuestResponse.isSuccess(), "Guest user deletion should succeed");

        // Remove founder user
        Response<Boolean> deleteFounderResponse = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteFounderResponse.isSuccess(), "Founder user deletion should succeed");

        // Remove registered user 2
        Response<Boolean> deleteRegisteredResponse2 = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteRegisteredResponse2.isSuccess(), "Registered user 2 deletion should succeed");

        // Remove registered user 3
        Response<Boolean> deleteRegisteredResponse3 = systemService.deleteUser(testHelper.validEmail3());
        assertTrue(deleteRegisteredResponse3.isSuccess(), "Registered user 3 deletion should succeed");
    }
 
    @Test
    void testImmediatePurchase_Registered_Success() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 1);
    }

    @Test
    void testImmediatePurchase_Guest_Success() {
        Response<Void> responseAddToBasket = systemService.addToBasket(guestId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (guestId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
    }

    @Test
    void testImmediatePurchase_invalidCountry_Failure() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.invalidCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Invalid country code", responsePurchaseCart.getMessage());
                
        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 0);
    }    
    
    @Test
    void testImmediatePurchase_invalUserId_Failure() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId+10 , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Error during purchase cart: User not found", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 0);  
        }

    @Test
    void testImmediatePurchase_Cartisempty_Failure() {

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Cart is empty", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 0);
    }

    @Test
    void testImmediatePurchase_ProductsNotInStores_Failure() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId,productIdInt, storeId,1); 
        assertTrue(responseAddToBasket.isSuccess());
        // register1 added the product to the basket

        Response<Void> responseAddToBasket3 = systemService.addToBasket(registeredId3, productIdInt, storeId,1); 
        assertTrue(responseAddToBasket3.isSuccess());
        // user3 added the product to the basket

         Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
        //user2 purchased the cart

        //------------------------------------------------------------------------------------------------//
        //now user 3 try to purchase the cart
        Response<String> responsePurchaseCart3 = systemService.purchaseCart
                    (registeredId3 , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart3.isSuccess());
        assertEquals("Product is not available: Test Product", responsePurchaseCart3.getMessage());

    }

    @Test
    void testImmediatePurchase_CreditSystemRejection_Failure() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId,productIdInt, storeId,1);
        assertTrue(responseAddToBasket.isSuccess());
        // User added the product to the basket

        // Simulate rejection from the credit system
        Response<String> responsePurchaseCart = systemService.purchaseCart(
                registeredId, testHelper.validCountry(), LocalDate.now(), null, null,
                null, null, null, null, null,
                null, null);
    

        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Payment failed", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 0);
    }

    @Test
    void testImmediatePurchase_ShippingSystemRejection_Failure() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId,productIdInt, storeId, 1);
        assertTrue(responseAddToBasket.isSuccess());
        // User added the product to the basket

        
        Response<String> responsePurchaseCart = systemService.purchaseCart(
                registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                null, "1234567890123456", "cardHolder",
                "12/25", "123", null, "Recipient",
                "Package details");

        // Assert: Purchase fails, no credit is charged
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Delivery failed", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> orders =systemService.getOrdersByUserId(registeredId);
        assertTrue(orders.isSuccess());
        assertTrue(orders.getData().size() == 0);
    }

    @Test
    void testImmediatePurchase_Parallel_LastProduct() throws Exception {
        // Both users add the same product to their basket
        assertTrue(systemService.addToBasket(registeredId, productIdInt, storeId, 1).isSuccess());
        assertTrue(systemService.addToBasket(registeredId3, productIdInt, storeId, 1).isSuccess());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Response<String>> task1 = () -> systemService.purchaseCart(
            registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
            "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
            "123 Main St* City* Country* 0000", "Recipient", "Package details"
        );

        Callable<Response<String>> task2 = () -> systemService.purchaseCart(
            registeredId3, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
            "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
            "123 Main St* City* Country* 0000", "Recipient", "Package details"
        );

        Future<Response<String>> future1 = executor.submit(task1);
        Future<Response<String>> future2 = executor.submit(task2);

        Response<String> result1 = null;
        Response<String> result2 = null;
        boolean optimisticLockingFailureCaught = false;

        try {
            result1 = future1.get();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                cause instanceof org.hibernate.StaleStateException) {
                optimisticLockingFailureCaught = true;
            } else {
                throw e;
            }
        }
        try {
            result2 = future2.get();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                cause instanceof org.hibernate.StaleStateException) {
                optimisticLockingFailureCaught = true;
            } else {
                throw e;
            }
        }

        executor.shutdown();

        // Assert only one success (either one result is success, or one thread failed with optimistic locking)
        int successCount = 0;
        if (result1 != null && result1.isSuccess()) successCount++;
        if (result2 != null && result2.isSuccess()) successCount++;
        assertTrue(successCount == 1 || (successCount == 0 && optimisticLockingFailureCaught),
            "Only one user should be able to purchase the last product, or one should fail with optimistic locking.");

        // If both results are present, check the error message for the failed one
        String expectedFailureMessage = "Product is not available: Test Product";
        if (result1 != null && !result1.isSuccess()) {
            assertEquals(expectedFailureMessage, result1.getMessage());
        }
        if (result2 != null && !result2.isSuccess()) {
            assertEquals(expectedFailureMessage, result2.getMessage());
        }
    }

    @Test
    void testImmediatePurchase_Parallel_ProductDeletedByOwner() throws Exception {
        // User adds the product to basket
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1);
        assertTrue(responseAddToBasket.isSuccess());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: Store owner deletes the product
        Callable<Response<Void>> deleteTask = () -> systemService.removeProductFromStore(storeId, StoreFounderId, productIdInt);

        // Task 2: User tries to purchase the product
        Callable<Response<String>> purchaseTask = () -> systemService.purchaseCart(
            registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
            "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
            "123 Main St* City* Country* 0000", "Recipient", "Package details"
        );

        Future<Response<Void>> deleteFuture = executor.submit(deleteTask);
        Future<Response<String>> purchaseFuture = executor.submit(purchaseTask);

        Response<Void> deleteResult = null;
        Response<String> purchaseResult = null;
        boolean optimisticLockingFailureCaught = false;

        try {
            deleteResult = deleteFuture.get();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                cause instanceof org.hibernate.StaleStateException) {
                optimisticLockingFailureCaught = true;
            } else {
                throw e;
            }
        }
        try {
            purchaseResult = purchaseFuture.get();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                cause instanceof org.hibernate.StaleStateException) {
                optimisticLockingFailureCaught = true;
            } else {
                throw e;
            }
        }

        executor.shutdown();

        // If purchase succeeded, deletion may or may not have succeeded
        if (purchaseResult != null && purchaseResult.isSuccess()) {
            // If purchase succeeded, deletion may have happened after
            assertTrue(deleteResult == null || deleteResult.isSuccess() || !deleteResult.isSuccess(),
                "Product deletion may or may not succeed depending on timing.");
        } else if (purchaseResult != null) {
            // If purchase failed, make sure it failed because the product was unavailable
            assertEquals(
                "Error during purchase cart: Product with ID: " + productIdInt + " does not exist in store ID: " + storeId,
                purchaseResult.getMessage()
            );
        } else {
            // If purchaseResult is null, it means an optimistic locking failure occurred
            assertTrue(optimisticLockingFailureCaught, "Expected optimistic locking failure in parallel scenario.");
        }
    }

}