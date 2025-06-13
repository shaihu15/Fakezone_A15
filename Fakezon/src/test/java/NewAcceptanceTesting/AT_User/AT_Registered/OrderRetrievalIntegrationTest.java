package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import DomainLayer.Enums.PaymentMethod;
import ApplicationLayer.Services.SystemService;


@SpringBootTest(classes = FakezoneApplication.class)
public class OrderRetrievalIntegrationTest {

    @Autowired
    private SystemService systemService;

    // Test data
    private int storeId;
    private int userId;
    private int ownerId;
    private int productId1;
    private int productId2;
    private final double PRODUCT_PRICE_1 = 100.0;
    private final double PRODUCT_PRICE_2 = 50.0;
    private final LocalDate USER_DOB = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setUp() {
        // Initialize repositories
        
        systemService.clearAllData();

        setupTestData();
    }

    private void setupTestData() {
        try {
            // Register and login owner
            String ownerEmail = "owner@test.com";
            String ownerPassword = "StrongPass123";
            String ownerDob = "1985-01-01";
            String country = "IL";
            
            Response<String> ownerRegResponse = systemService.guestRegister(ownerEmail, ownerPassword, ownerDob, country);
            assertTrue(ownerRegResponse.isSuccess(), "Owner registration failed: " + ownerRegResponse.getMessage());
            
            Response<AbstractMap.SimpleEntry<UserDTO, String>> ownerLoginResponse = systemService.login(ownerEmail, ownerPassword);
            assertTrue(ownerLoginResponse.isSuccess(), "Owner login failed: " + ownerLoginResponse.getMessage());
            ownerId = ownerLoginResponse.getData().getKey().getUserId();

            // Register and login regular user
            String userEmail = "user@test.com";
            String userPassword = "StrongPass456";
            String userDob = "1990-01-01";
            
            Response<String> userRegResponse = systemService.guestRegister(userEmail, userPassword, userDob, country);
            assertTrue(userRegResponse.isSuccess(), "User registration failed: " + userRegResponse.getMessage());
            
            Response<AbstractMap.SimpleEntry<UserDTO, String>> userLoginResponse = systemService.login(userEmail, userPassword);
            assertTrue(userLoginResponse.isSuccess(), "User login failed: " + userLoginResponse.getMessage());
            userId = userLoginResponse.getData().getKey().getUserId();

            // Create store
            Response<Integer> storeResponse = systemService.addStore(ownerId, "Test Store");
            assertTrue(storeResponse.isSuccess(), "Store creation failed: " + storeResponse.getMessage());
            storeId = storeResponse.getData();

            // Add products to store
            Response<StoreProductDTO> product1Response = systemService.addProductToStore(storeId, ownerId, "Test Product 1", "Description 1", 
                PRODUCT_PRICE_1, 100, "ELECTRONICS");
            assertTrue(product1Response.isSuccess(), "Product 1 creation failed: " + product1Response.getMessage());
            productId1 = product1Response.getData().getProductId();

            Response<StoreProductDTO> product2Response = systemService.addProductToStore(storeId, ownerId, "Test Product 2", "Description 2", 
                PRODUCT_PRICE_2, 100, "ELECTRONICS");
            assertTrue(product2Response.isSuccess(), "Product 2 creation failed: " + product2Response.getMessage());
            productId2 = product2Response.getData().getProductId();

        } catch (Exception e) {
            fail("Failed to setup test data: " + e.getMessage());
        }
    }

    @Test
    void testGetOrdersByUser_NoOrders_ReturnsEmptyList() {
        try {
            Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);
            
            assertTrue(response.isSuccess(), "Should successfully retrieve empty order list");
            assertNotNull(response.getData(), "Order list should not be null");
            assertTrue(response.getData().isEmpty(), "Order list should be empty for new user");
            
            System.out.println("✓ No orders test passed: Empty list returned for new user");

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testGetOrdersByUser_WithSingleOrder() {
        try {
            // Add product to cart and purchase
            systemService.addToBasket(userId, productId1, storeId, 1);
            
            Response<String> purchaseResponse = systemService.purchaseCart(
                userId, "IL", USER_DOB, PaymentMethod.CREDIT_CARD,
                "Standard", "123456789", "Test User", "12/25", "123",
                "Test Address*Test city*IL*12345", "Test Recipient", "Test Package"
            );
            assertTrue(purchaseResponse.isSuccess(), "Purchase failed: " + purchaseResponse.getMessage());

            // Get orders
            Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);
            
            assertTrue(response.isSuccess(), "Should successfully retrieve orders");
            assertNotNull(response.getData(), "Order list should not be null");
            assertEquals(1, response.getData().size(), "Should have exactly one order");
            
            OrderDTO order = response.getData().get(0);
            assertEquals(userId, order.getUserId(), "Order should belong to test user");
            assertEquals(storeId, order.getStoreId(), "Order should be from test store");
            assertEquals(1, order.getProducts().size(), "Order should contain one product");
            ProductDTO product = order.getProducts().iterator().next();
            assertEquals(productId1, product.getId(), "Order should contain product 1");
            
            System.out.println("✓ Single order test passed:");
            System.out.println("  Order ID: " + order.getOrderId());
            System.out.println("  Products: " + order.getProducts().size());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testGetOrdersByUser_WithMultipleOrders() {
        try {
            // First purchase
            systemService.addToBasket(userId, productId1, storeId, 1);
            Response<String> purchase1Response = systemService.purchaseCart(
                userId, "IL", USER_DOB, PaymentMethod.CREDIT_CARD,
                "Standard", "123456789", "Test User", "12/25", "123",
                "Test Address*Test city*IL*12345", "Test Recipient", "Test Package"
            );
            assertTrue(purchase1Response.isSuccess(), "First purchase failed");

            // Second purchase
            systemService.addToBasket(userId, productId2, storeId, 2);
            Response<String> purchase2Response = systemService.purchaseCart(
                userId, "IL", USER_DOB, PaymentMethod.CREDIT_CARD,
                "Standard", "123456789", "Test User", "12/25", "123",
                "Test Address*Test city*IL*12345", "Test Recipient", "Test Package"
            );
            assertTrue(purchase2Response.isSuccess(), "Second purchase failed");

            // Get orders
            Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);
            
            assertTrue(response.isSuccess(), "Should successfully retrieve orders");
            assertNotNull(response.getData(), "Order list should not be null");
            assertEquals(2, response.getData().size(), "Should have exactly two orders");
            
            // Verify first order
            OrderDTO order1 = response.getData().get(0);
            assertEquals(1, order1.getProducts().size(), "First order should contain one product");
            ProductDTO product1 = order1.getProducts().iterator().next();
            assertEquals(productId1, product1.getId(), "First order should contain product 1");
            assertEquals(storeId, order1.getStoreId(), "First order should be from test store");
            // Verify second order
            OrderDTO order2 = response.getData().get(1);
            assertEquals(1, order2.getProducts().size(), "Second order should contain one product");
            ProductDTO product2 = order2.getProducts().iterator().next();
            assertEquals(productId2, product2.getId(), "Second order should contain product 2");
            assertEquals(storeId, order2.getStoreId(), "Second order should be from test store");

            System.out.println("✓ Multiple orders test passed:");
            System.out.println("  Total orders: " + response.getData().size());
            System.out.println("  First order products: " + order1.getProducts().size());
            System.out.println("  Second order products: " + order2.getProducts().size());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testGetOrdersByUser_InvalidUserId() {
        try {
            int invalidUserId = -1;
            Response<List<OrderDTO>> response = systemService.getOrdersByUserId(invalidUserId);
            
            assertFalse(response.isSuccess(), "Should fail for invalid user ID");
            assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType(), "Should return INTERNAL_ERROR error type");
            
            System.out.println("✓ Invalid user ID test passed: " + response.getMessage());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testGetOrdersByUser_NotLoggedIn() {
        try {
            // Logout user
            Response<Void> logoutResponse = systemService.userLogout(userId);
            assertTrue(logoutResponse.isSuccess(), "Logout failed");

            // Try to get orders
            Response<List<OrderDTO>> response = systemService.getOrdersByUserId(userId);
            
            assertFalse(response.isSuccess(), "Should fail for logged out user");
            assertEquals(ErrorType.INVALID_INPUT, response.getErrorType(), "Should return INVALID_INPUT error type");
            
            System.out.println("✓ Not logged in test passed: " + response.getMessage());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}