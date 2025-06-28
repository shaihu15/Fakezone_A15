package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.atmosphere.config.service.Delete;
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
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;

import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import NewAcceptanceTesting.TestHelper;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_Delete_product {
    //Use-case: 4.1 StoreOwner - Delete a product
    
     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int productId;

    @BeforeEach
    void setUp() {
        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!

        testHelper = new TestHelper(systemService);

        // Initialize the system with a store owner and a product
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertTrue(StoreOwnerResult.isSuccess());   
         
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertTrue(storeResult.isSuccess());
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse);
        assertTrue(storePResponse.isSuccess());

        productId = storePResponse.getData().getProductId();
        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        assertTrue(resultGetProduct.isSuccess()); 
    }

    @Test
    void testDeleteProduct_validArguments_Success() {        
        Response<Boolean> resultDeleteProduct = systemService.deleteProduct(productId);
        assertTrue(resultDeleteProduct.isSuccess());    
        assertTrue(resultDeleteProduct.getData());
    }   

    @Test
    void testDeleteProduct_invalidProductId_Failure() {
        int invalidProductId = -1; // Invalid product ID
        Response<Boolean> resultDeleteProduct = systemService.deleteProduct(invalidProductId);
        assertFalse(resultDeleteProduct.isSuccess());
        assertEquals(resultDeleteProduct.getMessage(),"Error during deleting product");
    }

    @Test
    void testDeleteProduct_productNotFound_Failure() {
        // Delete the product first
        Response<Boolean> resultDeleteProduct = systemService.deleteProduct(productId);
        assertTrue(resultDeleteProduct.isSuccess());    
        assertTrue(resultDeleteProduct.getData());

        // Try to delete the same product again
        Response<Boolean> resultDeleteProductAgain = systemService.deleteProduct(productId);
        assertFalse(resultDeleteProductAgain.isSuccess());
        assertEquals(resultDeleteProductAgain.getMessage(),"Error during deleting product");
    }
/* 
    @Test
    void testDeleteProduct_notStoreOwner_Failure() {
        // Register a new user (not a store owner)
        Response<UserDTO> newUserResult = testHelper.register_and_login2();
        assertTrue(newUserResult.isSuccess());
        int newUserId = newUserResult.getData().getUserId();

        // Try to delete the product as a different user
        Response<Boolean> resultDeleteProduct = systemService.deleteProduct(productId);
        assertNull(systemService.deleteProduct(productId));
        assertEquals(resultDeleteProduct.getMessage(),"Error during deleting product");
    }

    @Test
    void testDeleteProduct_notLoggedIn_Failure() {
        // Simulate a not logged-in user by creating a new SystemService instance
        SystemService notLoggedInSystemService = new SystemService(storeService, userService, productService, orderService, deliveryService, authenticatorService, paymentService, eventPublisher);

        // Try to delete the product without logging in
        Response<Boolean> resultDeleteProduct = notLoggedInSystemService.deleteProduct(productId);
        assertFalse(resultDeleteProduct.isSuccess());
        assertEquals(resultDeleteProduct.getMessage(),"Error during deleting product");
    }
*/

}
