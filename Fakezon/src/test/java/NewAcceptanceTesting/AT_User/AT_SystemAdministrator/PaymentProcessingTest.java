package NewAcceptanceTesting.AT_User.AT_SystemAdministrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import ApplicationLayer.Services.*;
import DomainLayer.Enums.PaymentMethod;

import NewAcceptanceTesting.TestHelper;
import com.fakezone.fakezone.FakezoneApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;

@SpringBootTest(classes = FakezoneApplication.class)

public class PaymentProcessingTest {

    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    private int storeId;
    private int userId;
    private int productId;

    @BeforeEach
    void setUp() {


        testHelper = new TestHelper(systemService);

        // Register and login a user
        Response<UserDTO> userResponse = testHelper.register_and_login();
        assertNotNull(userResponse, "User registration and login failed");
        assertTrue(userResponse.isSuccess(), "User registration and login was not successful");
        userId = userResponse.getData().getUserId();

        // Open a store
        Response<Integer> storeResponse = testHelper.openStore(userId);
        assertNotNull(storeResponse, "Store creation failed");
        assertTrue(storeResponse.isSuccess(), "Store creation was not successful");
        storeId = storeResponse.getData();

        // Add a product to the store
        Response<StoreProductDTO> productResponse = testHelper.addProductToStore(storeId, userId);
        assertNotNull(productResponse, "Adding product to store failed");
        assertTrue(productResponse.isSuccess(), "Adding product to store was not successful");
        productId = productResponse.getData().getProductId();
    }

    @Test
    void testSuccessfulPayment() {
        // Add product to cart
        Response<Void> addToCartResponse = systemService.addToBasket(userId, productId, storeId, 1); // Corrected productId
        assertTrue(addToCartResponse.isSuccess(), "Adding product to basket failed");

        // Act
        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(testHelper.validBirthDate_Over18()),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "1234567812345678", // Valid card number
            "John Doe",
            "12/25",
            "123",
            "123 Main St",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertTrue(paymentResponse.isSuccess(), "Payment was not successful");
        assertEquals("Cart purchased successfully", paymentResponse.getMessage());
    }

    @Test
    void testInvalidPaymentDetails() {
        // Add product to cart
        Response<Void> addToCartResponse = systemService.addToBasket(userId, productId, storeId, 1); // Corrected productId
        assertTrue(addToCartResponse.isSuccess(), "Adding product to basket failed");

        // Act
        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(testHelper.validBirthDate_Over18()),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "INVALID_CARD", // Invalid card number
            "John Doe",
            "12/25",
            "123",
            "123 Main St",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertFalse(paymentResponse.isSuccess(), "Payment should have failed with invalid card details");
        assertEquals("Invalid payment details", paymentResponse.getMessage());
    }

    @Test
    void testOrderNotInStock() {
        // Attempt to add an out-of-stock product to the cart
        Response<Void> addToCartResponse = systemService.addToBasket(userId, 999, storeId, 1); // Non-existent Product ID
        assertFalse(addToCartResponse.isSuccess(), "Adding out-of-stock product should have failed");

        // Act
        Response<String> paymentResponse = systemService.purchaseCart(
            userId,
            "IL",
            LocalDate.parse(testHelper.validBirthDate_Over18()),
            PaymentMethod.CREDIT_CARD,
            "Standard Delivery",
            "1234567812345678", // Valid card number
            "John Doe",
            "12/25",
            "123",
            "123 Main St",
            "John Doe",
            "Order Details"
        );

        // Assert
        assertFalse(paymentResponse.isSuccess(), "Payment should have failed for out-of-stock items");
        assertEquals("Order contains out-of-stock items", paymentResponse.getMessage());
    }
}