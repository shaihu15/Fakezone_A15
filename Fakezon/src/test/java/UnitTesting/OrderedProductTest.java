package UnitTesting;

import ApplicationLayer.DTO.StoreProductDTO;
import org.junit.jupiter.api.Test;
import DomainLayer.Model.OrderedProduct;
import ApplicationLayer.Enums.PCategory; 

import static org.junit.jupiter.api.Assertions.*;

class OrderedProductTest {

    @Test
    void testConstructorAndGetters() {
        int productId = 10;
        String name = "TestProduct";
        double price = 99.99;
        int quantity = 3;

        OrderedProduct orderedProduct = new OrderedProduct(productId, name, price, quantity);

        assertEquals(productId, orderedProduct.getProductId());
        assertEquals(name, orderedProduct.getName());
        assertEquals(price, orderedProduct.getPrice());
        assertEquals(quantity, orderedProduct.getQuantity());
    }

    @Test
    void testConstructorWithStoreProductDTO() {
        int productId = 20;
        String name = "DTOProduct";
        double basePrice = 55.5;
        int quantity = 2;
        int storeId = 1;
        int storeProductId = productId;
        double avgRating = 4.0;
        PCategory category = PCategory.ELECTRONICS;

        StoreProductDTO dto = new StoreProductDTO(storeProductId, name, basePrice, quantity, avgRating, storeId, category);

        OrderedProduct orderedProduct = new OrderedProduct(dto, quantity);

        assertEquals(productId, orderedProduct.getProductId());
        assertEquals(name, orderedProduct.getName());
        assertEquals(basePrice, orderedProduct.getPrice());
        assertEquals(quantity, orderedProduct.getQuantity());
    }
}