package UnitTesting;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Model.StoreProduct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreProductDTOTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        StoreProductDTO dto = new StoreProductDTO(1, "TestProduct", 99.99, 5, 4.5, 10, PCategory.ELECTRONICS);

        assertEquals(1, dto.getProductId());
        assertEquals("TestProduct", dto.getName());
        assertEquals(99.99, dto.getBasePrice());
        assertEquals(5, dto.getQuantity());
        assertEquals(4.5, dto.getAverageRating());
        assertEquals(10, dto.getStoreId());
        assertEquals(PCategory.ELECTRONICS, dto.getCategory());
    }

    @Test
    void testNoArgsConstructor() {
        StoreProductDTO dto = new StoreProductDTO();
        assertEquals(0, dto.getProductId());
        assertNull(dto.getName());
        assertEquals(0.0, dto.getBasePrice());
        assertEquals(0, dto.getQuantity());
        assertEquals(0.0, dto.getAverageRating());
        assertEquals(0, dto.getStoreId());
        assertNull(dto.getCategory());
    }

    @Test
    void testStoreProductConstructor() {
        StoreProduct mockStoreProduct = mock(StoreProduct.class);
        when(mockStoreProduct.getSproductID()).thenReturn(2);
        when(mockStoreProduct.getStoreId()).thenReturn(20);
        when(mockStoreProduct.getName()).thenReturn("MockProduct");
        when(mockStoreProduct.getBasePrice()).thenReturn(55.5);
        when(mockStoreProduct.getQuantity()).thenReturn(7);
        when(mockStoreProduct.getAverageRating()).thenReturn(3.7);
        when(mockStoreProduct.getCategory()).thenReturn(PCategory.AUTOMOTIVE);

        StoreProductDTO dto = new StoreProductDTO(mockStoreProduct);

        assertEquals(2, dto.getProductId());
        assertEquals("MockProduct", dto.getName());
        assertEquals(55.5, dto.getBasePrice());
        assertEquals(7, dto.getQuantity());
        assertEquals(3.7, dto.getAverageRating());
        assertEquals(20, dto.getStoreId());
        assertEquals(PCategory.AUTOMOTIVE, dto.getCategory());
    }

    @Test
    void testStoreProductAndQuantityConstructor() {
        StoreProduct mockStoreProduct = mock(StoreProduct.class);
        when(mockStoreProduct.getSproductID()).thenReturn(3);
        when(mockStoreProduct.getStoreId()).thenReturn(30);
        when(mockStoreProduct.getName()).thenReturn("AnotherProduct");
        when(mockStoreProduct.getBasePrice()).thenReturn(12.3);
        when(mockStoreProduct.getAverageRating()).thenReturn(2.2);
        when(mockStoreProduct.getCategory()).thenReturn(PCategory.AUTOMOTIVE);

        StoreProductDTO dto = new StoreProductDTO(mockStoreProduct, 99);

        assertEquals(3, dto.getProductId());
        assertEquals("AnotherProduct", dto.getName());
        assertEquals(12.3, dto.getBasePrice());
        assertEquals(99, dto.getQuantity());
        assertEquals(2.2, dto.getAverageRating());
        assertEquals(30, dto.getStoreId());
        assertEquals(PCategory.AUTOMOTIVE, dto.getCategory());
    }
}