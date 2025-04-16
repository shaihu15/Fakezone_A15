package UnitTesting;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import ApplicationLayer.DTO.ProductDTO;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.Interfaces.IProduct;
import ApplicationLayer.Services.ProductService;

public class ProductServiceTest {
    private ProductService productService;
    private IProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = mock(IProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void givenValidProductDetails_WhenAddProduct_ThenProductIsAdded() {
        productService.addProduct("Product1", "Description1");
        verify(productRepository, times(1)).addProduct(any(IProduct.class));
    }

    @Test
    void givenValidProductDetails_WhenUpdateProduct_ThenProductIsUpdated() {
        productService.updateProduct(1, "UpdatedProduct", "UpdatedDescription");
        verify(productRepository, times(1)).updateProduct(any(IProduct.class));
    }

    @Test
    void givenNonExistingProduct_WhenUpdateProduct_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Product not found")).when(productRepository).updateProduct(any(IProduct.class));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(1, "UpdatedProduct", "UpdatedDescription");
        });
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void givenValidProductId_WhenDeleteProduct_ThenProductIsDeleted() {
        productService.deleteProduct(1);
        verify(productRepository, times(1)).deleteProduct(1);
    }

    @Test
    void givenNonExistingProduct_WhenDeleteProduct_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Product not found")).when(productRepository).deleteProduct(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(1);
        });
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void givenValidProductId_WhenViewProduct_ThenReturnsProductDTO() {
        IProduct mockProduct = mock(IProduct.class);
        when(mockProduct.getName()).thenReturn("Product1");
        when(mockProduct.getDescription()).thenReturn("Description1");
        when(productRepository.getProductById(1)).thenReturn(mockProduct);

        ProductDTO productDTO = productService.viewProduct(1);

        assertEquals("Product1", productDTO.getName());
        assertEquals("Description1", productDTO.getDescription());
    }

    @Test
    void givenNonExistingProduct_WhenViewProduct_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Product not found")).when(productRepository).getProductById(1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.viewProduct(1);
        });
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void givenKeyword_WhenSearchProducts_ThenReturnsMatchingProducts() {
        IProduct mockProduct1 = mock(IProduct.class);
        IProduct mockProduct2 = mock(IProduct.class);

        when(mockProduct1.getName()).thenReturn("Product1");
        when(mockProduct1.getDescription()).thenReturn("Description1");
        when(mockProduct2.getName()).thenReturn("Product2");
        when(mockProduct2.getDescription()).thenReturn("Description2");

        when(productRepository.searchProducts("keyword")).thenReturn(Arrays.asList(mockProduct1, mockProduct2));

        List<ProductDTO> productDTOs = productService.searchProducts("keyword");

        assertEquals(2, productDTOs.size());
        assertEquals("Product1", productDTOs.get(0).getName());
        assertEquals("Description1", productDTOs.get(0).getDescription());
        assertEquals("Product2", productDTOs.get(1).getName());
        assertEquals("Description2", productDTOs.get(1).getDescription());
    }

    @Test
    void givenKeyword_WhenSearchProducts_ThenThrowsException() {
        doThrow(new RuntimeException("Search failed")).when(productRepository).searchProducts("keyword");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.searchProducts("keyword");
        });
        assertEquals("Search failed", exception.getMessage());
    }
}