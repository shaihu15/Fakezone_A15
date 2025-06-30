package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.vaadin.flow.data.provider.ArrayUpdater.Update;

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
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_Update_product_detailsTest {
    //Use-case: 4.1 StoreOwner -Update product details

     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int userId;
    int storeId;
    String productName;
    String productDescription;
    String category;
    int productId;
    String newProductName;
    String newProductDescription;
    double newBasePrice;
    int newQuantity;


    @BeforeEach
    void setUp() {
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
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertTrue(storePResponse.isSuccess());
        productId = storePResponse.getData().getProductId();

        newProductName = "Updated Product";
        newProductDescription = "Updated Description";
        newBasePrice = 10.0;
        newQuantity = 5;
    }

    @Test
    void testUpdateProductDetails_validDetails_Success() {
        Response<Void> updateResponse = systemService.updateProductInStore(storeId, userId, productId, newBasePrice, newQuantity);
        assertTrue(updateResponse.isSuccess());

        assertEquals(systemService.getProductFromStore(productId, storeId).getData().getQuantity(), newQuantity);
        assertEquals(systemService.getProductFromStore(productId, storeId).getData().getBasePrice(), newBasePrice);
    }

    @Test
    void testUpdateProductDetails_invalidProductId_Failure() {
        int invalidProductId = -1;
        Response<Void> updateResponse = systemService.updateProductInStore(storeId, userId, invalidProductId, newBasePrice, newQuantity);
        assertFalse(updateResponse.isSuccess());
        assertEquals("Error during updating product in store: Product not found in the repository.", updateResponse.getMessage());
    }

    @Test
    void testUpdateProductDetails_invalidStoreId_Failure() {
        int invalidStoreId = -1;
        Response<Void> updateResponse = systemService.updateProductInStore(invalidStoreId, userId, productId, newBasePrice, newQuantity);
        assertFalse(updateResponse.isSuccess());
        assertEquals("Error during updating product in store: Store not found", updateResponse.getMessage());
    }

    @Test
    void testUpdateProductDetails_productNotInStore_Failure() {
        Response<Integer> storeResult = systemService.addStore(userId, "Store2");
        assertNotNull(storeResult);
        int storeId2 = storeResult.getData(); 
        // StoreOwner is the owner of Store2

        int anotherStoreId = storeId2;
        Response<Void> updateResponse = systemService.updateProductInStore(anotherStoreId, userId, productId, newBasePrice, newQuantity);
        assertFalse(updateResponse.isSuccess());
        assertEquals("Error during updating product in store: Product "+productId+" is not in store "+storeId2, updateResponse.getMessage());
    }

    @Test
    void testUpdateProductDetails_invalidUserId_Failure() {
        int invalidUserId = -1;
        Response<Void> updateResponse = systemService.updateProductInStore(storeId, invalidUserId, productId, newBasePrice, newQuantity);
        assertFalse(updateResponse.isSuccess());
        assertEquals("Error during updating product in store: User -1 has insufficient inventory permissions for store "+storeId, updateResponse.getMessage());
    }

    @Test
    void testUpdateProductDetails_invalidBasePrice_Failure() {
        double invalidBasePrice = -10.0;
        Response<Void> updateResponse = systemService.updateProductInStore(storeId, userId, productId, invalidBasePrice, newQuantity);
        assertFalse(updateResponse.isSuccess());
        assertEquals("Error during updating product in store: Product's base price must be greater than 0", updateResponse.getMessage());
    }

    @Test
    void testUpdateProductDetails_sameDetails_Success() {
        Response<Void> updateResponse = systemService.updateProductInStore(storeId, userId, productId, newBasePrice, newQuantity);
        assertTrue(updateResponse.isSuccess());

        Response<Void> updateResponse2 = systemService.updateProductInStore(storeId, userId, productId, newBasePrice, newQuantity);
        assertTrue(updateResponse2.isSuccess());
    }

}
