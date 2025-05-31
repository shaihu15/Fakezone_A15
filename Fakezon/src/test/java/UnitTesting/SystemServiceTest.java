package UnitTesting;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.context.ApplicationEventPublisher;

import DomainLayer.Interfaces.*;

import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.ProductRatingDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;

import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.User;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.StoreRepository;


class SystemServiceTest {
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private IOrderService orderService;
    private ApplicationEventPublisher publisher;
    private INotificationWebSocketHandler notificationWebSocketHandler;
    private ISystemService systemService;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);
        storeService = mock(IStoreService.class);
        productService = mock(IProductService.class);
        orderService = mock(IOrderService.class);
        publisher = mock(ApplicationEventPublisher.class);
        notificationWebSocketHandler = mock(INotificationWebSocketHandler.class);
        systemService = new SystemService(storeService, userService, productService, orderService, null, null, null, publisher, notificationWebSocketHandler);
    }

    // purchaseCart
    @Test
    void testPurchaseCart_Success() {
        int userId = 1;
        String country = "IL";
        LocalDate dob = LocalDate.now();
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
        String deliveryMethod = "deliver";
        String cardNumber = "1234";
        String cardHolder = "Test";
        String expDate = "12/25";
        String cvv = "123";
        String address = "address";
        String recipient = "rec";
        String packageDetails = "details";

        Cart cart = mock(Cart.class);
        when(userService.getUserCart(userId)).thenReturn(cart);
        when(cart.getAllProducts()).thenReturn(Map.of(1, Map.of(2, 1)));
        when(userService.getAnyUserById(userId)).thenReturn(Optional.of(mock(DomainLayer.Model.User.class)));
        when(storeService.decrementProductsInStores(eq(userId), any())).thenReturn(new HashMap<>());
        when(storeService.calcAmount(eq(userId), eq(cart), eq(dob))).thenReturn(Map.of(1, 10.0));
        when(productService.getAllProducts()).thenReturn(List.of());
        when(storeService.getProductFromStore(anyInt(), anyInt())).thenReturn(mock(StoreProductDTO.class));
        when(storeService.checkIfProductsInStores(eq(userId), any())).thenReturn(new HashMap<>());
        IPayment paymentService = mock(IPayment.class);
        when(paymentService.pay(anyString(), anyString(), anyString(), anyString(), anyDouble())).thenReturn(true);
        IDelivery deliveryService = mock(IDelivery.class);
        when(deliveryService.deliver(anyString(), anyString(), anyString(), anyString())).thenReturn(true);

        // inject mocks for payment and delivery
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryService, null, paymentService, publisher, notificationWebSocketHandler);

        Response<String> response = systemService.purchaseCart(userId, country, dob, paymentMethod, deliveryMethod, cardNumber, cardHolder, expDate, cvv, address, recipient, packageDetails);

        assertTrue(response.isSuccess());
        assertEquals("Cart purchased successfully", response.getMessage());
    }

    @Test
    void testPurchaseCart_CartEmpty() {
        int userId = 1;
        Cart cart = mock(Cart.class);
        when(userService.getUserCart(userId)).thenReturn(cart);
        when(cart.getAllProducts()).thenReturn(Collections.emptyMap());

        Response<String> response = systemService.purchaseCart(userId, "IL", LocalDate.now(), PaymentMethod.CREDIT_CARD, "deliver", "1234", "Test", "12/25", "123", "address", "rec", "details");

        assertFalse(response.isSuccess());
        assertEquals("Cart is empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testPurchaseCart_InvalidCountry() {
        int userId = 1;
        Cart cart = mock(Cart.class);
        when(userService.getUserCart(userId)).thenReturn(cart);
        when(cart.getAllProducts()).thenReturn(Map.of(1, Map.of(2, 1)));

        Response<String> response = systemService.purchaseCart(userId, "XX", LocalDate.now(), PaymentMethod.CREDIT_CARD, "deliver", "1234", "Test", "12/25", "123", "address", "rec", "details");

        assertFalse(response.isSuccess());
        assertEquals("Invalid country code", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testPurchaseCart_UserNotFound() {
        int userId = 1;
        Cart cart = mock(Cart.class);
        when(userService.getUserCart(userId)).thenReturn(cart);
        when(cart.getAllProducts()).thenReturn(Map.of(1, Map.of(2, 1)));
        when(userService.getAnyUserById(userId)).thenReturn(Optional.empty());

        Response<String> response = systemService.purchaseCart(userId, "IL", LocalDate.now(), PaymentMethod.CREDIT_CARD, "deliver", "1234", "Test", "12/25", "123", "address", "rec", "details");

        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testPurchaseCart_Exception() {
        int userId = 1;
        when(userService.getUserCart(userId)).thenThrow(new RuntimeException("fail"));

        Response<String> response = systemService.purchaseCart(userId, "IL", LocalDate.now(), PaymentMethod.CREDIT_CARD, "deliver", "1234", "Test", "12/25", "123", "address", "rec", "details");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during purchase cart: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    // ratingStoreProduct
    @Test
    void testRatingStoreProduct_Success() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(productService.getProduct(productId)).thenReturn(mock(IProduct.class));
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(mock(StoreProductDTO.class));
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(userService.didPurchaseProduct(userId, storeId, productId)).thenReturn(true);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertTrue(response.isSuccess());
        assertEquals("Product rated successfully", response.getMessage());
    }

    @Test
    void testRatingStoreProduct_StoreClosed() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertEquals("Store is close", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_InvalidRating() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 6.0, "bad");

        assertFalse(response.isSuccess());
        assertEquals("Invalid rating value", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_ProductNotFound() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(productService.getProduct(productId)).thenReturn(null);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertEquals("Product not found", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_ProductNotInStore() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(productService.getProduct(productId)).thenReturn(mock(IProduct.class));
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(null);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertEquals("Product not found in store", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_UserNotLoggedIn() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(productService.getProduct(productId)).thenReturn(mock(IProduct.class));
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(mock(StoreProductDTO.class));
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_UserDidNotPurchase() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(productService.getProduct(productId)).thenReturn(mock(IProduct.class));
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(mock(StoreProductDTO.class));
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(userService.didPurchaseProduct(userId, storeId, productId)).thenReturn(false);

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertEquals("User did not purchase that productfrom the store", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStoreProduct_Exception() {
        int storeId = 1, productId = 2, userId = 3;
        when(storeService.isStoreOpen(storeId)).thenThrow(new RuntimeException("fail"));

        Response<Void> response = systemService.ratingStoreProduct(storeId, productId, userId, 4.5, "good");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during rating product: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    // addProductToStore
    @Test
    void testAddProductToStore_Success() {
        int storeId = 1, requesterId = 2;
        String productName = "prod";
        String description = "desc";
        double basePrice = 10.0;
        int quantity = 5;
        String category = "ELECTRONICS";
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of(requesterId));
        when(productService.getAllProducts()).thenReturn(List.of());
        when(storeService.addProductToStore(eq(storeId), eq(requesterId), anyInt(), eq(productName), eq(basePrice), eq(quantity), any())).thenReturn(mock(StoreProductDTO.class));

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, productName, description, basePrice, quantity, category);

        assertTrue(response.isSuccess());
        assertEquals("Product added to store successfully", response.getMessage());
    }

    @Test
    void testAddProductToStore_StoreClosed() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "prod", "desc", 10.0, 5, "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertEquals("Invalid store ID", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProductToStore_NotOwner() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of());

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "prod", "desc", 10.0, 5, "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertEquals("User is not a owner of this store", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProductToStore_DescriptionEmpty() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of(requesterId));

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "prod", "", 10.0, 5, "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertEquals("Product description must not be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProductToStore_ProductNameEmpty() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of(requesterId));

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "", "desc", 10.0, 5, "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertEquals("Product name must not be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProductToStore_InvalidCategory() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of(requesterId));

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "prod", "desc", 10.0, 5, "INVALID");

        assertFalse(response.isSuccess());
        assertEquals("Invalid category", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProductToStore_Exception() {
        int storeId = 1, requesterId = 2;
        when(storeService.isStoreOpen(storeId)).thenThrow(new RuntimeException("fail"));

        Response<StoreProductDTO> response = systemService.addProductToStore(storeId, requesterId, "prod", "desc", 10.0, 5, "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during adding product to store: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    // sendMessageToStore
    @Test
    void testSendMessageToStore_Success() {
        int userId = 1, storeId = 2;
        String message = "hello";
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.isStoreOpen(storeId)).thenReturn(true);

        Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);

        assertTrue(response.isSuccess());
        assertEquals("Message sent successfully", response.getMessage());
    }

    @Test
    void testSendMessageToStore_MessageEmpty() {
        int userId = 1, storeId = 2;

        Response<Void> response = systemService.sendMessageToStore(userId, storeId, "");

        assertFalse(response.isSuccess());
        assertEquals("Message cannot be empty", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSendMessageToStore_UserNotLoggedIn() {
        int userId = 1, storeId = 2;
        String message = "hello";
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSendMessageToStore_StoreClosed() {
        int userId = 1, storeId = 2;
        String message = "hello";
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);

        assertFalse(response.isSuccess());
        assertEquals("Store is closed", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSendMessageToStore_Exception() {
        int userId = 1, storeId = 2;
        String message = "hello";
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<Void> response = systemService.sendMessageToStore(userId, storeId, message);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during sending message to store: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testRatingStore_Success() {
        int storeId = 1, userId = 2;
        double rating = 4.5;
        String comment = "Great!";
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.didPurchaseStore(userId, storeId)).thenReturn(true);

        Response<Void> response = systemService.ratingStore(storeId, userId, rating, comment);

        assertTrue(response.isSuccess());
        assertEquals("Store rated successfully", response.getMessage());
    }

    @Test
    void testRatingStore_StoreClosed() {
        int storeId = 1, userId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<Void> response = systemService.ratingStore(storeId, userId, 4.5, "Great!");

        assertFalse(response.isSuccess());
        assertEquals("Store is close", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStore_UserDidNotPurchase() {
        int storeId = 1, userId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.didPurchaseStore(userId, storeId)).thenReturn(false);

        Response<Void> response = systemService.ratingStore(storeId, userId, 4.5, "Great!");

        assertFalse(response.isSuccess());
        assertEquals("User did not purchase from this store", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStore_InvalidRating() {
        int storeId = 1, userId = 2;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.didPurchaseStore(userId, storeId)).thenReturn(true);

        Response<Void> response = systemService.ratingStore(storeId, userId, 6.0, "Bad!");

        assertFalse(response.isSuccess());
        assertEquals("Invalid rating value", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testRatingStore_Exception() {
        int storeId = 1, userId = 2;
        when(storeService.isStoreOpen(storeId)).thenThrow(new RuntimeException("fail"));

        Response<Void> response = systemService.ratingStore(storeId, userId, 4.5, "Great!");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during rating store: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testAddToBasket_Success() {
        int userId = 1, productId = 2, storeId = 3, quantity = 1;
        StoreProductDTO product = mock(StoreProductDTO.class);
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(product);
        when(product.getQuantity()).thenReturn(5);

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertTrue(response.isSuccess());
        assertEquals("Product added to basket successfully", response.getMessage());
    }

    @Test
    void testAddToBasket_StoreClosed() {
        int userId = 1, productId = 2, storeId = 3, quantity = 1;
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertFalse(response.isSuccess());
        assertEquals("Store is closed", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddToBasket_UserNotLoggedIn() {
        int userId = 1, productId = 2, storeId = 3, quantity = 1;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.isUserLoggedIn(userId)).thenReturn(false);
        when(userService.isUnsignedUser(userId)).thenReturn(false);

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in or Guest", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddToBasket_ProductNotFound() {
        int userId = 1, productId = 2, storeId = 3, quantity = 1;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(null);

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertFalse(response.isSuccess());
        assertEquals("Product not found", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddToBasket_NotEnoughStock() {
        int userId = 1, productId = 2, storeId = 3, quantity = 10;
        StoreProductDTO product = mock(StoreProductDTO.class);
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.getProductFromStore(productId, storeId)).thenReturn(product);
        when(product.getQuantity()).thenReturn(5);

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertFalse(response.isSuccess());
        assertEquals("Not enough product in stock", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddToBasket_Exception() {
        int userId = 1, productId = 2, storeId = 3, quantity = 1;
        when(storeService.isStoreOpen(storeId)).thenThrow(new RuntimeException("fail"));

        Response<Void> response = systemService.addToBasket(userId, productId, storeId, quantity);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during adding to basket: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testAddProduct_Success() {
        when(productService.addProduct(anyString(), anyString(), any())).thenReturn(1);

        Response<Integer> response = systemService.addProduct("prod", "desc", "ELECTRONICS");

        assertTrue(response.isSuccess());
        assertEquals("Product added successfully", response.getMessage());
        assertEquals(1, response.getData());
    }

    @Test
    void testAddProduct_InvalidInput() {
        Response<Integer> response = systemService.addProduct(null, "desc", "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertEquals("Invalid input", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProduct_InvalidCategory() {
        Response<Integer> response = systemService.addProduct("prod", "desc", "INVALID");

        assertFalse(response.isSuccess());
        assertEquals("Invalid category", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testAddProduct_Exception() {
        when(productService.addProduct(anyString(), anyString(), any())).thenThrow(new RuntimeException("fail"));

        Response<Integer> response = systemService.addProduct("prod", "desc", "ELECTRONICS");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during adding product: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testSendMessageToUser_Success() {
        int managerId = 1, storeId = 2, userToAnswer = 3;
        String message = "hello";
        when(userService.isUserLoggedIn(managerId)).thenReturn(true);
        when(storeService.isStoreOpen(storeId)).thenReturn(true);

        Response<Void> response = systemService.sendMessageToUser(managerId, storeId, userToAnswer, message);

        assertTrue(response.isSuccess());
        assertEquals("Message sent successfully", response.getMessage());
    }

    @Test
    void testSendMessageToUser_UserNotLoggedIn() {
        int managerId = 1, storeId = 2, userToAnswer = 3;
        String message = "hello";
        when(userService.isUserLoggedIn(managerId)).thenReturn(false);

        Response<Void> response = systemService.sendMessageToUser(managerId, storeId, userToAnswer, message);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSendMessageToUser_StoreClosed() {
        int managerId = 1, storeId = 2, userToAnswer = 3;
        String message = "hello";
        when(userService.isUserLoggedIn(managerId)).thenReturn(true);
        when(storeService.isStoreOpen(storeId)).thenReturn(false);

        Response<Void> response = systemService.sendMessageToUser(managerId, storeId, userToAnswer, message);

        assertFalse(response.isSuccess());
        assertEquals("Store is closed", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSendMessageToUser_Exception() {
        int managerId = 1, storeId = 2, userToAnswer = 3;
        String message = "hello";
        when(userService.isUserLoggedIn(managerId)).thenThrow(new RuntimeException("fail"));

        Response<Void> response = systemService.sendMessageToUser(managerId, storeId, userToAnswer, message);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during sending message to user: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testGetUnsignedUserById_Success() {
        int userId = 1;
        User mockUser = mock(User.class);
        UserDTO userDTO = mock(UserDTO.class);
        when(userService.getUnsignedUserById(userId)).thenReturn(Optional.of(mockUser));
        when(mockUser.toDTO()).thenReturn(userDTO);

        Response<UserDTO> response = systemService.getUnsignedUserById(userId);

        assertTrue(response.isSuccess());
        assertEquals(userDTO, response.getData());
        assertEquals("Unsigned user retrieved successfully", response.getMessage());
    }

    @Test
    void testGetUnsignedUserById_NotFound() {
        int userId = 1;
        when(userService.getUnsignedUserById(userId)).thenReturn(Optional.empty());

        Response<UserDTO> response = systemService.getUnsignedUserById(userId);

        assertFalse(response.isSuccess());
        assertEquals("Unsigned user not found", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetUnsignedUserById_Exception() {
        int userId = 1;
        when(userService.getUnsignedUserById(userId)).thenThrow(new RuntimeException("fail"));

        Response<UserDTO> response = systemService.getUnsignedUserById(userId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error getting unsigned user: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetAllUnsignedUsers_Success() {
        int adminId = 1;
        List<UserDTO> users = List.of(mock(UserDTO.class));
        when(userService.isSystemAdmin(adminId)).thenReturn(true);
        when(userService.getAllUnsignedUsersDTO()).thenReturn(users);

        Response<List<UserDTO>> response = systemService.getAllUnsignedUsers(adminId);

        assertTrue(response.isSuccess());
        assertEquals(users, response.getData());
        assertEquals("Retrieved " + users.size() + " unsigned users", response.getMessage());
    }

    @Test
    void testGetAllUnsignedUsers_NotAdmin() {
        int adminId = 1;
        when(userService.isSystemAdmin(adminId)).thenReturn(false);

        Response<List<UserDTO>> response = systemService.getAllUnsignedUsers(adminId);

        assertFalse(response.isSuccess());
        assertEquals("Admin privileges required", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAllUnsignedUsers_Exception() {
        int adminId = 1;
        when(userService.isSystemAdmin(adminId)).thenReturn(true);
        when(userService.getAllUnsignedUsersDTO()).thenThrow(new RuntimeException("fail"));

        Response<List<UserDTO>> response = systemService.getAllUnsignedUsers(adminId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error getting all unsigned users: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testSearchOrders_Success() {
        int userId = 1;
        String keyword = "order";
        IOrder order = mock(IOrder.class);
        List<IOrder> orders = List.of(order);
        OrderDTO orderDTO = mock(OrderDTO.class);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(orderService.searchOrders(keyword)).thenReturn(orders);
        // Mock createOrderDTO if needed
        SystemService spyService = spy((SystemService)systemService);
        doReturn(orderDTO).when(spyService).createOrderDTO(order);

        Response<List<OrderDTO>> response = spyService.searchOrders(keyword, userId);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Orders retrieved successfully", response.getMessage());
    }

    @Test
    void testSearchOrders_UserNotLoggedIn() {
        int userId = 1;
        String keyword = "order";
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<List<OrderDTO>> response = systemService.searchOrders(keyword, userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testSearchOrders_Exception() {
        int userId = 1;
        String keyword = "order";
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<List<OrderDTO>> response = systemService.searchOrders(keyword, userId);

        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetOrdersByStoreId_Success() {
        int storeId = 1, userId = 2;
        IOrder order = mock(IOrder.class);
        List<IOrder> orders = List.of(order);
        OrderDTO orderDTO = mock(OrderDTO.class);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(orderService.getOrdersByStoreId(storeId)).thenReturn(orders);
        ISystemService spyService = spy(systemService);
        doReturn(orderDTO).when((SystemService)spyService).createOrderDTO(order);

        Response<List<OrderDTO>> response = spyService.getOrdersByStoreId(storeId, userId);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Orders retrieved successfully", response.getMessage());
    }

    @Test
    void testGetOrdersByStoreId_UserNotLoggedIn() {
        int storeId = 1, userId = 2;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<List<OrderDTO>> response = systemService.getOrdersByStoreId(storeId, userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetOrdersByStoreId_Exception() {
        int storeId = 1, userId = 2;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));

        Response<List<OrderDTO>> response = systemService.getOrdersByStoreId(storeId, userId);

        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testGetUnsignedUserCount_Success() {
        int adminId = 1;
        when(userService.isSystemAdmin(adminId)).thenReturn(true);
        when(userService.getUnsignedUserCount()).thenReturn(5);

        Response<Integer> response = systemService.getUnsignedUserCount(adminId);

        assertTrue(response.isSuccess());
        assertEquals(5, response.getData());
        assertEquals("Retrieved unsigned user count: 5", response.getMessage());
    }

    @Test
    void testGetUnsignedUserCount_NotAdmin() {
        int adminId = 1;
        when(userService.isSystemAdmin(adminId)).thenReturn(false);

        Response<Integer> response = systemService.getUnsignedUserCount(adminId);

        assertFalse(response.isSuccess());
        assertEquals("Admin privileges required", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetUnsignedUserCount_Exception() {
        int adminId = 1;
        when(userService.isSystemAdmin(adminId)).thenReturn(true);
        when(userService.getUnsignedUserCount()).thenThrow(new RuntimeException("fail"));

        Response<Integer> response = systemService.getUnsignedUserCount(adminId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error getting unsigned user count: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testRemoveStoreManager_Success() {
        int storeId = 1, requesterId = 2, managerId = 3;
        doNothing().when(userService).removeRole(managerId, storeId);
        doNothing().when(storeService).removeStoreManager(storeId, requesterId, managerId);

        Response<Void> response = systemService.removeStoreManager(storeId, requesterId, managerId);

        assertTrue(response.isSuccess());
        assertEquals("Store manager removed successfully", response.getMessage());
    }

    @Test
    void testRemoveStoreManager_RemoveRoleException() {
        int storeId = 1, requesterId = 2, managerId = 3;
        doThrow(new RuntimeException("fail")).when(userService).removeRole(managerId, storeId);

        Response<Void> response = systemService.removeStoreManager(storeId, requesterId, managerId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during removing store manager: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testRemoveStoreManager_RemoveStoreManagerException() {
        int storeId = 1, requesterId = 2, managerId = 3;
        doNothing().when(userService).removeRole(managerId, storeId);
        doThrow(new RuntimeException("fail")).when(storeService).removeStoreManager(storeId, requesterId, managerId);

        Response<Void> response = systemService.removeStoreManager(storeId, requesterId, managerId);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during removing store manager: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testRemoveStoreOwner_Success() {
        int storeId = 1, requesterId = 2, ownerId = 3;
        doNothing().when(userService).removeRole(ownerId, storeId);
        doNothing().when(storeService).removeStoreOwner(storeId, requesterId, ownerId);
    
        Response<Void> response = systemService.removeStoreOwner(storeId, requesterId, ownerId);
    
        assertTrue(response.isSuccess());
        assertEquals("Store owner removed successfully", response.getMessage());
    }
    
    @Test
    void testRemoveStoreOwner_RemoveRoleException() {
        int storeId = 1, requesterId = 2, ownerId = 3;
        doThrow(new RuntimeException("fail")).when(userService).removeRole(ownerId, storeId);
    
        Response<Void> response = systemService.removeStoreOwner(storeId, requesterId, ownerId);
    
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testRemoveStoreOwner_RemoveStoreOwnerException() {
        int storeId = 1, requesterId = 2, ownerId = 3;
        doNothing().when(userService).removeRole(ownerId, storeId);
        doThrow(new RuntimeException("fail")).when(storeService).removeStoreOwner(storeId, requesterId, ownerId);
    
        Response<Void> response = systemService.removeStoreOwner(storeId, requesterId, ownerId);
    
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during removing store owner: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testGetAllStoreOrders_PermissionDenied() {
        int storeId = 1, requesterId = 2;
        when(storeService.canViewOrders(storeId, requesterId)).thenReturn(false);

        Response<List<OrderDTO>> response = systemService.getAllStoreOrders(storeId, requesterId);

        assertFalse(response.isSuccess());
        assertEquals("user 2 has insufficient permissions to view orders from store 1", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAllStoreOrders_Exception() {
        int storeId = 1, requesterId = 2;
        when(storeService.canViewOrders(storeId, requesterId)).thenThrow(new RuntimeException("fail"));

        Response<List<OrderDTO>> response = systemService.getAllStoreOrders(storeId, requesterId);

        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertEquals("fail", response.getMessage());
    }
    @Test
    void testGetAllStoreOrders_UserNotLoggedIn() {
        int storeId = 1, requesterId = 2;
        when(userService.isUserLoggedIn(requesterId)).thenReturn(false);
    
        Response<List<OrderDTO>> response = systemService.getAllStoreOrders(storeId, requesterId);
    
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testGetAllStoreOrders_Success() {
        int storeId = 1, requesterId = 2;
        List<IOrder> orders = List.of(mock(IOrder.class));
        OrderDTO orderDTO = mock(OrderDTO.class);

        when(storeService.canViewOrders(storeId, requesterId)).thenReturn(true);
        when(orderService.getOrdersByStoreId(storeId)).thenReturn(orders);

        // Cast systemService to SystemService for spying
        SystemService spyService = spy((SystemService) systemService);
        doReturn(orderDTO).when(spyService).createOrderDTO(any());

        Response<List<OrderDTO>> response = spyService.getAllStoreOrders(storeId, requesterId);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("success", response.getMessage());
    }
    @Test
    void testUpdateProductInStore_StoreClosed() {
        int storeId = 1, productId = 2, requesterId = 3, quantity = 5;
        double price = 10.0;
        when(storeService.isStoreOpen(storeId)).thenReturn(false);
    
        Response<Void> response = systemService.updateProductInStore(storeId, productId, requesterId, price, quantity);
    
        assertFalse(response.isSuccess());
    }
    
    @Test
    void testUpdateProductInStore_NotOwner() {
        int storeId = 1, productId = 2, requesterId = 3, quantity = 5;
        double price = 10.0;
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.getStoreOwners(storeId, requesterId)).thenReturn(List.of());
    
        Response<Void> response = systemService.updateProductInStore(storeId, productId, requesterId, price, quantity);
    
        assertFalse(response.isSuccess());
    }
    
    @Test
    void testUpdateProductInStore_Exception() {
        int storeId = 1, productId = 2, requesterId = 3, quantity = 5;
        double price = 10.0;
        when(storeService.isStoreOpen(storeId)).thenThrow(new RuntimeException("fail"));
    
        Response<Void> response = systemService.updateProductInStore(storeId, requesterId,productId , price, quantity);
    
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testGetUserRoles_Success() {
        int userId = 1;
        HashMap<Integer, IRegisteredRole> roles = new HashMap<>();
        roles.put(1, mock(IRegisteredRole.class));
        when(userService.isUserLoggedIn(userId)).thenReturn(true); // <-- Add this line
        when(userService.getAllRoles(userId)).thenReturn(roles);

        Response<HashMap<Integer,IRegisteredRole>> response = systemService.getUserRoles(userId);

        assertTrue(response.isSuccess());
        assertEquals(roles, response.getData());
    }
    
    @Test
    void testGetUserRoles_Exception() {
        int userId = 1;
        when(userService.getAllRoles(userId)).thenThrow(new RuntimeException("fail"));
    
        Response<HashMap<Integer, IRegisteredRole>> response = systemService.getUserRoles(userId);
    
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testAcceptAssignment_Success() {
        int userId = 1, storeId = 2;
        when(storeService.acceptAssignment(storeId, userId)).thenReturn(true);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);

        Response<String> response = systemService.acceptAssignment(userId, storeId);

        assertTrue(response.isSuccess());
    }
    
    @Test
    void testAcceptAssignment_Exception() {
        int userId = 1, storeId = 2;
        doThrow(new RuntimeException("fail")).when(storeService).acceptAssignment(userId, storeId);
    
        Response<String> response = systemService.acceptAssignment(userId, storeId);
    
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testSendResponseForAuctionByOwner_Success() {
    int storeId = 2, requesterId = 1, productId = 3;
    boolean accept = true;
    doNothing().when(storeService).sendResponseForAuctionByOwner(storeId, requesterId, productId, accept);
    when(userService.isUserLoggedIn(requesterId)).thenReturn(true);

    Response<String> response = systemService.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept);

    assertTrue(response.isSuccess());
    }
    
    @Test
    void testSendResponseForAuctionByOwner_Exception() {
        int ownerId = 1, storeId = 2, auctionId = 3;
        boolean responseValue = true;
        doThrow(new RuntimeException("fail")).when(storeService).sendResponseForAuctionByOwner(ownerId, storeId, auctionId, responseValue);

        Response<String> response = systemService.sendResponseForAuctionByOwner(ownerId, storeId, auctionId, responseValue);

        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }


    @Test
    void testgetOrdersByUserId_Success() {
        int userId = 1;
        IOrder order = mock(IOrder.class);
        List<IOrder> orders = List.of(order);
        OrderDTO orderDTO = mock(OrderDTO.class);

        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(orderService.getOrdersByUserId(userId)).thenReturn(orders);

        SystemService spyService = spy((SystemService) systemService);
        doReturn(orderDTO).when(spyService).createOrderDTO(order);

        Response<List<OrderDTO>> response = spyService.getOrdersByUserId(userId);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Orders retrieved successfully", response.getMessage());
    }

    @Test
    void testgetOrdersByUserId_UserNotLoggedIn() {
        int userId = 1;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);

        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);

        assertFalse(response.isSuccess());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }

    @Test
    void testgetOrdersByUserId_Exception() {
        int userId = 1;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));
        Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Error during getting all orders by user ID: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }


 @Test
    void testIsStoreOwner_SuccessTrue() {
        int storeId = 5, userId = 42;
        when(storeService.isStoreOwner(storeId, userId)).thenReturn(true);

        Response<Boolean> resp = systemService.isStoreOwner(storeId, userId);

        assertTrue(resp.isSuccess());
        assertNull(resp.getErrorType());
        assertTrue(resp.getData());
        verify(storeService, times(1)).isStoreOwner(storeId, userId);
    }

    @Test
    void testIsStoreOwner_SuccessFalse() {
        int storeId = 5, userId = 99;
        when(storeService.isStoreOwner(storeId, userId)).thenReturn(false);

        Response<Boolean> resp = systemService.isStoreOwner(storeId, userId);

        assertTrue(resp.isSuccess());
        assertNull(resp.getErrorType());
        assertFalse(resp.getData());
        verify(storeService, times(1)).isStoreOwner(storeId, userId);
    }

    @Test
    void testIsStoreOwner_Exception() {
        int storeId = 7, userId = 8;
        when(storeService.isStoreOwner(storeId, userId))
            .thenThrow(new RuntimeException("boom"));

        Response<Boolean> resp = systemService.isStoreOwner(storeId, userId);

        assertFalse(resp.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
        assertTrue(resp.getMessage().contains("boom"));
        assertNull(resp.getData());
        verify(storeService, times(1)).isStoreOwner(storeId, userId);
    }

    @Test
    void testIsStoreManager_SuccessWithPerms() {
        int storeId = 10, userId = 20;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);
        when(storeService.isStoreManager(storeId, userId)).thenReturn(perms);

        Response<List<StoreManagerPermission>> resp = systemService.isStoreManager(storeId, userId);

        assertTrue(resp.isSuccess());
        assertNull(resp.getErrorType());
        assertEquals(perms, resp.getData());
        verify(storeService, times(1)).isStoreManager(storeId, userId);
    }

    @Test
    void testIsStoreManager_SuccessNotManager() {
        int storeId = 10, userId = 21;
        when(storeService.isStoreManager(storeId, userId)).thenReturn(null);

        Response<List<StoreManagerPermission>> resp = systemService.isStoreManager(storeId, userId);

        assertTrue(resp.isSuccess());
        assertNull(resp.getErrorType());
        assertNull(resp.getData());
        verify(storeService, times(1)).isStoreManager(storeId, userId);
    }

    @Test
    void testIsStoreManager_Exception() {
        int storeId = 11, userId = 22;
        when(storeService.isStoreManager(storeId, userId))
            .thenThrow(new IllegalStateException("oops"));

        Response<List<StoreManagerPermission>> resp = systemService.isStoreManager(storeId, userId);

        assertFalse(resp.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
        assertTrue(resp.getMessage().contains("oops"));
        assertNull(resp.getData());
        verify(storeService, times(1)).isStoreManager(storeId, userId);
    }
    @Test
    void testGetDeliveryService() {
        IDelivery deliveryMock = mock(IDelivery.class);
        systemService = new SystemService(storeService, userService, productService, orderService, deliveryMock, null, null, publisher, notificationWebSocketHandler);
    
        IDelivery result = ((SystemService)systemService).getDeliveryService();
    
        assertEquals(deliveryMock, result);
    }
    
    @Test
    void testGetAuthenticatorService() {
        IAuthenticator authMock = mock(IAuthenticator.class);
        systemService = new SystemService(storeService, userService, productService, orderService, null, authMock, null, publisher, notificationWebSocketHandler);
    
        IAuthenticator result = ((SystemService)systemService).getAuthenticatorService();
    
        assertEquals(authMock, result);
    }
    
    @Test
    void testGetPaymentService() {
        IPayment paymentMock = mock(IPayment.class);
        systemService = new SystemService(storeService, userService, productService, orderService, null, null, paymentMock, publisher, notificationWebSocketHandler);
    
        IPayment result = ((SystemService)systemService).getPaymentService();
    
        assertEquals(paymentMock, result);
    }
    @Test
    void testUserAccessStore_Success() {
        int storeId = 1;
        StoreDTO storeDTO = mock(StoreDTO.class);
        when(storeService.viewStore(storeId)).thenReturn(storeDTO);
        when(storeDTO.isOpen()).thenReturn(true);
    
        Response<StoreDTO> response = systemService.userAccessStore(storeId);
    
        assertTrue(response.isSuccess());
        assertEquals(storeDTO, response.getData());
        assertEquals("Store retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testUserAccessStore_StoreClosed() {
        int storeId = 1;
        StoreDTO storeDTO = mock(StoreDTO.class);
        when(storeService.viewStore(storeId)).thenReturn(storeDTO);
        when(storeDTO.isOpen()).thenReturn(false);
    
        Response<StoreDTO> response = systemService.userAccessStore(storeId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Store is closed", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testUserAccessStore_Exception() {
        int storeId = 1;
        when(storeService.viewStore(storeId)).thenThrow(new RuntimeException("fail"));
    
        Response<StoreDTO> response = systemService.userAccessStore(storeId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during user access store: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testGetProductFromStore_Success() {
        int productId = 1, storeId = 2;
        StoreDTO storeDTO = mock(StoreDTO.class);
        StoreProductDTO productDTO = mock(StoreProductDTO.class);
    
        when(storeService.isStoreOpen(storeId)).thenReturn(true);
        when(storeService.viewStore(storeId)).thenReturn(storeDTO);
        when(storeDTO.getStoreProductById(productId)).thenReturn(productDTO);
    
        Response<StoreProductDTO> response = systemService.getProductFromStore(productId, storeId);
    
        assertTrue(response.isSuccess());
        assertEquals(productDTO, response.getData());
        assertEquals("Product retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }

    @Test
    void testGetProduct_Success() {
        int productId = 1;
        ProductDTO productDTO = mock(ProductDTO.class);
        when(productService.viewProduct(productId)).thenReturn(productDTO);
    
        Response<ProductDTO> response = systemService.getProduct(productId);
    
        assertTrue(response.isSuccess());
        assertEquals(productDTO, response.getData());
        assertEquals("Product retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetProduct_InvalidInput() {
        int productId = 1;
        when(productService.viewProduct(productId)).thenThrow(new IllegalArgumentException("bad id"));
    
        Response<ProductDTO> response = systemService.getProduct(productId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Invalid input", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testGetProduct_NullPointer() {
        int productId = 1;
        when(productService.viewProduct(productId)).thenThrow(new NullPointerException("null!"));
    
        Response<ProductDTO> response = systemService.getProduct(productId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Unexpected null value", response.getMessage());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    
    @Test
    void testGetProduct_GeneralException() {
        int productId = 1;
        when(productService.viewProduct(productId)).thenThrow(new RuntimeException("fail"));
    
        Response<ProductDTO> response = systemService.getProduct(productId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("An unexpected error occurred", response.getMessage());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testUpdateProduct_Success() {
        int productId = 1;
        String productName = "TestProduct";
        String productDescription = "TestDesc";
        Set<Integer> storesIds = Set.of(1, 2);
    
        // No exception thrown means success
        doNothing().when(productService).updateProduct(productId, productName, productDescription, storesIds);
    
        Response<Boolean> response = systemService.updateProduct(productId, productName, productDescription, storesIds);
    
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        assertEquals("Product updated successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testUpdateProduct_Exception() {
        int productId = 1;
        String productName = "TestProduct";
        String productDescription = "TestDesc";
        Set<Integer> storesIds = Set.of(1, 2);
    
        doThrow(new RuntimeException("fail")).when(productService).updateProduct(productId, productName, productDescription, storesIds);
    
        Response<Boolean> response = systemService.updateProduct(productId, productName, productDescription, storesIds);
    
        assertFalse(response.isSuccess());
        assertFalse(response.getData());
        assertEquals("Error during updating product", response.getMessage());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testDeleteProduct_Success() {
        int productId = 1;
    
        // No exception thrown means success
        doNothing().when(productService).deleteProduct(productId);
    
        Response<Boolean> response = systemService.deleteProduct(productId);
    
        assertTrue(response.isSuccess());
        assertTrue(response.getData());
        assertEquals("Product deleted successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testDeleteProduct_Exception() {
        int productId = 1;
    
        doThrow(new RuntimeException("fail")).when(productService).deleteProduct(productId);
    
        Response<Boolean> response = systemService.deleteProduct(productId);
    
        assertFalse(response.isSuccess());
        assertFalse(response.getData());
        assertEquals("Error during deleting product", response.getMessage());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testGetStoreRoles_Success() {
        int storeId = 1, userId = 2;
        StoreRolesDTO storeRolesDTO = mock(StoreRolesDTO.class);
        when(userService.isUserLoggedIn(userId)).thenReturn(true);
        when(storeService.getStoreRoles(storeId, userId)).thenReturn(storeRolesDTO);
    
        Response<StoreRolesDTO> response = systemService.getStoreRoles(storeId, userId);
    
        assertTrue(response.isSuccess());
        assertEquals(storeRolesDTO, response.getData());
        assertEquals("Store roles retrieved successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testGetStoreRoles_UserNotLoggedIn() {
        int storeId = 1, userId = 2;
        when(userService.isUserLoggedIn(userId)).thenReturn(false);
    
        Response<StoreRolesDTO> response = systemService.getStoreRoles(storeId, userId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("User is not logged in", response.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, response.getErrorType());
    }
    
    @Test
    void testGetStoreRoles_Exception() {
        int storeId = 1, userId = 2;
        when(userService.isUserLoggedIn(userId)).thenThrow(new RuntimeException("fail"));
    
        Response<StoreRolesDTO> response = systemService.getStoreRoles(storeId, userId);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during getting store roles: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    } 
    @Test
    void testAddStoreManagerPermissions_Success() {
        int storeId = 1, managerId = 2, requesterId = 3;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);
    
        // No exception thrown means success
        doNothing().when(storeService).addStoreManagerPermissions(storeId, requesterId, managerId, perms);
    
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, managerId, requesterId, perms);
    
        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Permissions added successfully", response.getMessage());
        assertNull(response.getErrorType());
    }
    
    @Test
    void testAddStoreManagerPermissions_Exception() {
        int storeId = 1, managerId = 2, requesterId = 3;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);
    
        doThrow(new RuntimeException("fail")).when(storeService).addStoreManagerPermissions(storeId, requesterId, managerId, perms);
    
        Response<Void> response = systemService.addStoreManagerPermissions(storeId, managerId, requesterId, perms);
    
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during adding store manager permissions: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }
    @Test
    void testAddStoreManager_Success() {
        int storeId = 1, requesterId = 2, managerId = 3;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);

        when(userService.isUserLoggedIn(requesterId)).thenReturn(true);
        when(userService.isUserRegistered(managerId)).thenReturn(true);
        doNothing().when(storeService).addStoreManager(storeId, requesterId, managerId, perms);
        doNothing().when(userService).addRole(eq(managerId), eq(storeId), any(StoreManager.class));

        Response<Void> response = systemService.addStoreManager(storeId, requesterId, managerId, perms);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Store manager added successfully", response.getMessage());
        assertNull(response.getErrorType());
    }

    @Test
    void testAddStoreManager_Exception() {
        int storeId = 1, requesterId = 2, managerId = 3;
        List<StoreManagerPermission> perms = List.of(StoreManagerPermission.VIEW_PURCHASES);

        when(userService.isUserLoggedIn(requesterId)).thenReturn(true);
        when(userService.isUserRegistered(managerId)).thenReturn(true);
        doThrow(new RuntimeException("fail")).when(storeService).addStoreManager(storeId, requesterId, managerId, perms);

        Response<Void> response = systemService.addStoreManager(storeId, requesterId, managerId, perms);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during adding store manager: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    } 
    @Test
    void testAddStoreOwner_Success() {
        int storeId = 1, requesterId = 2, ownerId = 3;
        doNothing().when(storeService).addStoreOwner(storeId, requesterId, ownerId);
        when(userService.isUserLoggedIn(requesterId)).thenReturn(true);
        when(userService.isUserRegistered(ownerId)).thenReturn(true);

        Response<Void> response = systemService.addStoreOwner(storeId, requesterId, ownerId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Store owner added successfully", response.getMessage());
        assertNull(response.getErrorType());
    }

    @Test
    void testAddStoreOwner_Exception() {
        int storeId = 1, requesterId = 2, ownerId = 3;
        doThrow(new RuntimeException("fail")).when(storeService).addStoreOwner(storeId, requesterId, ownerId);

        when(userService.isUserLoggedIn(requesterId)).thenReturn(true);
        when(userService.isUserRegistered(ownerId)).thenReturn(true);
        Response<Void> response = systemService.addStoreOwner(storeId, requesterId, ownerId);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during adding store owner: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }

    @Test
    void testAddAuctionProductToStore_Success() {
        int storeId = 1, requesterId = 2, productID = 3, minutesToEnd = 60;
        double basePrice = 100.0;
        doNothing().when(storeService).addAuctionProductToStore(storeId, requesterId, productID, basePrice, minutesToEnd);

        Response<Void> response = systemService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, minutesToEnd);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Auction product added successfully", response.getMessage());
        assertNull(response.getErrorType());
    }

    @Test
    void testAddAuctionProductToStore_Exception() {
        int storeId = 1, requesterId = 2, productID = 3, minutesToEnd = 60;
        double basePrice = 100.0;
        doThrow(new RuntimeException("fail")).when(storeService).addAuctionProductToStore(storeId, requesterId, productID, basePrice, minutesToEnd);

        Response<Void> response = systemService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, minutesToEnd);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertTrue(response.getMessage().contains("Error during adding auction product to store: fail"));
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
    }


}
    
