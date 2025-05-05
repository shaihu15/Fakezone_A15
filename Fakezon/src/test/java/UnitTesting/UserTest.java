package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import ApplicationLayer.Enums.PCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Services.UserService;
import DomainLayer.Model.User;
import InfrastructureLayer.Repositories.UserRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Basket;
import org.mockito.Mockito;

import com.vaadin.open.App;

import java.time.LocalDate;

public class UserTest {

    private IUserRepository userRepository; // Mocked user repository
    
    private UserService userService; // Class under test
    private User user;
    @BeforeEach
    void setUp() {
        // Initialize a User object before each test
        user = new User();
        userRepository = new UserRepository();
        userService = new UserService(userRepository); // Initialize the UserService with the mocked repository
    }

    @Test
    void testAddToBasket() {
        // Arrange
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5,1,PCategory.ELECTRONICS);

        // Act
        user.addToBasket(storeId, product);

        // Assert
        Basket basket = user.getBasket(storeId);
        assertNotNull(basket, "Basket should not be null after adding a product");
        assertEquals(1, basket.getProducts().size(), "Basket should contain one product");
        assertEquals("Test Product", basket.getProducts().get(0).getName(), "Product name should match");
    }

    @Test
    void testViewCart() {
        // Arrange
        int storeId = 1;
        int storeId2 = 2;
        StoreProductDTO product1 = new StoreProductDTO(101, "Product 1", 10.0, 2, 4.5, 1, PCategory.ELECTRONICS);
        StoreProductDTO product2 = new StoreProductDTO(102, "Product 2", 20.0, 1, 5.0,2,    PCategory.BEAUTY);


        user.addToBasket(storeId, product1);
        user.addToBasket(storeId2, product2);


        // Act
        List<StoreProductDTO> cartProducts = user.viewCart();

        // Assert
        assertNotNull(cartProducts, "Cart products should not be null");
        assertEquals(2, cartProducts.size(), "Cart should contain two products");
    }

    @Test
    void testClearCart() {
        // Arrange
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5,1, PCategory.ELECTRONICS);
        user.addToBasket(storeId, product);

        // Act
        user.clearCart();

        // Assert
        List<StoreProductDTO> cartProducts = user.viewCart();
        assertTrue(cartProducts.isEmpty(), "Cart should be empty after clearing");
    }

    @Test
    void testLogout() {
        // Arrange
        int storeId = 1;
        StoreProductDTO product = new StoreProductDTO(101, "Test Product", 10.0, 2, 4.5,1, PCategory.ELECTRONICS);
        user.addToBasket(storeId, product);

        // Act
        boolean result = user.logout();

        // Assert
        assertTrue(result, "Logout should return true");
        List<StoreProductDTO> cartProducts = user.viewCart();
        assertTrue(cartProducts.isEmpty(), "Cart should be empty after logout");
    }
    
}