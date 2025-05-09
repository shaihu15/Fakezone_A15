package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import NewAcceptanceTesting.TestHelper; 


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

public class User_Registration {
    //Use-case: 1.3 User Registration

    private SystemService systemService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private IOrderRepository orderRepository;
    private IDelivery   deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private ApplicationEventPublisher eventPublisher;
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
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService, authenticatorService, paymentService, eventPublisher);
        testHelper = new TestHelper(systemService);
    }

    @Test
    void testRegisterUser_validArguments_Success() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );

        assertTrue(result.isSuccess());
        assertEquals("Guest registered successfully", result.getMessage());
    }
/*
    @Test
    void testRegisterUser_invalidEmail_Failure() {
        String invalidEmail = testHelper.invalidEmail();
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(invalidEmail, validPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
    }
*/
    @Test
    void testRegisterUser_invalidPassword_Failure() {
        String validEmail = testHelper.validEmail();
        String invalidPassword = testHelper.invalidPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(validEmail, invalidPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid password", result.getMessage());
    }
    
    @Test
    void testRegisterUser_invalidBirthDate_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String invalidBirthDay = testHelper.invalidBirthDate();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, invalidBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid date of birth format. Expected format: YYYY-MM-DD", result.getMessage());
    }
    
    @Test
    void testRegisterUser_invalidCountry_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String invalidCountry = testHelper.invalidCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, invalidCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid country code", result.getMessage());
    }
/*
    @Test
    void testRegisterUser_AlreadyRegistered_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        Response<String> user1 = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        Response<String> user2 = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );


        assertFalse(user2.isSuccess());
        assertEquals("User already registered", user2.getMessage());
    }
*/
/*
    @Test
    void testRegisterUser_EmptyEmail_Failure() {
        String emptyEmail = "";
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(emptyEmail, validPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid email format", result.getMessage());
    }
*/
    @Test
    void testRegisterUser_EmptyPassword_Failure() {
        String validEmail = testHelper.validEmail();
        String emptyPassword = "";
        String validBirthDay = testHelper.validBirthDate_Over18();
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(validEmail, emptyPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid password", result.getMessage());
    }
    @Test

    void testRegisterUser_EmptyBirthDate_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String emptyBirthDay = "";
        String validCountry = testHelper.validCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, emptyBirthDay, validCountry );
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid date of birth format. Expected format: YYYY-MM-DD", result.getMessage());
    }
    
    @Test
    void testRegisterUser_EmptyCountry_Failure() {
        String validEmail = testHelper.validEmail();
        String validPassword = testHelper.validPassword();
        String validBirthDay = testHelper.validBirthDate_Over18();
        String emptyCountry = "";
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, emptyCountry );
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid country code", result.getMessage());
    }
    
}
