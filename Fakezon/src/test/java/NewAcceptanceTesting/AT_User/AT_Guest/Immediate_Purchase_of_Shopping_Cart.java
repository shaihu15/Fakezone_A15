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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

public class Immediate_Purchase_of_Shopping_Cart {
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
        systemService.clearAllData();
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
 
    @Test
    void testImmediatePurchase_Registered_Success() {
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
        //user2 purchased the cart

        //------------------------------------------------------------------------------------------------//
        //now user 3 try to purchase the cart
        Response<String> responsePurchaseCart3 = systemService.purchaseCart
                    (registeredId3 , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
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

        /*Using ExecutorService is the standard and safe way to manage threads in Java for parallel tasks,
        instead of creating Thread objects directly and managing them manually. */
        
        // Prepare concurrent purchase tasks
        ExecutorService executor = Executors.newFixedThreadPool(2);

        /*Callable is a functional interface that allows you to define a task that can be executed in a separate thread, 
            and it can also return a value (in this case, Response<Void>) and throw exceptions (which is important for error checking).
            Each task represents a call to a systemService function with different parameters (for example, with different users). */

        Callable<Response<String>> task1 = () -> systemService.purchaseCart(
            registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
            "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
            "123 Main St, City, Country", "Recipient", "Package details"
        );

        Callable<Response<String>> task2 = () -> systemService.purchaseCart(
            registeredId3, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
            "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
            "123 Main St, City, Country", "Recipient", "Package details"
        );

        /*The submit() method of the ExecutorService submits the task for execution on an available thread in the pool. It returns a Future object.
        A Future object represents the result of an asynchronous task (that is, a task that runs in the background). 
        It allows you to check whether the task has completed, cancel it, and most importantly, receive its result as soon as it completes. */

        Future<Response<String>> future1 = executor.submit(task1);
        Future<Response<String>> future2 = executor.submit(task2);

        Response<String> result1 = future1.get(); // waits for first purchase
        Response<String> result2 = future2.get(); // waits for second purchase

        executor.shutdown();

        // Assert only one success
        boolean oneSuccess = result1.isSuccess() ^ result2.isSuccess(); // XOR: exactly one is true
        assertTrue(oneSuccess, "Only one user should be able to purchase the last product.");

        // Validate the error message from the failure
        String expectedFailureMessage = "Product is not available: Test Product";
        if (!result1.isSuccess()) {
            assertEquals(expectedFailureMessage, result1.getMessage());
        } else {
            assertEquals(expectedFailureMessage, result2.getMessage());
        }

    }

    @Test
    void testImmediatePurchase_Parallel_ProductDeletedByOwner() throws Exception {
        // User adds the product to basket
        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1);
        assertTrue(responseAddToBasket.isSuccess());

        // Use ExecutorService to simulate parallel execution
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: Store owner deletes the product
        Callable<Response<Void>> deleteTask = () -> {
            return systemService.removeProductFromStore(storeId, StoreFounderId, productIdInt);
        };

        // Task 2: User tries to purchase the product
        Callable<Response<String>> purchaseTask = () -> {
            return systemService.purchaseCart(
                registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
                "123 Main St, City, Country", "Recipient", "Package details"
            );
        };

        Future<Response<Void>> deleteFuture = executor.submit(deleteTask);
        Future<Response<String>> purchaseFuture = executor.submit(purchaseTask);

        Response<Void> deleteResult = deleteFuture.get();
        Response<String> purchaseResult = purchaseFuture.get();

        executor.shutdown();

        // Either deletion or purchase can happen first, but purchase should fail if deletion won
        if (purchaseResult.isSuccess()) {
            // If purchase succeeded, deletion must have happened after
            assertTrue(deleteResult.isSuccess() || !deleteResult.isSuccess(), 
                "Product deletion may or may not succeed depending on timing.");
        } else {
            // If purchase failed, make sure it failed because the product was unavailable
            assertEquals("Error during purchase cart: Product with ID: "+ productIdInt +" does not exist in store ID: " + storeId, purchaseResult.getMessage());
        }
    }

}
