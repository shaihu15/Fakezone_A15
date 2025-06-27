package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;
import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class Guest_User_Check_Cart_Content {

    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    private int StoreFounderId;
    private int storeId;
    private int registeredId;
    private int guestId;
    private int productId1;
    private int productId2;
    private String founderEmail;
    private String registeredEmail;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        StoreFounderId = resultRegister1.getData().getUserId();
        founderEmail = resultRegister1.getData().getUserEmail();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "StoreSuccess");
        assertTrue(storeResult.isSuccess());
        storeId = storeResult.getData(); 
        System.out.println("storeId: " + storeId);
        //the store is open

        Response<UserDTO> guestResponse = systemService.createUnsignedUser();
        assertTrue(guestResponse.isSuccess());
        guestId = guestResponse.getData().getUserId();
        // guest is created 

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        registeredId = resultRegister2.getData().getUserId();
        registeredEmail = resultRegister2.getData().getUserEmail();
        // registered is registered and logged in

        Response<StoreProductDTO> storePResponse1 = testHelper.addProductToStore(storeId, StoreFounderId); 
        assertTrue(storePResponse1.isSuccess());
        productId1 = storePResponse1.getData().getProductId();
        //product 1 is added to the store

        Response<StoreProductDTO> storePResponse2 = testHelper.addProductToStore2(storeId, StoreFounderId); 
        assertTrue(storePResponse2.isSuccess());
        productId2 = storePResponse2.getData().getProductId();
        //product 2 is added to the store

        Response<Void> responseGuest = systemService.addToBasket(guestId, productId1, storeId,1); 
        assertTrue(responseGuest.isSuccess());
        // Guest adds product 1 to the cart

        Response<Void> responseRegistered = systemService.addToBasket(registeredId, productId2, storeId,1); 
        assertTrue(responseRegistered.isSuccess());
        // Registered user adds product 2 to the cart
    }

    @AfterEach
    void tearDown() {
        Response<String> deleteStoreResponse = systemService.closeStoreByFounder(storeId, StoreFounderId);
        assertTrue(deleteStoreResponse.isSuccess(), "Store deletion should succeed");
        Response<Boolean> deleteResponse = systemService.removeUnsignedUser(guestId);
        assertTrue(deleteResponse.isSuccess(), "Guest user deletion should succeed");
        Response<Boolean> deleteUserResponse = systemService.deleteUser(founderEmail);
        assertTrue(deleteUserResponse.isSuccess(), "Registered user deletion should succeed");
        Response<Boolean> deleteRegisteredResponse = systemService.deleteUser(registeredEmail);
        assertTrue(deleteRegisteredResponse.isSuccess(), "Registered user deletion should succeed");

    }

    @Test
    void testGuesUseChecCartContent_Success(){
        // Guest checks cart content
        Response<List<CartItemInfoDTO>> cartContentResponse = systemService.viewCart(guestId);
        assertTrue(cartContentResponse.isSuccess());
        assertEquals("Cart retrieved successfully", cartContentResponse.getMessage());
        List<CartItemInfoDTO> cartItems1 = cartContentResponse.getData();
        assertEquals(1, cartItems1.size());
        assertEquals(productId1, cartItems1.get(0).getProductId());


        //Registered user checks cart content
        Response<List<CartItemInfoDTO>> registeredCartContentResponse = systemService.viewCart(registeredId);   
        assertTrue(registeredCartContentResponse.isSuccess());
        assertEquals("Cart retrieved successfully", registeredCartContentResponse.getMessage());
        List<CartItemInfoDTO> cartItems2 = registeredCartContentResponse.getData();
        assertEquals(1, cartItems2.size());
        assertEquals(productId2, cartItems2.get(0).getProductId());
    }

    @Test
    void testGuestUserCheckCartContent_Failure() {
        // Guest tries to check cart content with an invalid user ID
        Response<List<CartItemInfoDTO>> invalidCartResponse = systemService.viewCart(9999);
        assertFalse(invalidCartResponse.isSuccess());
        assertEquals("Error during viewing cart: User not found", invalidCartResponse.getMessage());
    }

    @Test
    void testGuestUserCheckCartContent_EmptyCart() {
        Response<UserDTO> resultRegister3 = testHelper.register_and_login3();
        assertTrue(resultRegister3.isSuccess());
        int registeredId3 = resultRegister3.getData().getUserId();
        Response<List<CartItemInfoDTO>> emptyCartResponse = systemService.viewCart(registeredId3);
        assertFalse(emptyCartResponse.isSuccess());
        assertEquals("Cart is empty", emptyCartResponse.getMessage());
        assertNull(emptyCartResponse.getData());
    }
}
