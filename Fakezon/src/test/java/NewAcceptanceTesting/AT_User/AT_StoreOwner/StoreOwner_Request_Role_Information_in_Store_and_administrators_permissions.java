package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
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

public class StoreOwner_Request_Role_Information_in_Store_and_administrators_permissions {
        
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
    private INotificationWebSocketHandler notificationWebSocketHandler;
    private TestHelper testHelper;

    int storeId;
    int storeOwnerId;

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
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService, authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);

        // Initialize the system with a store owner and a product
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertTrue(StoreOwnerResult.isSuccess());   
         
        storeOwnerId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(storeOwnerId, "Store1");
        assertTrue(storeResult.isSuccess());
        storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

    }

    @Test
    void testRequestRoleInformation_Success() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, storeOwnerId);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        StoreRolesDTO storeRoles = result.getData();
        boolean foundOwner = storeRoles.getStoreOwners().stream().anyMatch(ownerId -> ownerId == storeOwnerId);
        assertTrue(foundOwner, "Store owner should be present in the user roles list");
   
        //add a manager to the store and test if the manager is present in the user roles list
    }

    @Test
    void testRequestRoleInformation_InvalidStoreId_Failue() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(-1, storeOwnerId);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: Store not found", result.getMessage());
    }

    @Test
    void testRequestRoleInformation_InvalidUserId_Failure() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, -1);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: User not found", result.getMessage());
    }

    @Test
    void testRequestRoleInformation_UserNotStoreOwner_Failure() {
        // Create a new user and try to get store roles
        Response<UserDTO> newUserResult = testHelper.register_and_login2();
        assertTrue(newUserResult.isSuccess());
        int newUserId = newUserResult.getData().getUserId();

        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, newUserId);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: User with id: " + newUserId + " has insufficient permissions for store ID: " + storeId, result.getMessage());
    }

    @Test
    void testRequestRoleInformation_Request_administrators_permissions_Success() {
        //no function for that yet
        
    }



}
