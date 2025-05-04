package UnitTesting;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        productService.updateProduct(1, "UpdatedProduct", "UpdatedDescription", new HashSet<>());
        verify(productRepository, times(1)).updateProduct(1, "UpdatedProduct", "UpdatedDescription", new HashSet<>());
    }

    @Test
    void givenNonExistingProduct_WhenUpdateProduct_ThenThrowsException() {
        doThrow(new IllegalArgumentException("Product not found")).when(productRepository).updateProduct(1, "UpdatedProduct", "UpdatedDescription", new HashSet<>());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(1, "UpdatedProduct", "UpdatedDescription", new HashSet<>());
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

    @Test
    void givenValidStoreIdAndProductIds_WhenAddProductsToStore_ThenProductsAreUpdated() {
        // Arrange
        int storeId = 101;
        Set<Integer> productIds = new HashSet<>(Arrays.asList(1, 2));
        IProduct mockProduct1 = mock(IProduct.class);
        IProduct mockProduct2 = mock(IProduct.class);

        // Mock product repository behavior
        when(productRepository.getProductById(1)).thenReturn(mockProduct1);
        when(productRepository.getProductById(2)).thenReturn(mockProduct2);

        // Mock product behavior
        when(mockProduct1.getId()).thenReturn(1);
        when(mockProduct1.getName()).thenReturn("UpdatedProduct1");
        when(mockProduct1.getDescription()).thenReturn("UpdatedDescription1");
        when(mockProduct2.getId()).thenReturn(2);
        when(mockProduct2.getName()).thenReturn("UpdatedProduct2");
        when(mockProduct2.getDescription()).thenReturn("UpdatedDescription2");

        // Act
        productService.addProductsToStore(storeId, productIds);

        // Assert
        verify(mockProduct1, times(1)).addStore(storeId);
        verify(mockProduct2, times(1)).addStore(storeId);
        verify(productRepository, times(1)).updateProduct(1, "UpdatedProduct1", "UpdatedDescription1", new HashSet<>());
        verify(productRepository, times(1)).updateProduct(2, "UpdatedProduct2", "UpdatedDescription2", new HashSet<>());
    }

    @Test
    void givenInvalidProductId_WhenAddProductsToStore_ThenThrowsException() {
        // Arrange
        int storeId = 101;
        Set<Integer> productIds = new HashSet<>(Arrays.asList(1, 2));
        when(productRepository.getProductById(1)).thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.addProductsToStore(storeId, productIds);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).getProductById(1);
        verify(productRepository, never()).updateProduct(1, "UpdatedProduct", "UpdatedDescription", new HashSet<>());
    }
}