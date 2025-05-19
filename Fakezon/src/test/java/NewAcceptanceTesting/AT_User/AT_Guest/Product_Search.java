package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

import DomainLayer.Interfaces.*;
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
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import InfrastructureLayer.Security.TokenService;
import NewAcceptanceTesting.TestHelper;

public class Product_Search {
    //Use-case: 2.2 Product Search

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
    private TokenService tokenService;


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
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService,
                authenticatorService, paymentService, eventPublisher, notificationWebSocketHandler);
        testHelper = new TestHelper(systemService);
        tokenService = new TokenService(); 

    }

    @Test
    void testSearchByCategory_validCategoryExistingProduct_Success() {
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult.getData());
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse.getData());
        int productId = storePResponse.getData().getProductId();
        // StoreOwner added a product to Store1
        
        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        String category_toTset = resultGetProduct.getData().getCategory().name();
        Response<List<ProductDTO>> result = systemService.searchByCategory(category_toTset);
        assertNotNull(result.getData());
        assertTrue(result.isSuccess());
        assertEquals(productName, result.getData().get(0).getName());
    }

    @Test
    void testSearchByCategory_invalidCategory_Failure() {
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult.getData());
        int userId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        String nonExistingCategory = "nonExistingCategory"; // Non-existing category

        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse.getData());
        int productId = storePResponse.getData().getProductId();
        // StoreOwner added a product to Store1


        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        Response<List<ProductDTO>> result = systemService.searchByCategory(nonExistingCategory);
        System.err.println("result: " + result.getData());
        assertNotNull(resultGetProduct.getData());
        assertNull(result.getData());
    }

    @Test
    void testSearchBy_validCategoryExistingProduct_Success() {
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
        // StoreOwner added a product to Store1
        
        // NOTE: We're still generating the token for verification in the test, even though we don't pass it to systemService
        String guestToken = tokenService.generateGuestToken(); 
        assertNotNull(guestToken);

        Response<List<ProductDTO>> result1 = systemService.searchByKeyword(productName);
        Response<List<ProductDTO>> result2 = systemService.searchByKeyword("Test");
        Response<List<ProductDTO>> result3 = systemService.searchByKeyword("Product");
        int productIdFromResult2 = result1.getData().get(0).getId();
        int productIdFromResult3 = result1.getData().get(0).getId();

        assertNotNull(result1.getData());
        assertNotNull(result2.getData());
        assertNotNull(result3.getData());
        assertEquals(productIdFromResult2, productIdFromResult3);
    }

    @Test
    void testSearchBy_invalidCategoryExistingProduct_Failure() {
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
        // StoreOwner added a product to Store1
        
        // NOTE: We're still generating the token for verification in the test, even though we don't pass it to systemService
        String guestToken = tokenService.generateGuestToken(); 
        assertNotNull(guestToken);

        Response<List<ProductDTO>> result1 = systemService.searchByKeyword(productName);
        Response<List<ProductDTO>> result2 = systemService.searchByKeyword("12");// Invalid keyword

        assertNotNull(result1.getData());
        assertTrue(result2.getData().isEmpty());
    }
}
