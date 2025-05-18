package UnitTesting;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.fakezone.fakezone.controller.ProductController;

import ApplicationLayer.DTO.ProductDTO;
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
        when(systemService.getProduct(productId)).thenReturn(new Response<>(null, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<ProductDTO>> response = productController.getProdcut(productId, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).getProduct(productId);
    }

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
    void testGetProduct_ExceptionHandling() {
        int productId = 1;
        String token = "valid-token";

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
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("valid-token", productDTO);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds()))
                .thenReturn(new Response<>(true, null, true, null, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
    }

    @Test
    void testUpdateProduct_BadRequest() {
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("valid-token", productDTO);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds()))
                .thenReturn(new Response<>(false, "Invalid data", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
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
    void testUpdateProduct_ExceptionHandling() {
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("valid-token", productDTO);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
    }

    @Test
    void testDeleteProduct_Success() {
        int productId = 1;
        Request<Integer> request = new Request<>("valid-token", productId);
        String token = "valid-token";

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
        Request<Integer> request = new Request<>("valid-token", productId);
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(false, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).deleteProduct(productId);
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

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenReturn(new Response<>(products, null, true, null, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }

    @Test
    void testSearchProducts_BadRequest() {
        String keyword = "Test";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenReturn(new Response<>(null, "Invalid keyword", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }

    @Test
    void testSearchProducts_InvalidToken() {
        String keyword = "Test";
        String token = "invalid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(false);

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid token", response.getBody().getMessage());
        verify(systemService, never()).searchByKeyword(anyString());
    }

    @Test
    void testSearchProducts_ExceptionHandling() {
        String keyword = "Test";
        String token = "valid-token";

        when(authenticatorAdapter.isValid(token)).thenReturn(true);
        when(systemService.searchByKeyword(keyword)).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(500, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword);
    }
}