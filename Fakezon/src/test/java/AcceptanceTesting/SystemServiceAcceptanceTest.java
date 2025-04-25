package AcceptanceTesting;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.SystemService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;

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
    private IDelivery deliveryService;
    private IPayment paymentService;

    @BeforeEach
    void setUp() {
    // Mock the dependencies
    storeService = mock(IStoreService.class);
    userService = mock(IUserService.class);
    productService = mock(IProductService.class);
    authenticatorService = mock(IAuthenticator.class);
    deliveryService = mock(IDelivery.class);
    paymentService = mock(IPayment.class);

    // Inject the mocked services using the overloaded constructor
    systemService = new SystemService(storeService, userService, productService, deliveryService, authenticatorService, paymentService);

    }

    // closeStore_Founder_Success
    @Test
    void UserRegistration_Guest_Success() {
        // Arrange
        String email = "test@gmail.com";
        String password = "password123";
        String dobInput = "1990-01-01";
        LocalDate dob = LocalDate.parse(dobInput);
        Registered user = new Registered(email, password, dob);
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(user)); // User exists

        // Act
        systemService.guestRegister(email, password, dobInput);
        assertEquals(email, this.userRepository.findByUserName(email).get().getEmail());
    }
    @Test
    void GetProductByName_Success() {
        
        // Arrange
        int productId = 1;
        ProductDTO mockProduct = new ProductDTO("Test Product", "Description");
        when(productService.viewProduct(productId)).thenReturn(mockProduct);

        // Act
        ProductDTO result = systemService.getProduct(productId);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        
    }
    @Test
    void GetProductByKeyword_Success() {
        // Arrange
        String token = "validToken";
        String keyword = "Test";
        List<ProductDTO> mockProducts = Arrays.asList(
                new ProductDTO("Test Product 1", "Description 1"),
                new ProductDTO("Test Product 2", "Description 2")
        );
        when(authenticatorService.isValid(token)).thenReturn(true);
        when(productService.searchProducts(keyword)).thenReturn(mockProducts);

        // Act
        List<ProductDTO> result = systemService.getProduct(token, keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
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
        List<ProductDTO> result = systemService.getProduct(token, keyword);

        // Assert
        assertNull(result);
        verify(authenticatorService, times(1)).isValid(token);
        verify(productService, times(1)).searchProducts(keyword);
    }




}
