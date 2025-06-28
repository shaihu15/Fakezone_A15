package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.*;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Interfaces.*;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class StoreOwner_Add_product {
    //Use-case: 4.1 StoreOwner - Add a product

     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int storeId;
    private int userId;
    private String productName;
    private String productDescription;
    private String category;

    @BeforeEach
    void setUp() {
        //Use-case: 4.1 StoreOwner - add a product

        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!

        testHelper = new TestHelper(systemService);

        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        productName = "Test Product";
        productDescription = "Test Description";
        category = PCategory.ELECTRONICS.toString();
    }

    @Test
    void testAddProductToStore_validArguments_Success(){
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        //assertNotNull(storePResponse);
        assertEquals("Product added to store successfully",storePResponse.getMessage());
        assertTrue(storePResponse.isSuccess());

        int productId = storePResponse.getData().getProductId();

        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        assertTrue(resultGetProduct.isSuccess());
        assertEquals(productName, resultGetProduct.getData().getName());
    }

    @Test
    void testAddProductToStore_invalidCategory_Failure(){
        assertEquals("Invalid category",systemService.addProductToStore
                                        (storeId, userId, productName, productDescription, 1, 1, "invalidCategory").getMessage());
    }

    @Test
    void testAddProductToStore_invalidStoreId_Failure(){     
        assertEquals("Error during adding product to store: Store not found",systemService.addProductToStore
                                        (-1, userId, productName, productDescription, 1, 1, category).getMessage());
    }
    
    @Test
    void testAddProductToStore_invalidUserId_Failure(){
        assertEquals("Error during adding product to store: User with id: 9 has insufficient permissions for store ID: "+storeId,
                systemService.addProductToStore(storeId, 9, productName, productDescription, 1, 1, category).getMessage());
    }

    @Test
    void testAddProductToStore_emptyProductName_Failure(){
        assertEquals("Product name must not be empty",systemService.addProductToStore
                                        (storeId, userId, "", productDescription, 1, 1, category).getMessage());   
    }                                  
   
    @Test
    void testAddProductToStore_nullProductName_Failure(){
        assertEquals("Product name must not be empty",systemService.addProductToStore
                                        (storeId, userId, null, productDescription, 1, 1, category).getMessage());   
    } 
        
    @Test
    void testAddProductToStore_emptyProductDescription_Failure(){
        assertEquals("Product description must not be empty",systemService.addProductToStore
                                        (storeId, userId, productName, "", 1, 1, category).getMessage());   
    }                                  
   
    @Test
    void testAddProductToStore_nullProductDescription_Failure(){
        assertEquals("Product description must not be empty",systemService.addProductToStore
                                        (storeId, userId, productName, null, 1, 1, category).getMessage());   
    } 
                                        
    @Test
    void testAddProductToStore_ProductAlreadyExists_Failure(){
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse);
        assertTrue(storePResponse.isSuccess());
        assertEquals("Product added to store successfully",storePResponse.getMessage());
        int productId = storePResponse.getData().getProductId();
        Response<ProductDTO> productResponse = systemService.getProduct(productId);
        assertNotNull(productResponse);
        assertTrue(productResponse.isSuccess());
        assertEquals(productName, productResponse.getData().getName());


        Response<StoreProductDTO> productResponse2 = systemService.getProductFromStore(productId, storeId);
        //assertNotNull(productResponse2);
        //assertTrue(productResponse2.isSuccess());
        //assertEquals(productName, productResponse2.getData().getName());


        Response<StoreProductDTO> storePResponse2 = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        //assertEquals("Error during adding product to store: Product "+ productId+" is already in store "+storeId,storePResponse2.getMessage());
    }
}
