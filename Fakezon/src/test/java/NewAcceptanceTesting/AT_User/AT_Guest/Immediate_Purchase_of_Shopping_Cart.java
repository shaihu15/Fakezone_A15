package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
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
import DomainLayer.Enums.PaymentMethod;
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
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;

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

          Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());

        Collection<IOrder> newOrders = orderRepository.getAllOrders();
        assertTrue(newOrders.size() == 1);
        int newOrderID = newOrders.iterator().next().getId();
        orderRepository.deleteOrder(newOrderID);

        Collection<IOrder> orders2 = orderRepository.getAllOrders();
        assertTrue(orders2.size() == 0);



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
        
        //There is no new order in order service
        Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);
        
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

        //There is no new order in order service
        Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);    }

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

        //------------------------------------------------------------------------------------------------//

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId , testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St, City, Country","Recipient",
                     "Package details");
        
        assertFalse(responsePurchaseCart.isSuccess());
        assertEquals("Cart is empty", responsePurchaseCart.getMessage());

        //There is no new order in order service//There is no new order in order service
        Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);}

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
        //There is no new order in order service
        Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);
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

        //There is no new order in order service
        Collection<IOrder> orders = orderRepository.getAllOrders();
        assertTrue(orders.size() == 0);
    }

    @Test
void testImmediatePurchase_Parallel_LastProduct() throws Exception {

    Response<UserDTO> storeOwnerResponse = testHelper.register_and_login();
    assertTrue(storeOwnerResponse.isSuccess());
    int storeOwnerId = storeOwnerResponse.getData().getUserId();
    // storeOwner is registered and logged in

    Response<Integer> storeResult = systemService.addStore(storeOwnerId, "Store1");
    assertTrue(storeResult.isSuccess());
    int storeId = storeResult.getData(); 
    //the store is open

    Response<StoreProductDTO> productResponse = testHelper.addProductToStore(storeId, storeOwnerId); //only one product is added
    assertTrue(productResponse.isSuccess());
    int productId = productResponse.getData().getProductId();
    //the product is added to the store

    // Register two customers
    Response<UserDTO> user1 = testHelper.register_and_login2();
    Response<UserDTO> user2 = testHelper.register_and_login3();
    assertTrue(user1.isSuccess());
    assertTrue(user2.isSuccess());
    int user1Id = user1.getData().getUserId();
    int user2Id = user2.getData().getUserId();

    // Both users add the same product to their basket
    assertTrue(systemService.addToBasket(user1Id, storeId, productId, 1).isSuccess());
    assertTrue(systemService.addToBasket(user2Id, storeId, productId, 1).isSuccess());

    /*Using ExecutorService is the standard and safe way to manage threads in Java for parallel tasks,
    instead of creating Thread objects directly and managing them manually. */
    
    // Prepare concurrent purchase tasks
    ExecutorService executor = Executors.newFixedThreadPool(2);

    /*Callable is a functional interface that allows you to define a task that can be executed in a separate thread, 
        and it can also return a value (in this case, Response<Void>) and throw exceptions (which is important for error checking).
        Each task represents a call to a systemService function with different parameters (for example, with different users). */

    Callable<Response<String>> task1 = () -> systemService.purchaseCart(
        user1Id, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
        "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
        "123 Main St, City, Country", "Recipient", "Package details"
    );

    Callable<Response<String>> task2 = () -> systemService.purchaseCart(
        user2Id, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
        "deliveryMethod", "1234567890123456", "cardHolder", "12/25", "123",
        "123 Main St, City, Country", "Recipient", "Package details"
    );

    /*The submit() method of the ExecutorService submits the task for execution on an available thread in the pool. It returns a Future object.
    A Future object represents the result of an asynchronous task (that is, a task that runs in the background). 
    It allows you to check whether the task has completed, cancel it, and most importantly, receive its result as soon as it completes. */

    Future<Response<String>> future1 = executor.submit(task1);
    Future<Response<String>> future2 = executor.submit(task2);

    Response<String> result1 = future1.get(); // waits for first purchase
    Response<String> result2 = future2.get(); // waits for second purchase

    executor.shutdown();

    // Assert only one success
    boolean oneSuccess = result1.isSuccess() ^ result2.isSuccess(); // XOR: exactly one is true
    assertTrue(oneSuccess, "Only one user should be able to purchase the last product.");

    // Validate the error message from the failure
    String expectedFailureMessage = "Product is not available: Test Product";
    if (!result1.isSuccess()) {
        assertEquals(expectedFailureMessage, result1.getMessage());
    } else {
        assertEquals(expectedFailureMessage, result2.getMessage());
    }

    // Clean up: remove created order if any
    Collection<IOrder> orders = orderRepository.getAllOrders();
    assertEquals(1, orders.size(), "There should be only one successful order.");
    int orderId = orders.iterator().next().getId();
    orderRepository.deleteOrder(orderId);
}


}
