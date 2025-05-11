package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;

import ApplicationLayer.DTO.StoreProductDTO;
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
import DomainLayer.Enums.PaymentMethod;
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


public class Immediate_Purchase_of_Shopping_Cart {
    //Use-Case: 2.5 Immediate Purchase of Shopping Cart

    //private PaymentMethod paymentMethod;
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
        //paymentMethod = PaymentMethod.CREDIT_CARD;
    }
 
    @Test
    void testImmediatePurchase_Success() {

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
        //add more asserts to check the order details, stock updates, etc.
    }

    @Test
    void testImmediatePurchase_invalidCountry_Failure() {

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.invalidCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Invalid country code", responsePurchaseCart.getMessage());
        //add more asserts to check the order details, stock updates, etc.
    }
    
    @Test
    void testImmediatePurchase_invalUserId_Failure() {

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId+10 , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Error during purchase cart: User not found", responsePurchaseCart.getMessage());
        //add more asserts to check the order details, stock updates, etc.
    }

    @Test
    void testImmediatePurchase_Cartisempty_Failure() {

        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        //the product is added to the store

        //Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1); 
        //assertTrue(responseAddToBasket.isSuccess());

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Cart is empty", responsePurchaseCart.getMessage());
        //add more asserts to check the order details, stock updates, etc.
    }

    @Test
    void testImmediatePurchase_ProductsNotInStores_Failure() {
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData(); 
        //the store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // user2 is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId); //only one product is added
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1); 
        assertTrue(responseAddToBasket.isSuccess());
        // user2 added the product to the basket

        Response<UserDTO> resultRegister3 = testHelper.register_and_login3();
        assertNotNull(resultRegister3.getData());
        int registeredId3 = resultRegister3.getData().getUserId();
        // user3 is registered and logged in


        Response<Void> responseAddToBasket3 = systemService.addToBasket(registeredId3, storeId, productIdInt, 1); 
        assertTrue(responseAddToBasket3.isSuccess());
        // user3 added the product to the basket

         Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());
        //user2 purchased the cart

        //------------------------------------------------------------------------------------------------//
        //now user 3 try to purchase the cart
        Response<String> responsePurchaseCart3 = systemService.purchaseCart
                    (registeredId3 , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart3.isSuccess());
        assertEquals("Product is not available: Test Product", responsePurchaseCart3.getMessage());
        //add more asserts to check the order details, stock updates, etc.
    }

    @Test
    void testImmediatePurchase_CreditSystemRejection_Failure() {
        // Setup: User with a valid cart
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData();
        // The store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // User is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId);
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        // The product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1);
        assertTrue(responseAddToBasket.isSuccess());
        // User added the product to the basket

        // Simulate rejection from the credit system
        Response<String> responsePurchaseCart = systemService.purchaseCart(
                registeredId, testHelper.validCountry(), LocalDate.now(), null, null,
                null, null, null, null, null,
                null, null);
    

        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Payment failed", responsePurchaseCart.getMessage());
    }

    @Test
    void testImmediatePurchase_ShippingSystemRejection_Failure() {
        // Setup: User with a valid cart
        Response<UserDTO> resultRegister1 = testHelper.register_and_login();
        assertNotNull(resultRegister1.getData());
        int StoreFounderId = resultRegister1.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> storeResult = systemService.addStore(StoreFounderId, "Store1");
        assertNotNull(storeResult.getData());
        int storeId = storeResult.getData();
        // The store is open

        Response<UserDTO> resultRegister2 = testHelper.register_and_login2();
        assertNotNull(resultRegister2.getData());
        int registeredId = resultRegister2.getData().getUserId();
        // User is registered and logged in

        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, StoreFounderId);
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        // The product is added to the store

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, storeId, productIdInt, 1);
        assertTrue(responseAddToBasket.isSuccess());
        // User added the product to the basket

        
        Response<String> responsePurchaseCart = systemService.purchaseCart(
                registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                null, "1234567890123456", "cardHolder",
                "12/25", "123", null, "Recipient",
                "Package details");

        // Assert: Purchase fails, no credit is charged
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Delivery failed", responsePurchaseCart.getMessage());
    }

}
