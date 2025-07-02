package NewAcceptanceTesting.AT_User.AT_Guest;

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
import ApplicationLayer.Services.SystemService;

@SpringBootTest(classes = FakezoneApplication.class)
public class Save_Products_in_Purchase_Basket {
    //Use-case: 2.3 Save Products in Purchase Basket 

       @Autowired
    private SystemService systemService;
    private TestHelper testHelper;


    private int StoreFounderId;
    private int storeId;
    private int registeredId;
    private int productIdInt;
    private int guestId;


    @BeforeEach
    void setUp() {
        systemService.clearAllData(); // Clear data before each test to ensure isolation
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "StoreSuccess");
        assertTrue(storeResult.isSuccess());
        storeId = storeResult.getData(); 
        System.out.println("storeId: " + storeId);
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        registeredId = resultRegister2.getData().getUserId();
        // registered is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore2(storeId, StoreFounderId); 
        assertTrue(storePResponse.isSuccess());
        productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        // Guest enters the system
        Response<UserDTO> guestResponse = systemService.createUnsignedUser();
        assertTrue(guestResponse.isSuccess());
        guestId = guestResponse.getData().getUserId();

    }


    @Test
    void testProductAddition_GuestUser_Success() {
        Response<Void> response = systemService.addToBasket(guestId, productIdInt, storeId,1); 
        assertTrue(response.isSuccess());
        assertEquals("Product added to basket successfully", response.getMessage()); 
    }
 

    @Test
    void testProductAddition_RegisteredUser_Success() {
        Response<Void> response = systemService.addToBasket(registeredId, productIdInt, storeId,1); 
        assertTrue(response.isSuccess());
        assertEquals("Product added to basket successfully", response.getMessage()); 
    }
 
    @Test
    void testProductAddition_OutOfStockProduct_Failure() {
        Response<Void> response = systemService.addToBasket(registeredId,9 , storeId, 1); // Assuming productId 9 is out of stock

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Product with ID: 9 does not exist in store ID: "+storeId, response.getMessage());
    }

    @Test
    void testProductAddition_invalidStoreId_Failure() {
        Response<Void> response = systemService.addToBasket(registeredId,productIdInt , 9, 1); // Assuming storeId 9 is invalid

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Store not found", response.getMessage());
    }

    @Test
    void testProductAddition_productNotFromeStore_Failure() {
         Response<Integer> storeResult2 = testHelper.openStore(StoreFounderId);
        assertNotNull(storeResult2.getData());
        int storeId2 = storeResult2.getData(); 
        //the store2 is open
        Response<Void> response = systemService.addToBasket(registeredId, productIdInt, storeId2, 1); 

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Product with ID: " + productIdInt + " does not exist in store ID: " +storeId2, response.getMessage());

    }

}

