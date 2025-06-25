package NewAcceptanceTesting.AT_Store;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.Cart;


@SpringBootTest(classes = FakezoneApplication.class)
@Transactional
public class DiscountPolicyIntegrationTest {

    @Autowired
    private SystemService systemService;


    // Test data - will be populated from actual registration/login
    private int storeId;
    private int userId;
    private int ownerId; 
    private int productId1;
    private int productId2;
    private int productId3;
    private final double PRODUCT_PRICE_1 = 100.0;
    private final double PRODUCT_PRICE_2 = 50.0;
    private final double PRODUCT_PRICE_3 = 200.0;
    private final LocalDate USER_DOB = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setUp() {
        // Initialize repositories that are not yet migrated to Hibernate
        // Note: storeRepository and productRepository are now injected by Spring via @Autowired
        
        // Initialize services
        
        systemService.clearAllData(); // Clear previous data before each test
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
            StoreProductDTO product1DTO = product1Response.getData();
            
            // Since productId might be 0, we need to get it a different way
            // Let's get the store products and find our products by name
            Response<StoreDTO> storeResponse2 = systemService.userAccessStore(storeId);
            assertTrue(storeResponse2.isSuccess(), "Failed to get store info: " + storeResponse2.getMessage());
            StoreDTO store = storeResponse2.getData();
            
            // Find products by name instead of relying on the DTO productId
            for (StoreProductDTO sp : store.getStoreProducts()) {
                if ("Test Product 1".equals(sp.getName())) {
                    productId1 = sp.getProductId();
                }
            }
            
            Response<StoreProductDTO> product2Response = systemService.addProductToStore(storeId, ownerId, "Test Product 2", "Description 2", 
                PRODUCT_PRICE_2, 100, "ELECTRONICS");
            assertTrue(product2Response.isSuccess(), "Product 2 creation failed: " + product2Response.getMessage());
            
            // Get updated store info
            storeResponse2 = systemService.userAccessStore(storeId);
            assertTrue(storeResponse2.isSuccess(), "Failed to get store info: " + storeResponse2.getMessage());
            store = storeResponse2.getData();
            
            for (StoreProductDTO sp : store.getStoreProducts()) {
                if ("Test Product 2".equals(sp.getName())) {
                    productId2 = sp.getProductId();
                }
            }

            Response<StoreProductDTO> product3Response = systemService.addProductToStore(storeId, ownerId, "Test Product 3", "Description 3", 
                PRODUCT_PRICE_3, 100, "ELECTRONICS");
            assertTrue(product3Response.isSuccess(), "Product 3 creation failed: " + product3Response.getMessage());
            
            // Get updated store info
            storeResponse2 = systemService.userAccessStore(storeId);
            assertTrue(storeResponse2.isSuccess(), "Failed to get store info: " + storeResponse2.getMessage());
            store = storeResponse2.getData();
            
            for (StoreProductDTO sp : store.getStoreProducts()) {
                if ("Test Product 3".equals(sp.getName())) {
                    productId3 = sp.getProductId();
                }
            }
            
        } catch (Exception e) {
            fail("Failed to setup test data: " + e.getMessage());
        }
    }

    @Test
    void testSimpleDiscountWithProductsScope_ReducesPriceCorrectly() {
        try {
            // Add 25% discount on Product 1
            double discountPercentage = 25.0;
            List<Integer> productIds = Arrays.asList(productId1);
            
            Response<Void> discountResponse = systemService.addSimpleDiscountWithProductsScope(
                storeId, ownerId, productIds, discountPercentage);
            assertTrue(discountResponse.isSuccess(), "Failed to add discount policy: " + discountResponse.getMessage());

            // Add product to cart
            int quantity = 2;
            Response<Void> addToCartResponse = systemService.addToBasket(userId, productId1, storeId, quantity);
            assertTrue(addToCartResponse.isSuccess(), "Failed to add product to cart: " + addToCartResponse.getMessage());

            // Calculate expected price
            double originalPrice = PRODUCT_PRICE_1 * quantity; // 100 * 2 = 200
            double expectedDiscount = originalPrice * (discountPercentage / 100); // 200 * 0.25 = 50
            double expectedFinalPrice = originalPrice - expectedDiscount; // 200 - 50 = 150

            // Get cart final price using systemService
            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();
            
            assertEquals(expectedFinalPrice, actualPrice, 0.01, 
                "Price should be reduced by 25% discount. Expected: " + expectedFinalPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Simple discount test passed:");
            System.out.println("  Original price: $" + originalPrice);
            System.out.println("  Discount (25%): -$" + expectedDiscount);
            System.out.println("  Final price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testSimpleDiscountWithStoreScope_ReducesPriceCorrectly() {
        try {
            // Add 15% store-wide discount
            double discountPercentage = 15.0;
            
            Response<Void> discountResponse = systemService.addSimpleDiscountWithStoreScope(
                storeId, ownerId, discountPercentage);
            assertTrue(discountResponse.isSuccess(), "Failed to add store discount policy: " + discountResponse.getMessage());

            // Add multiple products to cart
            systemService.addToBasket(userId, productId1, storeId, 1); // $100
            systemService.addToBasket(userId, productId2, storeId, 2); // $50 * 2 = $100
            
            double originalPrice = PRODUCT_PRICE_1 + (PRODUCT_PRICE_2 * 2); // 100 + 100 = 200
            double expectedDiscount = originalPrice * (discountPercentage / 100); // 200 * 0.15 = 30
            double expectedFinalPrice = originalPrice - expectedDiscount; // 200 - 30 = 170

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            assertEquals(expectedFinalPrice, actualPrice, 0.01,
                "Store-wide discount should reduce total price by 15%. Expected: " + expectedFinalPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Store-wide discount test passed:");
            System.out.println("  Original price: $" + originalPrice);
            System.out.println("  Discount (15%): -$" + expectedDiscount);
            System.out.println("  Final price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testMultipleDiscounts_AppliedCumulatively() {
        try {
            // Add 20% discount on Product 1 specifically
            Response<Void> productDiscountResponse = systemService.addSimpleDiscountWithProductsScope(
                storeId, ownerId, Arrays.asList(productId1), 20.0);
            assertTrue(productDiscountResponse.isSuccess());

            // Add 10% store-wide discount  
            Response<Void> storeDiscountResponse = systemService.addSimpleDiscountWithStoreScope(
                storeId, ownerId, 10.0);
            assertTrue(storeDiscountResponse.isSuccess());

            // Add products to cart
            systemService.addToBasket(userId, productId1, storeId, 1); // $100, should get both discounts
            systemService.addToBasket(userId, productId2, storeId, 1); // $50, should only get store discount

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            // Expected calculation:
            // Product 1: $100 - 20% (product discount) - 10% (store discount) = $100 - $20 - $10 = $70
            // Product 2: $50 - 10% (store discount) = $50 - $5 = $45
            // Total: $70 + $45 = $115
            double expectedPrice = 115.0; // Product 1: $70, Product 2: $45

            assertEquals(expectedPrice, actualPrice, 0.01,
                "Multiple discounts should be applied cumulatively. Expected: " + expectedPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Multiple discounts test passed:");
            System.out.println("  Product 1 ($100): 20% + 10% discount = $70");
            System.out.println("  Product 2 ($50): 10% discount = $45");
            System.out.println("  Total final price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testConditionDiscountWithProductsScope_WithConditionMet() {
        try {
            // Create a condition: cart must have more than 1 product
            List<Predicate<Cart>> conditions = Arrays.asList(
                cart -> cart.getAllProducts().values().stream()
                    .mapToInt(basket -> basket.size())
                    .sum() > 1
            );

            Response<Void> discountResponse = systemService.addConditionDiscountWithProductsScope(
                storeId, ownerId, 0, Arrays.asList(productId1), conditions, 30.0);
            assertTrue(discountResponse.isSuccess());

            // Add products to meet condition
            systemService.addToBasket(userId, productId1, storeId, 1); // $100 - should get discount
            systemService.addToBasket(userId, productId2, storeId, 1); // $50 - condition met

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            // Expected: Product 1 gets 30% discount = $100 - $30 = $70, Product 2 stays $50 = $120 total
            double expectedPrice = 120.0;

            assertEquals(expectedPrice, actualPrice, 0.01,
                "Condition discount should apply when condition is met. Expected: " + expectedPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Condition discount test passed:");
            System.out.println("  Condition met: Cart has more than 1 product");
            System.out.println("  Product 1 with 30% discount: $70");
            System.out.println("  Total price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testConditionDiscountWithStoreScope_WithConditionNotMet() {
        try {
            // Create a condition: cart total must be over $300
            List<Predicate<Cart>> conditions = Arrays.asList(
                cart -> {
                    double sum = 0.0;
                    for (Map.Entry<Integer, Map<Integer, Integer>> storeEntry : cart.getAllProducts().entrySet()) {
                        for (Map.Entry<Integer, Integer> prodEntry : storeEntry.getValue().entrySet()) {
                            Response<StoreProductDTO> productResp = systemService.getProductFromStore(prodEntry.getKey(), storeEntry.getKey());
                            assertTrue(productResp.isSuccess());
                            StoreProductDTO product = productResp.getData();
                            sum += product.getBasePrice() * prodEntry.getValue();
                        }
                    }
                    return sum > 300.0;
                }
            );

            Response<Void> discountResponse = systemService.addConditionDiscountWithStoreScope(
                storeId, ownerId, 0, conditions, 25.0);
            assertTrue(discountResponse.isSuccess());

            // Add products but don't meet condition
            systemService.addToBasket(userId, productId1, storeId, 1); // $100
            systemService.addToBasket(userId, productId2, storeId, 1); // $50
            // Total: $150 - condition not met (needs > $300)

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            // Expected: No discount applied, original price $150
            double expectedPrice = 150.0;

            assertEquals(expectedPrice, actualPrice, 0.01,
                "No discount should apply when condition is not met. Expected: " + expectedPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Condition not met test passed:");
            System.out.println("  Condition: Cart total > $300 (actual: $150)");
            System.out.println("  No discount applied, price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testZeroPercentDiscount_NoReduction() {
        try {
            // Add 0% discount
            Response<Void> discountResponse = systemService.addSimpleDiscountWithStoreScope(
                storeId, ownerId, 0.0);
            assertTrue(discountResponse.isSuccess());

            systemService.addToBasket(userId, productId1, storeId, 1);

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            assertEquals(PRODUCT_PRICE_1, actualPrice, 0.01,
                "0% discount should not change price");

            System.out.println("✓ Zero discount test passed: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testMaximumDiscount_HundredPercent() {
        try {
            // Add 100% discount
            Response<Void> discountResponse = systemService.addSimpleDiscountWithStoreScope(
                storeId, ownerId, 100.0);
            assertTrue(discountResponse.isSuccess());

            systemService.addToBasket(userId, productId1, storeId, 1);

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            assertEquals(0.0, actualPrice, 0.01,
                "100% discount should make price $0");

            System.out.println("✓ Maximum discount test passed: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void testDiscountOnMultipleQuantities() {
        try {
            // Add 20% discount on Product 1
            Response<Void> discountResponse = systemService.addSimpleDiscountWithProductsScope(
                storeId, ownerId, Arrays.asList(productId1), 20.0);
            assertTrue(discountResponse.isSuccess());

            // Add multiple quantities
            int quantity = 5;
            systemService.addToBasket(userId, productId1, storeId, quantity);

            Response<Double> finalPriceResponse = systemService.getCartFinalPrice(userId, USER_DOB);
            assertTrue(finalPriceResponse.isSuccess(), "Failed to get cart final price: " + finalPriceResponse.getMessage());
            double actualPrice = finalPriceResponse.getData();

            double originalPrice = PRODUCT_PRICE_1 * quantity; // $100 * 5 = $500
            double expectedDiscount = originalPrice * 0.20; // $500 * 0.20 = $100
            double expectedPrice = originalPrice - expectedDiscount; // $500 - $100 = $400

            assertEquals(expectedPrice, actualPrice, 0.01,
                "Discount should apply to total quantity. Expected: " + expectedPrice + ", Actual: " + actualPrice);

            System.out.println("✓ Multiple quantity discount test passed:");
            System.out.println("  5 units at $100 each = $500");
            System.out.println("  20% discount = -$100");
            System.out.println("  Final price: $" + actualPrice);

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    // @Test
    // void testDiscountDoesNotApplyToOtherStores() {
    //     try {
    //         // Create a second store
    //         Response<Integer> secondStoreResponse = systemService.addStore(ownerId, "Second Store");
    //         assertTrue(secondStoreResponse.isSuccess());
    //         int secondStoreId = secondStoreResponse.getData();
            
    //         // Add product to second store
    //         Response<StoreProductDTO> secondStoreProductResponse = systemService.addProductToStore(
    //             secondStoreId, ownerId, "Test Product 1", "Description 1", 
    //             PRODUCT_PRICE_1, 100, "ELECTRONICS");
    //         assertTrue(secondStoreProductResponse.isSuccess());
    //         int secondStoreProductId = secondStoreProductResponse.getData()

    //         // Add discount only to first store
    //         Response<Void> discountResponse = systemService.addSimpleDiscountWithStoreScope(
    //             storeId, ownerId, 50.0);
    //         assertTrue(discountResponse.isSuccess());

    //         // Add same product from both stores
    //         systemService.addToBasket(userId, productId1, storeId, 1);      // Should get discount
    //         systemService.addToBasket(userId, secondStoreProductId, secondStoreId, 1); // Should NOT get discount

    //         Cart userCart = userService.getUserCart(userId);
    //         Map<Integer, Double> storeAmounts = storeService.calcAmount(userId, userCart, USER_DOB);

    //         // Store 1 should have discounted price: $100 * 0.5 = $50
    //         // Store 2 should have original price: $100
    //         double store1Amount = storeAmounts.get(storeId);
    //         double store2Amount = storeAmounts.get(secondStoreId);

    //         assertEquals(50.0, store1Amount, 0.01, "First store should have discounted price");
    //         assertEquals(100.0, store2Amount, 0.01, "Second store should have original price");

    //         System.out.println("✓ Store isolation test passed:");
    //         System.out.println("  Store 1 (with discount): $" + store1Amount);
    //         System.out.println("  Store 2 (no discount): $" + store2Amount);

    //     } catch (Exception e) {
    //         fail("Test failed with exception: " + e.getMessage());
    //     }
    // }
} 