package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.UserDTO;
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

import InfrastructureLayer.Security.TokenService;

import NewAcceptanceTesting.TestHelper;

public class Guest_User_Access_to_Store {
    // Use-case: 2.1 Guest User Access to Store
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

    int storeOwnerId;
    int storeId;
    int productId;

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
        storeOwnerId = resultUser.getData().getUserId();

        Response<Integer> resultAddStore = testHelper.openStore();
        storeId = resultAddStore.getData();
        

        Response<StoreProductDTO> resultAddProduct = testHelper.addProductToStore(storeId, storeOwnerId);
        productId = resultAddProduct.getData().getProductId();



    }
/* 
    @Test
    void testGuestUserAccessStore_Succsses() {
        //Response<StoreProductDTO> result = systemService.userAccessStore("guest", storeId);
        assertTrue(result.isSuccess());
        assertEquals(storeId, result.getData().getStoreId());
        assertEquals(productId, result.getData().getProductId());
    }*/

    void testGuestUserAccessStore_StoreIsClose_Fail() {
        Response<UserDTO> resultRegister = testHelper.register_and_login();
        assertNotNull(resultRegister);
        int userId = resultRegister.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> resultAddStore = testHelper.openStore(userId);
        assertNotNull(resultAddStore);
        int storeId = resultAddStore.getData();
        assertTrue(storeRepository.findById(storeId).isOpen());
        //the store is open

        systemService.closeStoreByFounder(userId, storeId);
        assertFalse(storeRepository.findById(storeId).isOpen());
        //the store is closed
         
        String guestToken = authenticatorService.generateGuestToken(); 
        assertNotNull(guestToken);
        Response<StoreDTO> accessStoreResponse = systemService.userAccessStore( -1); 

        assertFalse(accessStoreResponse.isSuccess());
    }

}
