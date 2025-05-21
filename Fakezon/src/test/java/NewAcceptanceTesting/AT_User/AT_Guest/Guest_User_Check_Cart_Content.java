package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.StoreDTO;
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
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;

public class Guest_User_Check_Cart_Content {
    //Use-Case: 2.4 guest user check cart content

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
    private int productId;
    private int storeId;
    private int userId;
    
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
    void testGuestUserCheckCartContent_Success() {
        // Add the product to the cart
        Response<Void> addToCartResponse = systemService.addToBasket(userId, productId, storeId, 1);
        // Check if the product was added successfully
        assertTrue(addToCartResponse.isSuccess());

        // Check the cart content
        Response<Map<StoreDTO,Map<StoreProductDTO,Boolean>>> cartContentResponse = systemService.viewCart(userId);
        assertTrue(cartContentResponse.isSuccess());  
        assertEquals("Cart retrieved successfully", cartContentResponse.getMessage());  

        // Check if the cart contains the product
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> cartContent = cartContentResponse.getData();

        // Find the StoreDTO with the matching storeId
        StoreDTO store = cartContent.keySet().stream().filter(s -> s.getStoreId() == storeId).findFirst().orElse(null);
        assertNotNull(store);
        assertTrue(cartContent.containsKey(store));
    }

    @Test
    void testGuestUserCheckCartContent_EmpyCart_Failure() {
        // Attempt to check the cart content without adding any products
        Response<Map<StoreDTO,Map<StoreProductDTO,Boolean>>> cartContentResponse = systemService.viewCart(userId);
        assertFalse(cartContentResponse.isSuccess());  
        assertEquals("Cart is empty", cartContentResponse.getMessage());  
    }
}
