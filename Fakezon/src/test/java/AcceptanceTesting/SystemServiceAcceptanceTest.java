package AcceptanceTesting;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import ApplicationLayer.Response;

import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Product;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

public class SystemServiceAcceptanceTest {
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;

    private SystemService systemService;

    @Mock
    private IProductService productService;

    @Mock
    private IAuthenticator authenticatorService;

    private Store store1;
    private int store1Id = 1;
    private int founder1Id = 10;
    private Store store2;
    private int store2Id = 2;
    private int founder2Id = 20;
    private IStoreService storeService;
    private IUserService userService;
    private IOrderService orderService;
    private IDelivery deliveryService;
    private IPayment paymentService;

    private ApplicationEventPublisher publisher;


    @BeforeEach
    void setUp() {
        // Mock the dependencies
        storeService = mock(IStoreService.class);
        userService = mock(IUserService.class);
        productService = mock(IProductService.class);
        orderService = mock(IOrderService.class);
        authenticatorService = mock(IAuthenticator.class);
        deliveryService = mock(IDelivery.class);
        paymentService = mock(IPayment.class);
        publisher = mock(ApplicationEventPublisher.class);

        // Inject the mocked services using the overloaded constructor
        systemService = new SystemService(storeService, userService, productService,orderService, deliveryService,
                authenticatorService, paymentService, publisher);

    }

    // closeStore_Founder_Success
    @Test
    void UserRegistration_Guest_Success() {
        // Arrange
        String email = "test@gmail.com";
        String password = "password123";
        String dobInput = "1990-01-01";
        LocalDate dob = LocalDate.parse(dobInput);

        // Mock the behavior of the authenticatorService to return a token
        String mockToken = "mockToken123";
        when(authenticatorService.register(email, password, dob)).thenReturn(mockToken);

        // Act
        // Verify that the response is not null and contains the expected token
        Response<String> response = systemService.guestRegister(email, password, dobInput);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(mockToken, response.getData());
        verify(authenticatorService, times(1)).register(email, password, dob);
    }
    

    @Test
    void UserRegistration_InvalidDateOfBirth_Failure() {
        // Arrange
        String email = "test@gmail.com";
        String password = "password123";
        String invalidDobInput = "invalid-date";

        // Act
        Response<String> response = systemService.guestRegister(email, password, invalidDobInput);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid date of birth format. Expected format: YYYY-MM-DD", response.getMessage());
        verifyNoInteractions(authenticatorService);// Ensure authenticatorService is not called

    }
    

    //We have not defined what is a valid email.
     /* 
    @Test
    void UserRegistration_InvalidEmail_Failure() {
        // Arrange
        String invalidEmail = "invalid-email"; // Invalid email format
        String password = "password123";
        String dobInput = "1990-01-01";

        // Act
        Response<String> response = systemService.guestRegister(invalidEmail, password, dobInput);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid email format", response.getMessage());
        verifyNoInteractions(authenticatorService);

        //old:
        // Act
        //String result = systemService.guestRegister(invalidEmail, password, dobInput);

        // Assert
        //assertNull(result, "Expected guestRegister to return null for invalid email"); 

    }
    */

    @Test
    void GetProductByName_Success() {

        // Arrange
        int productId = 1;
        ProductDTO mockProduct = new ProductDTO("Test Product", "Description", productId);
        when(productService.viewProduct(productId)).thenReturn(mockProduct);

        // Act
        Response<ProductDTO> response = systemService.getProduct(productId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Test Product", response.getData().getName());
        verify(productService, times(1)).viewProduct(productId);

    }

    @Test
    void GetProductByKeyword_Success() {
        // Arrange
        String token = "validToken";
        String keyword = "Test";
        List<ProductDTO> mockProducts = Arrays.asList(
                new ProductDTO("Test Product 1", "Description 1", 1, null),
                new ProductDTO("Test Product 2", "Description 2", 2, null));
        when(authenticatorService.isValid(token)).thenReturn(true);
        when(productService.searchProducts(keyword)).thenReturn(mockProducts);

         // Act
        Response<List<ProductDTO>> response = systemService.searchByKeyword(token, keyword);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
        verify(authenticatorService, times(1)).isValid(token);
        verify(productService, times(1)).searchProducts(keyword);

    }

    @Test
    void GetProductByKeyword_Failure() {
        // Arrange
        String token = "validToken";
        String keyword = "Test";
        when(authenticatorService.isValid(token)).thenReturn(true);
        when(productService.searchProducts(keyword)).thenThrow(new RuntimeException("Search failed"));

        // Act
        Response<List<ProductDTO>> response = systemService.searchByKeyword(token, keyword);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Error during getting product: Search failed", response.getMessage());
        verify(authenticatorService, times(1)).isValid(token);
        verify(productService, times(1)).searchProducts(keyword);
        
    }

    @Test
    void getTopRatedProducts_Success() {
        // Arrange
        int limit = 2;
        // Create ProductDTOs with store IDs included in the constructor
        List<ProductDTO> allProducts = Arrays.asList(
            new ProductDTO("Product 1", "Description 1", 1, new HashSet<>(Arrays.asList(1, 2))),
            new ProductDTO("Product 2", "Description 2", 2, new HashSet<>(Arrays.asList(1))),
            new ProductDTO("Product 3", "Description 3", 3, new HashSet<>(Arrays.asList(2)))
        );
        
        // Create store products with ratings
        StoreProductDTO product1Store1 = new StoreProductDTO(1, "Product 1", 10.0, 5, 4.5);
        StoreProductDTO product1Store2 = new StoreProductDTO(1, "Product 1", 11.0, 3, 3.8);
        StoreProductDTO product2Store1 = new StoreProductDTO(2, "Product 2", 20.0, 10, 4.2);
        StoreProductDTO product3Store2 = new StoreProductDTO(3, "Product 3", 30.0, 7, 4.7);
        
        // Mock the behavior
        when(productService.getAllProducts()).thenReturn(allProducts);
        when(storeService.getProductFromStore(1, 1)).thenReturn(product1Store1);
        when(storeService.getProductFromStore(1, 2)).thenReturn(product1Store2);
        when(storeService.getProductFromStore(2, 1)).thenReturn(product2Store1);
        when(storeService.getProductFromStore(3, 2)).thenReturn(product3Store2);
        
        // Act
        Response<List<StoreProductDTO>> response = systemService.getTopRatedProducts(limit);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
        assertEquals(3, response.getData().get(0).getProductId()); // Product 3 has highest rating (4.7)
        assertEquals(1, response.getData().get(1).getProductId()); // Product 1 from store 1 has second highest (4.5)
        verify(productService, times(1)).getAllProducts();
    }
    
    @Test
    void getTopRatedProducts_EmptyResult() {
        // Arrange
        int limit = 3;
        // Create ProductDTOs with store IDs included in the constructor
        List<ProductDTO> allProducts = Arrays.asList(
            new ProductDTO("Product 1", "Description 1", 1, new HashSet<>(Arrays.asList(1))),
            new ProductDTO("Product 2", "Description 2", 2, new HashSet<>(Arrays.asList(2)))
        );
        
        // Mock behavior for a scenario where products have no ratings (NaN) or errors occur
        when(productService.getAllProducts()).thenReturn(allProducts);
        when(storeService.getProductFromStore(1, 1)).thenThrow(new IllegalArgumentException("Product not found"));
        when(storeService.getProductFromStore(2, 2)).thenReturn(
            new StoreProductDTO(2, "Product 2", 20.0, 10, Double.NaN) // No ratings
        );
        
        // Act
        Response<List<StoreProductDTO>> response = systemService.getTopRatedProducts(limit);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(0, response.getData().size()); // No products with valid ratings
        verify(productService, times(1)).getAllProducts();
    }

}
