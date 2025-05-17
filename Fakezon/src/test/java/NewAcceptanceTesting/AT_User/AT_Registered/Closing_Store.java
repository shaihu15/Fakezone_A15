package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
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

public class Closing_Store {
    //Use-case: 4.9 Closing Store

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
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService, authenticatorService, paymentService, eventPublisher);
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();
        storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        storeId = resultAddStore.getData();
    }

    @Test
    void testCloseStore_validArguments_Success() {
        Response<String> result = systemService.closeStoreByFounder(storeId, userId);
        assertTrue(result.isSuccess());
        assertEquals("Store closed successfully", result.getMessage());

        Response<String> result2 = systemService.closeStoreByFounder(storeId, userId);
        assertFalse(result2.isSuccess());
        assertEquals("Error during closing store: Store: 1 is already closed", result2.getMessage());
    }

    @Test
    void testCloseStore_invalidStoreId_Failure() {
        Response<String> result = systemService.closeStoreByFounder(-1, userId);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: Store not found", result.getMessage());
    }

    @Test
    void testCloseStore_invalidUserId_Failure() {
        Response<String> result = systemService.closeStoreByFounder(storeId, -1);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: User not found", result.getMessage());
    }

    @Test
    void testCloseStore_userNotFounder_Failure() {
        Response<UserDTO> resultUser = testHelper.register_and_login2();
        int notFoundeId = resultUser.getData().getUserId();

        Response<String> result = systemService.closeStoreByFounder(storeId, notFoundeId);
        assertFalse(result.isSuccess());
        assertEquals("Error during closing store: Requester ID: " + notFoundeId + " is not a Store Founder of store: " + storeId, result.getMessage());
    }


}
