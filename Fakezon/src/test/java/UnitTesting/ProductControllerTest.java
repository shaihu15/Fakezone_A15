package UnitTesting;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Request;
import ApplicationLayer.Response;
import com.fakezone.fakezone.controller.ProductController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ISystemService systemService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productController = new ProductController(systemService);
    }

    @Test
    void testGetProduct_Success() {
        int productId = 1;
        String token = "valid-token";
        ProductDTO productDTO = new ProductDTO("product name", "Test Product", productId, PCategory.ELECTRONICS);

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

        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds()))
                .thenReturn(new Response<>(true, null, true, null, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
    }

    @Test
    void testUpdateProduct_BadRequest() {
        ProductDTO productDTO = new ProductDTO("product", "Updated Product", 1, PCategory.ELECTRONICS);
        Request<ProductDTO> request = new Request<>("valid-token", productDTO);

        when(systemService.updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds()))
                .thenReturn(new Response<>(false, "Invalid data", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<Boolean>> response = productController.updateProduct(request);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).updateProduct(productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getStoresIds());
    }

    @Test
    void testDeleteProduct_Success() {
        int productId = 1;
        Request<Integer> request = new Request<>( "valid-token", productId);

        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(true, null, true, null, null));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData());
        verify(systemService, times(1)).deleteProduct(productId);
    }

    @Test
    void testDeleteProduct_InternalError() {
        int productId = 1;
        Request<Integer> request = new Request<>( "valid-token",productId);

        when(systemService.deleteProduct(productId)).thenReturn(new Response<>(false, "Internal error", false, ErrorType.INTERNAL_ERROR, null));

        ResponseEntity<Response<Boolean>> response = productController.deleteProduct(request);

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

        when(systemService.searchByKeyword(keyword, token)).thenReturn(new Response<>(products, null, true, null, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(products, response.getBody().getData());
        verify(systemService, times(1)).searchByKeyword(keyword, token);
    }

    @Test
    void testSearchProducts_BadRequest() {
        String keyword = "Test";
        String token = "valid-token";

        when(systemService.searchByKeyword(keyword, token)).thenReturn(new Response<>(null, "Invalid keyword", false, ErrorType.BAD_REQUEST, null));

        ResponseEntity<Response<List<ProductDTO>>> response = productController.searchProducts(keyword, token);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals(ErrorType.BAD_REQUEST, response.getBody().getErrorType());
        verify(systemService, times(1)).searchByKeyword(keyword, token);
    }
}