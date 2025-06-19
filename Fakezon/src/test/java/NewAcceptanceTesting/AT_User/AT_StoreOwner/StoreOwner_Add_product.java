package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import DomainLayer.Interfaces.*;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_Add_product {
    //Use-case: 4.1 StoreOwner - Add a product

     @Autowired
    private SystemService systemService;


    private TestHelper testHelper;

    int storeId;
    int userId;
    String productName;
    String productDescription;
    String category;

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
        int productId = storePResponse.getData().getProductId();

        assertEquals("Error during adding product to store: Product "+ productId+" is already in store "+storeId,
                        systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category).getMessage());

    }
}
