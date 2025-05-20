package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import DomainLayer.Interfaces.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
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
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import InfrastructureLayer.Security.TokenService;
import NewAcceptanceTesting.TestHelper;

public class Guest_User_Access_to_Product_Information {
    // Use-case: 2.1 Guest User Access to Product Information
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
    private TokenService tokenService;

    private TestHelper testHelper;
    int storeId;
    int userId;
    int productId;

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
        systemService = new SystemService(storeService, userService, productService, orderService,
                deliveryService, authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);
        tokenService = new TokenService(); 
                
        // Register a guest user
        Response<UserDTO> registerResponse = testHelper.register_and_login();
        assertTrue(registerResponse.isSuccess());
        userId = registerResponse.getData().getUserId();
        // Open a store
        Response<Integer> resultAddStore = testHelper.openStore(userId);
        assertTrue(resultAddStore.isSuccess());
        storeId = resultAddStore.getData();
        // Add a product to the store
        Response<StoreProductDTO> resultAddProduct = testHelper.addProductToStore(storeId, userId);
        assertTrue(resultAddProduct.isSuccess());
        productId = resultAddProduct.getData().getProductId();
    }

    @Test
    void testGuestUserAccessToProductInformation_Succsses() {
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, storeId);
        assertTrue(productResponse.isSuccess());
        assertEquals(productId, productResponse.getData().getProductId());
    }

    @Test
    void testGuestUserAccessToProductInformation_storeIsClose_Fail() {
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(productId, -1);
        assertFalse(productResponse.isSuccess());
        assertEquals("Error during getting product: Store not found", productResponse.getMessage());
    }

    @Test
    void testGuestUserAccessToProductInformation_productNotInStore_Fail() {
        Response<StoreProductDTO> productResponse = systemService.getProductFromStore(-1, storeId);
        assertFalse(productResponse.isSuccess());
        assertEquals("Error during getting product: Product with ID: -1 does not exist in store ID: " + storeId, productResponse.getMessage());

    }


}
