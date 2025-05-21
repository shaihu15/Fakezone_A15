package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.Enums.StoreManagerPermission;
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
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import NewAcceptanceTesting.TestHelper;

public class StoreOwner_Owner_Appointment {

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

    private int OwnerUserId;
    private int storeId;
    private int ManagerUserId;

    @BeforeEach
    void setUp() {
        storeRepository = new StoreRepository();
        userRepository = new UserRepository();
        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();
        paymentService = new PaymentAdapter();
        deliveryService = new DeliveryAdapter();
        notificationWebSocketHandler = new NotificationWebSocketHandler();
        eventPublisher = mock(ApplicationEventPublisher.class);
        storeService = new StoreService(storeRepository, eventPublisher);
        userService = new UserService(userRepository);
        orderService = new OrderService(orderRepository);
        productService = new ProductService(productRepository);
        authenticatorService = new AuthenticatorAdapter(userService);
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService, authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);

        Response<UserDTO> OwnerUser = testHelper.register_and_login();
        assertTrue(OwnerUser.isSuccess());
        OwnerUserId = OwnerUser.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(OwnerUserId, "TestStore");
        assertTrue(storeRes.isSuccess());
        storeId = storeRes.getData();

        Response<UserDTO> ManagerUser = testHelper.register_and_login2();
        assertTrue(ManagerUser.isSuccess());
        ManagerUserId = ManagerUser.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
            Response<Void> addManagerRes = systemService.addStoreManager(storeId, OwnerUserId, ManagerUserId, perms);
        assertTrue(addManagerRes.isSuccess());


        // The new manager accepts the assignment
        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());


    }

    @Test
    void testAddStoreOwner_Success() {
        
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        //problems with the EventListener 
        // Response<HashMap<Integer, String>> AssignmentMessagesRes= systemService.getAssignmentMessages(ManagerUserId);
        // assertTrue(AssignmentMessagesRes.isSuccess());
        // assertTrue(AssignmentMessagesRes.getData().keySet().contains(storeId));
    }

    @Test
    void testAddStoreOwner_ManagerAccept_Success() {
        
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());
        
        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(ManagerUserId));

    }

        @Test
    void testAddStoreOwner_ManagerDecline_Failure() {
        
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> declineRes = systemService.declineAssignment(storeId, ManagerUserId);
        assertTrue(declineRes.isSuccess());
        
        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertFalse(storeRolesData.getStoreOwners().contains(ManagerUserId));
    }

    @Test
    void testAddStoreOwner_FailureInvalidUser() {
        int invalidUserId = -999;

        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, invalidUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store owner"));
    }

    @Test
    void testAddStoreOwner_FailureInvalidStore() {
        int invalidStoreId = -123;

        Response<Void> response = systemService.addStoreOwner(invalidStoreId, OwnerUserId, ManagerUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store owner"));
    }

    
}  
