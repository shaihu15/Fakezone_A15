package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Services.SystemService;


@SpringBootTest(classes = FakezoneApplication.class)



public class Product_Search {
    //Use-case: 2.2 Product Search

   @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int productId ;
    int userId;
    int storeId;
    String productName = "Test Product";
    String productDescription = "Test Description";
    String category = PCategory.ELECTRONICS.toString();
    String username;

    @BeforeEach
    void setUp() {
        testHelper = new TestHelper(systemService);

        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertNotNull(StoreOwnerResult.getData());
        userId = StoreOwnerResult.getData().getUserId();
        username = StoreOwnerResult.getData().getUserEmail();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(userId, "Store1");
        assertNotNull(storeResult.getData());
        storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        productName = "Test Product";
        productDescription = "Test Description";
        category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse.getData());
        productId = storePResponse.getData().getProductId();
        // StoreOwner added a product to Store1
    }

    @AfterEach
    void tearDown() {
        // Close the store (ignore if already closed)
        Response<String> deleteStoreResponse = systemService.closeStoreByFounder(storeId, userId);
        if (!deleteStoreResponse.isSuccess()) {
            assertEquals("Error during closing store: Store: " + storeId + " is already closed", deleteStoreResponse.getMessage());
        }

        // Remove the store (ignore if already removed, if you have removeStore)
        Response<Void> removeStoreResponse = systemService.removeStore(storeId, userId);
        if (!removeStoreResponse.isSuccess()) {
            assertEquals("Error during removing store: Store not found", removeStoreResponse.getMessage());
        }

        // Delete the user
        Response<Boolean> deleteResponse = systemService.deleteUser(username);
        assertTrue(deleteResponse.isSuccess(), "User deletion should succeed");
    }

    @Test
    void testSearchByCategory_validCategoryExistingProduct_Success() {
        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        String category_toTset = resultGetProduct.getData().getCategory().name();
        Response<List<ProductDTO>> result = systemService.searchByCategory(category_toTset);
        assertNotNull(result.getData());
        assertTrue(result.isSuccess());
        // Fix: Check that at least one product matches the expected name
        boolean found = result.getData().stream()
            .anyMatch(p -> productName.equals(p.getName()));
        assertTrue(found, "Expected to find product with name: " + productName);
    }

    @Test
    void testSearchByCategory_invalidCategory_Failure() {
        String nonExistingCategory = "nonExistingCategory"; 

        Response<ProductDTO> resultGetProduct = systemService.getProduct(productId);
        Response<List<ProductDTO>> result = systemService.searchByCategory(nonExistingCategory);
        System.err.println("result: " + result.getData());
        assertNotNull(resultGetProduct.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testSearchBy_validCategoryExistingProduct_Success() {
        Response<List<ProductDTO>> result1 = systemService.searchByKeyword(productName);
        Response<List<ProductDTO>> result2 = systemService.searchByKeyword("Test");
        Response<List<ProductDTO>> result3 = systemService.searchByKeyword("Product");
        int productIdFromResult2 = result1.getData().get(0).getId();
        int productIdFromResult3 = result1.getData().get(0).getId();

        assertNotNull(result1.getData());
        assertNotNull(result2.getData());
        assertNotNull(result3.getData());
        assertEquals(productIdFromResult2, productIdFromResult3);
    }

    @Test
    void testSearchBy_invalidCategoryExistingProduct_Failure() {
        Response<List<ProductDTO>> result1 = systemService.searchByKeyword(productName);
        Response<List<ProductDTO>> result2 = systemService.searchByKeyword("Invalid keyword");

        assertNotNull(result1.getData());
        assertTrue(result2.getData().isEmpty());
    }
    
}
