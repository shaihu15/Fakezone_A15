package UnitTesting;


import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.ISystemService;
import DomainLayer.Interfaces.IAuthenticator;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

import com.fakezone.fakezone.controller.ProductController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;

import InfrastructureLayer.Adapters.AuthenticatorAdapter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.net.Authenticator;

import com.fakezone.fakezone.controller.ProductController;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.ProductRatingDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;

class ProductControllerTest {

    @Mock
    private ISystemService systemService;

    @Mock
    private AuthenticatorAdapter authenticatorAdapter;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productController = new ProductController(systemService, authenticatorAdapter);
    }

    @Test
    void testGetProduct_Success() {
        int productId = 1;
        String token = "valid-token";
        ProductDTO productDTO = new ProductDTO("product name", "Test Product", productId, PCategory.ELECTRONICS);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProduct(productId)).thenReturn(new Response<>(productDTO, null, true, null, null));

        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(productDTO, response.getBody().getData());
        verify(systemService, times(1)).getProduct(productId);
    }

    @Test
    void testGetProduct_BadRequest() {
        int productId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProduct(productId)).thenReturn(new Response<>(null, "Invalid product ID", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getProduct(productId);
    }

    @Test
    void testGetProduct_InternalError() {
        int productId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProduct(productId)).thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getProduct(productId);
    }

   
    @Test
    void testGetProduct_ExceptionHandling() {
        int productId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProduct(productId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getProduct(productId);
    }

    @Test
    void testUpdateProduct_Success() {
        String token = "valid-token";
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>(token, productDTO);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);


        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds()))
                .thenReturn(new Response<>(true, null, true, null, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds());
    }

    @Test
    void testUpdateProduct_BadRequest() {
        String token = "valid-token";
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>(token, productDTO);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds()))
                .thenReturn(new Response<>(false, "Invalid data", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds());
    }

  
    @Test
    void testUpdateProduct_ExceptionHandling() {
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("valid-token", productDTO);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds());
    }

    @Test
    void testDeleteProduct_Success() {
        String token = "valid-token";
        int productId = 1;        
        Request<Integer> request = new Request<>( token, productId);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);

        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(true, null, true, null, null));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).deleteProduct(productId);
    }

    @Test
    void testDeleteProduct_InternalError() {
        int productId = 1;
        String token = "valid-token";
        Request<Integer> request = new Request<>( token,productId);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(false, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).deleteProduct(productId);
    }

   
    @Test
    void testDeleteProduct_ExceptionHandling() {
        int productId = 1;
        Request<Integer> request = new Request<>("valid-token", productId);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.deleteProduct(productId)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).deleteProduct(productId);
    }

    @Test
    void testSearchProducts_Success() {
        String keyword = "Test";
        String token = "valid-token";
        List<ProductDTO> products = List.of(new ProductDTO("product", "Test Product", 1, PCategory.ELECTRONICS));
        when(authenticatorAdapter.isValid("valid-token")).thenReturn(true);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenReturn(new Response<>(products, null, true, null, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByKeyword(keyword, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }

    @Test
    void testSearchProducts_BadRequest() {
        String keyword = "Test";
        String token = "valid-token";
        when(authenticatorAdapter.isValid("valid-token")).thenReturn(true);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenReturn(new Response<>(null, "Invalid keyword", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByKeyword(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }

    
    @Test
    void testSearchProducts_ExceptionHandling() {
        String keyword = "Test";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByKeyword(keyword, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }

    @Test
    void testGetProductFromStore_Success() {
        int productId = 1;
        int storeId = 1;
        String token = "valid-token";
        StoreProductDTO product = new StoreProductDTO(1, "Test Product", 100.0, 10, 4.5, 1, PCategory.ELECTRONICS);

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProductFromStore(productId, storeId))
                .thenReturn(new Response<>(product, "Success", true, null, null));

        ResponseEntity<Response<StoreProductDTO>> response = productController.getProductFromStore(productId, storeId, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(product, response.getBody().getData());
        verify(systemService, times(1)).getProductFromStore(productId, storeId);
    }


    @Test
    void testGetProductFromStore_InternalServerError() {
        int productId = 1;
        int storeId = 1;
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProductFromStore(productId, storeId))
                .thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<StoreProductDTO>> response = productController.getProductFromStore(productId, storeId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getProductFromStore(productId, storeId);
    }

    @Test
    void testSearchByCategory_Success() {
        String category = "ELECTRONICS";
        String token = "valid-token";
        List<ProductDTO> products = List.of(new ProductDTO("product", "Test Product", 1, PCategory.ELECTRONICS));

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByCategory(category))
                .thenReturn(new Response<>(products, "Success", true, null, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByCategory(category, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).searchByCategory(category);
    }

   
    @Test
    void testSearchByCategory_InternalServerError() {
        String category = "ELECTRONICS";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByCategory(category))
                .thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByCategory(category, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByCategory(category);
    }
        // --- BadResponse tests ---
    
    @Test
    void testGetProduct_BadResponse() {
        int productId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProduct(productId)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getProduct(productId);
    }
    
    @Test
    void testUpdateProduct_BadResponse() {
        String token = "valid-token";
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>(token, productDTO);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds()))
                .thenReturn(new Response<>(false, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoreIds());
    }
    
    @Test
    void testDeleteProduct_BadResponse() {
        int productId = 1;
        String token = "valid-token";
        Request<Integer> request = new Request<>(token, productId);
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(false, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).deleteProduct(productId);
    }
    
    @Test
    void testSearchProducts_BadResponse() {
        String keyword = "Test";
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByKeyword(keyword, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }
    
    @Test
    void testGetProductFromStore_BadResponse() {
        int productId = 1;
        int storeId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getProductFromStore(productId, storeId)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<StoreProductDTO>> response = productController.getProductFromStore(productId, storeId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getProductFromStore(productId, storeId);
    }
    
    @Test
    void testSearchByCategory_BadResponse() {
        String category = "ELECTRONICS";
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByCategory(category)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByCategory(category, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByCategory(category);
    }
    
    // --- InvalidToken tests ---
    
    @Test
    void testGetProduct_InvalidToken() {
        int productId = 1;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getProduct(anyInt());
    }
    
    @Test
    void testUpdateProduct_InvalidToken() {
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("invalid-token", productDTO);
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).updateProduct(anyInt(), anyString(), anyString(), any());
    }
    
    @Test
    void testDeleteProduct_InvalidToken() {
        int productId = 1;
        Request<Integer> request = new Request<>("invalid-token", productId);
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).deleteProduct(anyInt());
    }
    
    @Test
    void testSearchProducts_InvalidToken() {
        String keyword = "Test";
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByKeyword(keyword, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).searchByKeyword(anyString());
    }
    
    @Test
    void testGetProductFromStore_InvalidToken() {
        int productId = 1;
        int storeId = 1;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<StoreProductDTO>> response = productController.getProductFromStore(productId, storeId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).getProductFromStore(anyInt(), anyInt());
    }
    
    @Test
    void testSearchByCategory_InvalidToken() {
        String category = "ELECTRONICS";
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByCategory(category, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).searchByCategory(anyString());
    }

    @Test
    void testSearchProductsByName_Success() {
        String name = "Test";
        String token = "valid-token";
        List<ProductDTO> products = List.of(new ProductDTO("product", "Test Product", 1, PCategory.ELECTRONICS));
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchProductsByName(name)).thenReturn(new Response<>(products, "Success", true, null, null));
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByName(name, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).searchProductsByName(name);
    }
    
    @Test
    void testSearchProductsByName_InvalidToken() {
        String name = "Test";
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByName(name, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).searchProductsByName(anyString());
    }
    
    @Test
    void testSearchProductsByName_ExceptionHandling() {
        String name = "Test";
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchProductsByName(name)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProductsByName(name, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).searchProductsByName(name);
    }
    
    // --- getTopRatedProducts ---
    
    @Test
    void testGetTopRatedProducts_Success() {
        int limit = 5;
        String token = "valid-token";
        List<StoreProductDTO> products = List.of(new StoreProductDTO(1, "Top Product", 100.0, 10, 5.0, 1, PCategory.ELECTRONICS));
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getTopRatedProducts(limit)).thenReturn(new Response<>(products, "Success", true, null, null));
    
        ResponseEntity<Response<List<StoreProductDTO>>> response = productController.getTopRatedProducts(limit, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).getTopRatedProducts(limit);
    }
    
    @Test
    void testGetTopRatedProducts_BadResponse() {
        int limit = 5;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getTopRatedProducts(limit)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<List<StoreProductDTO>>> response = productController.getTopRatedProducts(limit, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getTopRatedProducts(limit);
    }
    
    @Test
    void testGetTopRatedProducts_InternalError() {
        int limit = 5;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getTopRatedProducts(limit)).thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));
    
        ResponseEntity<Response<List<StoreProductDTO>>> response = productController.getTopRatedProducts(limit, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getTopRatedProducts(limit);
    }
    
    @Test
    void testGetTopRatedProducts_InvalidToken() {
        int limit = 5;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<List<StoreProductDTO>>> response = productController.getTopRatedProducts(limit, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).getTopRatedProducts(anyInt());
    }
    
    @Test
    void testGetTopRatedProducts_ExceptionHandling() {
        int limit = 5;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getTopRatedProducts(limit)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<List<StoreProductDTO>>> response = productController.getTopRatedProducts(limit, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, times(1)).getTopRatedProducts(limit);
    }
    
    // --- getStoreProductRatings ---
    
    @Test
    void testGetStoreProductRatings_Success() {
        int storeId = 1;
        int prodId = 1;
        String token = "valid-token";
        List<ProductRatingDTO> ratings = List.of(new ProductRatingDTO(1, "comment", "email"));
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getStoreProductRatings(storeId, prodId)).thenReturn(new Response<>(ratings, "Success", true, null, null));
    
        ResponseEntity<Response<List<ProductRatingDTO>>> response = productController.getStoreProductRatings(storeId, prodId, token);
    
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(ratings, response.getBody().getData());
        verify(systemService, times(1)).getStoreProductRatings(storeId, prodId);
    }
    
    @Test
    void testGetStoreProductRatings_BadResponse() {
        int storeId = 1;
        int prodId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getStoreProductRatings(storeId, prodId)).thenReturn(new Response<>(null, "Bad request", false, ErrorType.BAD_REQUEST, null));
    
        ResponseEntity<Response<List<ProductRatingDTO>>> response = productController.getStoreProductRatings(storeId, prodId, token);
    
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).getStoreProductRatings(storeId, prodId);
    }
    
    @Test
    void testGetStoreProductRatings_InternalError() {
        int storeId = 1;
        int prodId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getStoreProductRatings(storeId, prodId)).thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));
    
        ResponseEntity<Response<List<ProductRatingDTO>>> response = productController.getStoreProductRatings(storeId, prodId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getStoreProductRatings(storeId, prodId);
    }
    
    @Test
    void testGetStoreProductRatings_InvalidToken() {
        int storeId = 1;
        int prodId = 1;
        String token = "invalid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(false);
    
        ResponseEntity<Response<List<ProductRatingDTO>>> response = productController.getStoreProductRatings(storeId, prodId, token);
    
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, never()).getStoreProductRatings(anyInt(), anyInt());
    }
    
    @Test
    void testGetStoreProductRatings_ExceptionHandling() {
        int storeId = 1;
        int prodId = 1;
        String token = "valid-token";
        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.getStoreProductRatings(storeId, prodId)).thenThrow(new RuntimeException("Unexpected error"));
    
        ResponseEntity<Response<List<ProductRatingDTO>>> response = productController.getStoreProductRatings(storeId, prodId, token);
    
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.UNAUTHORIZED, response.getBody().getErrorType());
        verify(systemService, times(1)).getStoreProductRatings(storeId, prodId);
    }
}