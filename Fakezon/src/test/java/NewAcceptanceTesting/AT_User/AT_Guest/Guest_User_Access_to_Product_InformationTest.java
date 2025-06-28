package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import ApplicationLayer.Services.SystemService;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class Guest_User_Access_to_Product_InformationTest {
    // Use-case: 2.1 Guest User Access to Product Information
    
     @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int storeId;
    int userId;
    int productId;
    String username;

    @BeforeEach
    void setUp() {

        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultRegister = testHelper.register_and_login();
        assertTrue(resultRegister.isSuccess());
        userId = resultRegister.getData().getUserId();
        username = resultRegister.getData().getUserEmail();
        // StoreFounder is registered and logged in

        Response<Integer> resultAddStore = testHelper.openStore(userId);
        assertTrue(resultAddStore.isSuccess());
        // StoreFounder opened a store
        storeId = resultAddStore.getData();

        // Add a product to the store
        Response<StoreProductDTO> productResponse = testHelper.addProductToStore(storeId, userId);
        assertTrue(productResponse.isSuccess());
        productId = productResponse.getData().getProductId();
    }

    @AfterEach
    void tearDown() {
        Response<Void> deleteProductResponse = systemService.removeProductFromStore(storeId, userId, productId);
        assertTrue(deleteProductResponse.isSuccess(), "Product deletion should succeed");
        Response<String> deleteStoreResponse = systemService.closeStoreByFounder(storeId, userId);
        if (!deleteStoreResponse.isSuccess()) {
            assertEquals("Error during closing store: Store: " + storeId + " is already closed", deleteStoreResponse.getMessage());
        }
        Response<Boolean> deleteResponse = systemService.deleteUser(username);
        assertTrue(deleteResponse.isSuccess(), "User deletion should succeed");
        Response<Boolean> deleteGuestResponse = systemService.removeUnsignedUser(userId);
        assertTrue(deleteGuestResponse.isSuccess(), "Guest user deletion should succeed");
        // Clean up the store
        Response<Void> deleteStoreResponse2 = systemService.removeStore(storeId, userId);
        assertTrue(deleteStoreResponse2.isSuccess(), "Store deletion should succeed");
    }

    @Test
    void testGuestUserAccessToProductInformation_Succsses() {
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, storeId);
        assertTrue(productResponse.isSuccess());
        assertEquals(productId, productResponse.getData().getProductId());
    }

    @Test
    void testGuestUserAccessToProductInformation_ProductNotFound_Fail() {
        int nonExistentProductId = 9999; 
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(nonExistentProductId, storeId);
        assertFalse(productResponse.isSuccess());
        assertEquals("Error during getting product: Product with ID: 9999 does not exist in store ID: " + storeId, productResponse.getMessage());
    }

    @Test
    void testGuestUserAccessToProductInformation_StoreNotFound_Fail() {
        int nonExistentStoreId = 9999; 
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, nonExistentStoreId);
        assertFalse(productResponse.isSuccess());
        assertEquals("Error during getting product: Store not found", productResponse.getMessage());
    }

    @Test
    void testGuestUserAccessToProductInformation_ProductNotInStore_Fail() {
        int anotherStoreId = testHelper.openStore2(userId).getData(); // Open another store
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, anotherStoreId);
        assertFalse(productResponse.isSuccess());
        assertEquals("Error during getting product: Product with ID: " + productId + " does not exist in store ID: " + anotherStoreId, productResponse.getMessage());
    }
    //add test for closed store
    @Test
    void testGuestUserAccessToProductInformation_StoreIsClosed_Fail() {
        systemService.closeStoreByFounder(storeId, userId);
        // The store is closed

        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, storeId);
        assertFalse(productResponse.isSuccess());
        assertEquals("Store is closed", productResponse.getMessage());
    }

}