package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

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
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
public class Open_Store {
    //Use-case: 3.2 Open Store 

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
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService,
                authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);
    }

    @Test
    void testOpenStore_validArguments_Success() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        int userId = resultUser.getData().getUserId();
        String storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        int storeId = resultAddStore.getData();

        assertNotNull(resultAddStore.getData());
        assertEquals(storeRepository.findByName(storeName).getId(), storeId);
        assertTrue(storeRepository.findByName(storeName).isOpen());
        assertTrue(storeRepository.findById(resultAddStore.getData()).isOpen());

    }

    @Test
    void testOpenStore_StoreNameAlreadyTaken_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        int userId = resultUser.getData().getUserId();

        Response<Integer> resultAddStore1 = systemService.addStore(userId, "Test Store");
        Response<Integer> resultAddStore2 = systemService.addStore(userId, "Test Store");
        int storeId1 = resultAddStore1.getData();

        assertNotNull(storeId1);
        assertTrue(storeRepository.findById(storeId1).isOpen()); //store1 is open

        assertNull(resultAddStore2.getData());//store2 dont get id - not open
        assertEquals("Error during opening store: Store name already exists", resultAddStore2.getMessage());
    }

    @Test
    void testOpenStore_StoreNameIsEmpty_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        int userId = resultUser.getData().getUserId();

        String invalidStoreName = ""; 

        Response<Integer> resultAddStore = systemService.addStore(userId, invalidStoreName);

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: Store name is empty", resultAddStore.getMessage());
    }

        @Test
    void testOpenStore_StoreNameIsNull_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login();
        int userId = resultUser.getData().getUserId();


        Response<Integer> resultAddStore = systemService.addStore(userId, null);

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: Store name is empty", resultAddStore.getMessage());
    }
 
    @Test
    void testOpenStore_UserNotRegistered_Failure() {
        int userId = 9999; // Assuming this user ID does not exist

        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");

        assertNull(resultAddStore.getData());
        assertEquals("Error during opening store: User not found", resultAddStore.getMessage());
    }

}
