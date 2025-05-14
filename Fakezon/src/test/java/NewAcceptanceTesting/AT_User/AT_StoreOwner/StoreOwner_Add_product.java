package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
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

public class StoreOwner_Add_product {

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
        //Use-case: 4.1 StoreOwner - add a product

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
    void testAddProductToStore_validArguments_Success(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse);
        assertTrue(storePResponse.isSuccess());

        int productId = storePResponse.getData().getProductId();

        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        assertTrue(resultGetProduct.isSuccess());
        assertEquals(productName, resultGetProduct.getData().getName());
    }

    @Test
    void testAddProductToStore_invalidCategory_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData();
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        
        assertEquals("Invalid category",systemService.addProductToStore
                                        (storeId, userId, productName, productDescription, 1, 1, "invalidCategory").getMessage());
    }

    /* 
    //no test for invalid storeId in addProductToStore
    @Test
    void testAddProductToStore_invalidStoreId_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData();
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        
        assertEquals("error",systemService.addProductToStore
                                        (storeId, userId, productName, productDescription, 1, 1, category).getMessage());

    }
    
    //no test for invalid userId in addProductToStore
    @Test
    void testAddProductToStore_invalidUserId_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        
        assertEquals("error",systemService.addProductToStore
                                        (storeId, 9, productName, productDescription, 1, 1, category).getMessage());
    }

    //no test for invalid productId in addProductToStore
    @Test
    void testAddProductToStore_invalidProductName_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = ""; 
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        
        assertEquals("error",systemService.addProductToStore
                                        (storeId, userId, productName, productDescription, 1, 1, category).getMessage());   
    }                                  
   

    @Test
    void testAddProductToStore_invalidProductName_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        //String productName = ""; 
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        
        assertEquals("error",systemService.addProductToStore
                                        (storeId, userId, null, productDescription, 1, 1, category).getMessage());   
    } 
                                        
    */

    @Test
    void testAddProductToStore_ProductAlreadyExists_Failure(){
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult);
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult);
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();

        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse);
        assertTrue(storePResponse.isSuccess());
        int productId = storePResponse.getData().getProductId();

        assertEquals("Error during adding product to store: Product "+ productId+" is already in store "+storeId,
                        systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category).getMessage());

    }
}
