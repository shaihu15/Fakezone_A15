package NewAcceptanceTesting.AT_User.AT_Guest;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
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
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;

public class Save_Products_in_Purchase_Basket {
    //Use-case: 2.3 Save Products in Purchase Basket 

    private SystemService systemService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private IOrderRepository orderRepository;
    private IDelivery   deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private ApplicationEventPublisher eventPublisher;
    private INotificationWebSocketHandler notificationWebSocketHandler;
    private IStoreService storeService;
    private IProductService productService;
    private IUserService userService;
    private IOrderService orderService;

    private TestHelper testHelper;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepository();
        userRepository = new UserRepository();
        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();
        paymentService = new PaymentAdapter();
        deliveryService = new DeliveryAdapter();
        notificationWebSocketHandler = new NotificationWebSocketHandler();
        storeService = new StoreService(storeRepository, eventPublisher);
        userService = new UserService(userRepository);
        orderService = new OrderService(orderRepository);
        productService = new ProductService(productRepository);
        authenticatorService = new AuthenticatorAdapter(userService);
        systemService = new SystemService(storeService, userService, productService,
        orderService, deliveryService, authenticatorService,
        paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);
    }



    @Test
    void testProductAddition_RegisteredUser_Success() {
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "StoreSuccess");
        assertTrue(storeResult.isSuccess());
        int storeId1 = storeResult.getData(); 
        System.out.println("storeId1: " + storeId1);
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore2(storeId1, StoreFounderId); 
        assertTrue(storePResponse.isSuccess());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<Void> response = systemService.addToBasket(registeredId, storeId1, productIdInt, 1); 

        assertTrue(response.isSuccess());
        assertEquals("Product added to basket successfully", response.getMessage()); 
    }
 
    @Test
    void testSuccessfulProductAddition_OutOfStockProduct_Failure() {
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Void> response = systemService.addToBasket(registeredId,9 , storeId, 1); // Assuming productId 9 is out of stock

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Product with ID: 9 does not exist in store ID: "+storeId, response.getMessage());
    }

    @Test
    void testSuccessfulProductAddition_invalidStoreId_Failure() {
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); 
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Void> response = systemService.addToBasket(registeredId,productIdInt , 9, 1); // Assuming storeId 9 is invalid

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Store not found", response.getMessage());
    }

    @Test
    void testSuccessfulProductAddition_productNotFromeStore_Failure() {
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store1 is open

        Response<Integer> storeResult2 = systemService.addStore(StoreFounderId, "Store2");
        assertNotNull(storeResult2.getData());
        int storeId2 = storeResult2.getData(); 
        //the store1 is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // user is registered and logged in


        Response<StoreProductDTO> storePResponse2 = testHelper.addProductToStore2(storeId2, StoreFounderId); 
        int productIdInt2 = storePResponse2.getData().getProductId();
        //the product2 is added to the store2

        Response<Void> response = systemService.addToBasket(registeredId, storeId, productIdInt2, 1); 

        assertFalse(response.isSuccess());
        assertEquals("Error during adding to basket: Product with ID: 1 does not exist in store ID: 1", response.getMessage());
    }
}

