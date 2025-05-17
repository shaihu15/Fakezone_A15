package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;

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

public class User_Logout {
//Use-case: 3.1 User Logout

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

        storeService = new StoreService(storeRepository, eventPublisher);
        userService = new UserService(userRepository);
        orderService = new OrderService(orderRepository);
        productService = new ProductService(productRepository);
        authenticatorService = new AuthenticatorAdapter(userService);
        systemService = new SystemService(storeService, userService, productService, orderService,
                deliveryService, authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);
    }
 
    @Test
    void testUserLogout_Success() {
        Response<UserDTO> userResponse = testHelper.register_and_login();
        int userId = userResponse.getData().getUserId();

        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess());

       assertFalse(systemService.getUnsignedUserById(userId).isSuccess());
    }

    @Test
    void testUserLogout_Failure() {
        Response<UserDTO> userResponse = testHelper.register_and_login();
        int userId = userResponse.getData().getUserId();

        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess());

        // Attempt to logout again
        Response<Void> resultLogoutAgain=systemService.userLogout(userId);
        assertFalse(resultLogoutAgain.isSuccess());
    }



}
