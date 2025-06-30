package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
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
import DomainLayer.Enums.PaymentMethod;
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
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class Product_ratingTest {
     //Use-case: 3.4 Product rating
    
    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;
    
    int registeredId;
    int storeId;
    int productId;

    @BeforeEach
    void setUp() {

        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);

        Response<UserDTO> storeOwner = testHelper.register_and_login();
        int storeOwnerId = storeOwner.getData().getUserId();

        String storeName = "Test Store";
        Response<Integer> resultAddStore = systemService.addStore(storeOwnerId, storeName);
        assertTrue(resultAddStore.isSuccess());
        storeId = resultAddStore.getData();

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        Response<StoreProductDTO> resultAddProduct = systemService.addProductToStore(storeId, storeOwnerId, productName, productDescription, 1,100, category);
        assertTrue(resultAddProduct.isSuccess());
        productId = resultAddProduct.getData().getProductId();

        Response<UserDTO> registered = testHelper.register_and_login2();
        registeredId = registered.getData().getUserId();

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
    }

    @Test
    void testStoreRating_validArguments_Success() {
        double rating = 4.5;
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, registeredId, rating, comment);
        assertTrue(result.isSuccess());
        assertEquals("Product rated successfully", result.getMessage()); 
    }

    @Test
    void testStoreRating_invalidArguments_Failure() {
        double rating = 6.0; // Invalid rating
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, registeredId, rating, comment);
        assertFalse(result.isSuccess());
        assertEquals("Invalid rating value", result.getMessage()); 
    }

    @Test
    void testStoreRating_emptyComment_Success() {
        double rating = 4.5;
        String comment = ""; // Empty comment
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, registeredId, rating, comment);
        assertTrue(result.isSuccess());
        assertEquals("Product rated successfully", result.getMessage());
    }

    @Test
    void testStoreRating_nullComment_Success() {
        double rating = 4.5;
        String comment = null; // Null comment
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, registeredId, rating, comment);
        assertTrue(result.isSuccess());
        assertEquals("Product rated successfully", result.getMessage());
    }

    @Test
    void testStoreRating_invalidStoreId_Failure() {
        double rating = 4.5;
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(-1, productId, registeredId, rating, comment);
        assertFalse(result.isSuccess());
        assertEquals("Error during rating product: Store not found", result.getMessage()); 
    }

    @Test
    void testStoreRating_invalidUserId_Failure() {
        double rating = 4.5;
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, -1, rating, comment);
        assertFalse(result.isSuccess());
        assertEquals("Error during rating product: User not found", result.getMessage()); 
    }

    @Test
    void testStoreRating_invalidProductId_Failure() {
        double rating = 4.5;
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(storeId, -1, registeredId, rating, comment);
        assertFalse(result.isSuccess());
        assertEquals("Error during rating product: Product not found in the repository.", result.getMessage()); 
    }

    @Test
    void testStoreRating_userDidNotPurchase_Failure() {
        Response<UserDTO> registered = testHelper.register_and_login3();
        int registeredNotPurchaseId = registered.getData().getUserId();

        double rating = 4.5;
        String comment = "Great store!";
        Response<Void> result = systemService.ratingStoreProduct(storeId, productId, registeredNotPurchaseId, rating, comment);
        assertFalse(result.isSuccess());
        assertEquals("User did not purchase that productfrom the store", result.getMessage()); 



    }
}
