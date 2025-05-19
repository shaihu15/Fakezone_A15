package NewAcceptanceTesting.AT_User.AT_Guest;


import static org.junit.jupiter.api.Assertions.*;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
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
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import java.util.AbstractMap;

public class Guest_Login {
    //Use-case: 1.4 Guest Login

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
    void testLoginUser_validCredentials_Success() {
        String email = testHelper.validEmail();
        String password = testHelper.validPassword();
        
        Response<String> registerResponse = testHelper.registerUser(email, password, testHelper.validBirthDate_Over18(), testHelper.validCountry());
        assertTrue(registerResponse.isSuccess()); // User should be registered successfully
        
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);

        assertTrue(loginResponse.isSuccess());
        //assertTrue(loginResponse.getMessage().contains("Login successful with token: "),"Login successful with token: ");    
    }
    
    @Test
    void testLoginUser_mismatchEmail_Failure() {
        String validEmail = testHelper.validEmail();
        String validEmail2 = testHelper.validEmail2();
        String validPassword = testHelper.validPassword();

        Response<String> registerResponse = testHelper.registerUser(validEmail, validPassword, testHelper.validBirthDate_Over18(), testHelper.validCountry());
        assertTrue(registerResponse.isSuccess()); // User should be registered successfully

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail2, validPassword);

        assertFalse(loginResponse.isSuccess());
        //assertTrue(loginResponse.getMessage().contains("Login failed"), "Expected login failure message but got: " + loginResponse.getMessage());
    }
    
    @Test
    void testLoginUser_mismatchPassword_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String validPassword2 = testHelper.validPassword2();


        Response<String> registerResponse = testHelper.registerUser(validEmail, validPassword, testHelper.validBirthDate_Over18(), testHelper.validCountry());
        assertTrue(registerResponse.isSuccess()); // User should be registered successfully

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword2);

        assertFalse(loginResponse.isSuccess());
        //assertTrue(loginResponse.getMessage().contains("Login failed"), "Expected login failure message but got: " + loginResponse.getMessage());
    }
    
    @Test
    void testLoginUser_emptyEmail_Failure() {
        String validEmail = testHelper.validEmail();
        String emptyEmail = "";
        String validPassword = testHelper.validPassword();

        Response<String> registerResponse = testHelper.registerUser(validEmail, validPassword, testHelper.validBirthDate_Over18(), testHelper.validCountry());
        assertTrue(registerResponse.isSuccess()); // User should be registered successfully

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(emptyEmail, validPassword);
        assertFalse(loginResponse.isSuccess());
    }

    @Test
    void testLoginUser_emptyPassword_Failure() {
        String validEmail = testHelper.validEmail();
        String emptyPassword = "";
        String validPassword = testHelper.validPassword();

        Response<String> registerResponse = testHelper.registerUser(validEmail, validPassword, testHelper.validBirthDate_Over18(), testHelper.validCountry());
        assertTrue(registerResponse.isSuccess()); // User should be registered successfully

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, emptyPassword);
        assertFalse(loginResponse.isSuccess());    
    }
    
}
