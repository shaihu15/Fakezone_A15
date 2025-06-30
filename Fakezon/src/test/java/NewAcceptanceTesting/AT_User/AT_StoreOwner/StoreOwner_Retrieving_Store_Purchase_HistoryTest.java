package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.OrderedProductDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory; 

import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;

import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)

public class StoreOwner_Retrieving_Store_Purchase_HistoryTest {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int OwnerUserId;
    private int storeId;
    private int guestId;
    private int StoreFounderId;
    private int registeredId;
    private int registeredId3;
    private int productIdInt;


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

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, StoreFounderId, productName, productDescription, 1, 10, category);
        assertNotNull(storePResponse.getData());
        productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

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

    @AfterEach
    void tearDown() {
        // Remove the product from the store
        Response<Void> deleteProductResponse = systemService.removeProductFromStore(storeId, StoreFounderId, productIdInt);
        assertTrue(deleteProductResponse.isSuccess(), "Product deletion should succeed");

        // Close the store
        Response<String> deleteStoreResponse = systemService.closeStoreByFounder(storeId, StoreFounderId);
        assertTrue(deleteStoreResponse.isSuccess(), "Store deletion should succeed");

        // Delete the users
        Response<Boolean> deleteResponse1 = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteResponse1.isSuccess(), "User deletion should succeed");

        Response<Boolean> deleteResponse2 = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteResponse2.isSuccess(), "User deletion should succeed");

        Response<Boolean> deleteResponse3 = systemService.deleteUser(testHelper.validEmail3());
        assertTrue(deleteResponse3.isSuccess(), "User deletion should succeed");

        Response<Boolean> deleteGuestResponse = systemService.removeUnsignedUser(guestId);
        assertTrue(deleteGuestResponse.isSuccess(), "Guest user deletion should succeed");
    }
    
    @Test
    void testRetrieveStorePurchaseHistory_Success() {
        // Retrieve the purchase history for the store
        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(storeId, StoreFounderId);
        assertTrue(purchaseHistoryRes.isSuccess());
        assertFalse(purchaseHistoryRes.getData().isEmpty());
 
        // Check if the purchase history contains the expected product
        boolean foundProduct = false;
        for (OrderDTO order : purchaseHistoryRes.getData()) {
            for (OrderedProductDTO product : order.getProducts()) {
                if (product.getName().equals("Test Product")) {
                    foundProduct = true;
                    break;
                }
            }
        }
        assertTrue(foundProduct);
    }

    @Test
    void testRetrieveStorePurchaseHistory_StoreNotFound_Failure() {
        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(-1, StoreFounderId);
        assertFalse(purchaseHistoryRes.isSuccess());
        assertEquals("Store not found", purchaseHistoryRes.getMessage());
    }

    @Test
    void testRetrieveStorePurchaseHistory_UserNotStoreOwner_Failure() {
        // Attempt to retrieve purchase history as a user who is not the store owner
        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(storeId, registeredId);
        assertFalse(purchaseHistoryRes.isSuccess());
        assertEquals("user " + registeredId + " has insufficient permissions to view orders from store " + storeId, purchaseHistoryRes.getMessage());
    }

    @Test
    void testRetrieveStorePurchaseHistory_StoreWithNoPurchaseHistory_Success() {
        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store2");
        assertNotNull(storeResult.getData());
        int storeId2 = storeResult.getData(); 

        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(storeId2, StoreFounderId);
        assertTrue(purchaseHistoryRes.isSuccess());
        assertTrue(purchaseHistoryRes.getData().isEmpty());
    }

        @Test
    void testRetrieveStorePurchaseHistory_MultipleBuyers_Success() {
        // Register and login a second buyer
        Response<UserDTO> resultRegister4 = testHelper.register_and_login5();
        assertTrue(resultRegister4.isSuccess());
        int registeredId4 = resultRegister4.getData().getUserId();

        // Add the same product to the new buyer's basket
        Response<Void> responseAddToBasket2 = systemService.addToBasket(registeredId4, productIdInt, storeId, 1);
        assertTrue(responseAddToBasket2.isSuccess());

        // Purchase the cart for the new buyer
        Response<String> responsePurchaseCart2 = systemService.purchaseCart(
                registeredId4, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                "deliveryMethod", "1234567890123456", "cardHolder",
                "12/25", "123", "123 Main St* City* Country* 0000", "Recipient",
                "Package details");
        assertTrue(responsePurchaseCart2.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart2.getMessage());

        // Retrieve the purchase history for the store
        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(storeId, StoreFounderId);
        assertTrue(purchaseHistoryRes.isSuccess());

        // There should be at least 2 orders in the store's purchase history
        assertTrue(purchaseHistoryRes.getData().size() >= 2, "There should be at least 2 orders in the store's purchase history");

        // Optionally, check that both buyers' orders are present
        boolean foundFirstBuyer = false;
        boolean foundSecondBuyer = false;
        for (OrderDTO order : purchaseHistoryRes.getData()) {
            if (order.getUserId() == registeredId) {
                foundFirstBuyer = true;
            }
            if (order.getUserId() == registeredId4) {
                foundSecondBuyer = true;
            }
        }
        assertTrue(foundFirstBuyer, "First buyer's order should be present");
        assertTrue(foundSecondBuyer, "Second buyer's order should be present");
    }
}
