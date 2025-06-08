package NewAcceptanceTesting.AT_User.AT_SystemAdministrator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.*;
import ApplicationLayer.Response;
import ApplicationLayer.Services.*;
import DomainLayer.IRepository.*;
import DomainLayer.Interfaces.*;
import InfrastructureLayer.Adapters.*;
import InfrastructureLayer.Repositories.*;
import NewAcceptanceTesting.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

public class SystemAdministrator_initMarket {

    private SystemService systemService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private IOrderRepository orderRepository;
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private ApplicationEventPublisher eventPublisher;
    private IStoreService storeService;
    private IProductService productService;
    private IUserService userService;
    private IOrderService orderService;
    private INotificationWebSocketHandler notificationWebSocketHandler;
    private TestHelper testHelper;

    int storeId;
    int userId;
    String storeName;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepository();
        userRepository = new UserRepository();
        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();
        paymentService = new PaymentAdapter();
        deliveryService = new DeliveryAdapter();

        storeService = new StoreService(storeRepository, eventPublisher);
        userService = new UserService(userRepository);
        orderService = new OrderService(orderRepository);
        productService = new ProductService(productRepository);
        authenticatorService = new AuthenticatorAdapter(userService);

        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService,
                authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();
        storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        storeId = resultAddStore.getData();
    }

    @Test
    void testSuccessfulSystemInitialization() {
        // Setup
        Response<UserDTO> adminResponse = testHelper.register_and_login();
        assertNotNull(adminResponse);
        int adminId = adminResponse.getData().getUserId();
        systemService.addSystemAdmin(userId, adminId);

        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        assertTrue(systemService.isSystemAdmin(adminId).getData());

    }

    @Test
    void testNoAdminSystemInitialization() {


        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        Response<Integer> adminCount = systemService.getSystemAdminCount(userId);
        assertTrue(adminCount.getData() > 0);

    }

    @Test
    void testNoServicesSystemInitialization() {
        // Setup
        Response<UserDTO> adminResponse = testHelper.register_and_login();
        assertNotNull(adminResponse);
        int adminId = adminResponse.getData().getUserId();
        systemService.addSystemAdmin(userId, adminId);

        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        assertTrue(systemService.isSystemAdmin(adminId).getData());

    }
}