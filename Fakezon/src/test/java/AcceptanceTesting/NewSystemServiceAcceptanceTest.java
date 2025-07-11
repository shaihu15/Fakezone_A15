package AcceptanceTesting;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
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
import DomainLayer.Interfaces.IOrderRepository;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import DomainLayer.Model.*;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.*;
import com.fakezone.fakezone.FakezoneApplication;

@SpringBootTest(classes = FakezoneApplication.class)
@Transactional
public class NewSystemServiceAcceptanceTest {
    @Autowired
    private SystemService systemService;
    
    @Autowired
    private IStoreRepository storeRepository;
    
    @Autowired
    private IUserRepository userRepository;
    
    @Autowired
    private IProductRepository productRepository;
    
    @Autowired
    private IOrderRepository orderRepository;
    
    @Autowired
    private IDelivery deliveryService;
    
    @Autowired
    private IAuthenticator authenticatorService;
    
    @Autowired
    private IPayment paymentService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private INotificationWebSocketHandler notificationWebSocketHandler;
    
    @Autowired
    private IStoreService storeService;
    
    @Autowired
    private IProductService productService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IOrderService orderService;
    
    private String systemAdmainEmail = "SystemAdmain@gmail.com";
    private String systemAdmainPassword = "IAmTheSystemAdmain";
    private String systemAdmainCountry = "IL";
    private LocalDate systemAdmainBirthDate = LocalDate.of(2000, 1, 1);
    
    @BeforeEach
    void setUp() {
        // Clear all data before each test
        if (storeRepository != null) storeRepository.clearAllData();
        if (userRepository != null) userRepository.clearAllData();
        if (productRepository != null) productRepository.clearAllData();
        if (orderRepository != null) orderRepository.clearAllData();
        
        systemService.guestRegister(systemAdmainEmail, systemAdmainPassword, systemAdmainBirthDate.toString(), systemAdmainCountry);
    }
/* 
    @Test
    void testRegisterUser_validArguments_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> result = systemService.guestRegister(email, password, birthDate, country);
        System.out.println("Result: " + result.getMessage()+ " " + result.isSuccess());
        assertEquals("Guest registered successfully", result.getMessage());
    }
    @Test
    void testRegisterUser_validAndInvalidArguments() {
        String validEmail = "test@gmail.com";
        String invalidEmail = "invalid-email"; // no @
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String validCountry = "IL";
        String invalidCountry = "XYZ"; // invalid country code
    
        // Test valid input — should succeed
        Response<String> result = systemService.guestRegister(validEmail, password, birthDate, validCountry);
        System.out.println("Valid result: " + result.getMessage() + " " + result.isSuccess());
        assertTrue(result.isSuccess());
        assertEquals("Guest registered successfully", result.getMessage());
    
        // Test invalid email — should fail (assuming email validation is implemented)
        Response<String> resultInvalidEmail = systemService.guestRegister(invalidEmail, password, birthDate, validCountry);
        System.out.println("Invalid email result: " + resultInvalidEmail.getMessage() + " " + resultInvalidEmail.isSuccess());
        assertFalse(resultInvalidEmail.isSuccess());
        // assertEquals("Invalid email format", resultInvalidEmail.getMessage()); // uncomment if your service returns this message
    
        // Test invalid country — should fail
        Response<String> resultInvalidCountry = systemService.guestRegister(validEmail, password, birthDate, invalidCountry);
        System.out.println("Invalid country result: " + resultInvalidCountry.getMessage() + " " + resultInvalidCountry.isSuccess());
        assertFalse(resultInvalidCountry.isSuccess());
        assertEquals("Invalid country code", resultInvalidCountry.getMessage());
    }
    @Test
    void testLoginUser_validCredentials_Success() {
        // Arrange
        String email = "user@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String country = "IL";

        // Register the user
        Response<String> registerResponse = systemService.guestRegister(email, password, birthDate, country);
        assertTrue(registerResponse.isSuccess(), "User registration should succeed");

        // Act
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);

        // Assert
        assertNotNull(loginResponse, "Login response should not be null");
        assertTrue(loginResponse.isSuccess(), "Login should succeed with valid credentials");
    }
    @Test
    void testLoginUser_invalidCredentials_Failure() {
        // Arrange
        String email = "user@gmail.com";
        String password = "password123";
        String invalidPassword = "wrongPassword";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String country = "IL";
    
        // Register the user
        Response<String> registerResponse = systemService.guestRegister(email, password, birthDate, country);
        assertTrue(registerResponse.isSuccess(), "User registration should succeed");
    
        // Act
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, invalidPassword);
    
        // Assert
        assertNotNull(loginResponse, "Login response should not be null");
        assertFalse(loginResponse.isSuccess(), "Login should fail with invalid credentials");
    }    
    @Test
    void testSearchByCategory_validCategoryExistingProduct_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        int storeId = resultAddStore.getData();
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storeProductDTO = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        int productId = storeProductDTO.getData().getProductId();
        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        Response<List<ProductDTO>> result = systemService.searchByCategory(category);
        assertNotNull(result, "Search result should not be null");
        assertTrue(result.isSuccess(), "Search should succeed with valid category");
        assertEquals(productName, result.getData().get(0).getName(), "Product name should match the searched category");
    }

    @Test
    void testSearchByCategory_validCategoryNonExistingProduct_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        int storeId = resultAddStore.getData();
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        String nonExistingCategory = PCategory.BEAUTY.toString(); // Non-existing category
        Response<StoreProductDTO> storeProductDTO = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        int productId = storeProductDTO.getData().getProductId();
        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        Response<List<ProductDTO>> result = systemService.searchByCategory(nonExistingCategory);
        assertNotNull(result, "Search result should not be null");
        assertTrue(result.getData().isEmpty(), "Search result should be empty for non-existing pruducts in the category");
        assertTrue(result.isSuccess(), "Search should succeed with valid category");
    }

    // changed the logic of searchByCategory to return empty list if no products found instead of failure
    //  I did it that the UI will not throw http exeption when the user types a wrong category
    @Test
    void testSearchByCategory_invalidCategory_Failure() {
        // Arrange
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        systemService.guestRegister(email, password, birthDate, country);    
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUser=systemService.login(email, password);
        String invalidCategory = "INVALID_CATEGORY"; // Invalid category
        Response<List<ProductDTO>> result = systemService.searchByCategory(invalidCategory);
        assertTrue(result.getData().isEmpty(), "Data should be an empty list for invalid category");
        assertTrue(result.isSuccess(), "Search should succeed even with invalid category, returning an empty list");
        assertEquals("Invalid category", result.getMessage(), "Error message should indicate invalid category");
    }

    @Test
    void testAddStore_validArguments_Success(){
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        assertTrue(resultUserLogin.isSuccess());
        assertNotNull(resultUserLogin.getData(), "User login should succeed and return user data");
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        assertTrue(resultAddStore.isSuccess());
        assertNotNull(resultAddStore.getData(), "Store ID should not be null");
        System.out.println("Store ID: " + resultAddStore.getData());
        assertTrue(resultAddStore.getData() > 0, "Store ID should be greater than 0");
    }

    @Test
    void testAddProductToStore_validArguments_Success(){
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        int storeId = resultAddStore.getData();
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertTrue(resultAddStore.isSuccess(), "Product addition should succeed");
        Response<ProductDTO> resultGetProduct = systemService.getProduct(1);
        // assertTrue(resultGetProduct.isSuccess(), "Product retrieval should succeed");
        // assertEquals(productName, resultGetProduct.getData().getName());
    }

    @Test
    void testLogout_validUserLoggedIn_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        assertTrue(resultUserLogin.isSuccess(), "User login should succeed");
        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess(), "User logout should succeed");

    }
    @Test
    void testLogout_invalidUserNotLoggedIn_Failure() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        assertTrue(resultUserLogin.isSuccess(), "User login should succeed");
        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess(), "User logout should succeed");
        Response<Void> resultLogoutAgain=systemService.userLogout(userId);
        assertFalse(resultLogoutAgain.isSuccess(), "User logout should fail if user is not logged in");
    }
    @Test
    void testLogout_invalidUserNotExist_Failure() {
        int userId = 9999; // Assuming this user ID does not exist
        Response<Void> resultLogoutAgain=systemService.userLogout(userId);
        assertFalse(resultLogoutAgain.isSuccess(), "User logout should fail if user not exist");
    }
    @Test
    void testLogout_validUserLoggedInWithCart_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        int storeId = resultAddStore.getData();
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        int productId = storePResponse.getData().getProductId();
        systemService.addToBasket(userId, productId, storeId, 1);
        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess(), "User logout should succeed");
        // After logout, the cart should be empty or not accessible
        // need to fix viewCart as guest

        
        // Response<List<StoreProductDTO>> cart = systemService.viewCart(userId);
        // assertNotNull(cart, "Cart should not be null after logout");
        // assertTrue(cart.isSuccess(), "Cart retrieval should succeed after logout");
        // assertTrue(cart.getData().isEmpty(), "Cart should be empty after logout");
    }
    @Test
    void testAddToCart_validProduct_Success() {
        String email = "test@gmail.com";
        String password = "password123";
        String birthDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format the date        System.out.println("BirthDate: " + birthDate);
        String country = "IL";
        Response<String> resultRegister = systemService.guestRegister(email, password, birthDate, country);
        Response<AbstractMap.SimpleEntry<UserDTO, String>> resultUserLogin=systemService.login(email, password);
        int userId = resultUserLogin.getData().getKey().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        int storeId = resultAddStore.getData();
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        int productId = storePResponse.getData().getProductId();
        systemService.addToBasket(userId, productId, storeId, 1);
        Response<List<CartItemInfoDTO>> cart = systemService.viewCart(userId);
        assertNotNull(cart, "Cart should not be null");
        assertTrue(cart.isSuccess(), "Cart retrieval should succeed");
        assertTrue(cart.getData().size() > 0, "Cart should contain products");
    }
*/

}


    