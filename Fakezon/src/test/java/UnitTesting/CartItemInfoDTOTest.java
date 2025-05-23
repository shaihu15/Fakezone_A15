package UnitTesting;

import com.fasterxml.jackson.databind.ObjectMapper;

import ApplicationLayer.DTO.CartItemInfoDTO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CartItemInfoDTOTest {

    @Test
    public void testAllArgsConstructorAndGetters() {
        CartItemInfoDTO dto = new CartItemInfoDTO(
                1, 2, "Store A", "Product B", 3, true, 19.99);

        assertEquals(1, dto.getStoreId());
        assertEquals(2, dto.getProductId());
        assertEquals("Store A", dto.getStoreName());
        assertEquals("Product B", dto.getProductName());
        assertEquals(3, dto.getQuantityInCart());
        assertTrue(dto.isInStock());
        assertEquals(19.99, dto.getUnitPrice());
    }

    @Test
    public void testSettersAndGetters() {
        CartItemInfoDTO dto = new CartItemInfoDTO();

        dto.setStoreId(10);
        dto.setProductId(20);
        dto.setStoreName("Test Store");
        dto.setProductName("Test Product");
        dto.setQuantityInCart(5);
        dto.setInStock(false);
        dto.setUnitPrice(5.5);

        assertEquals(10, dto.getStoreId());
        assertEquals(20, dto.getProductId());
        assertEquals("Test Store", dto.getStoreName());
        assertEquals("Test Product", dto.getProductName());
        assertEquals(5, dto.getQuantityInCart());
        assertFalse(dto.isInStock());
        assertEquals(5.5, dto.getUnitPrice());
    }

    @Test
    public void testJacksonDeserialization() throws Exception {
        String json = """
                {
                    "storeId": 1,
                    "productId": 2,
                    "storeName": "Store A",
                    "productName": "Product B",
                    "quantityInCart": 3,
                    "inStock": true,
                    "unitPrice": 19.99
                }
                """;

        ObjectMapper mapper = new ObjectMapper();
        CartItemInfoDTO dto = mapper.readValue(json, CartItemInfoDTO.class);

        assertEquals(1, dto.getStoreId());
        assertEquals(2, dto.getProductId());
        assertEquals("Store A", dto.getStoreName());
        assertEquals("Product B", dto.getProductName());
        assertEquals(3, dto.getQuantityInCart());
        assertTrue(dto.isInStock());
        assertEquals(19.99, dto.getUnitPrice());
    }
}
