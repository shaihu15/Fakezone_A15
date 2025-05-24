package UnitTesting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Enums.PCategory;
import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Order;
import DomainLayer.Model.OrderedProduct;

public class OrderTest {
    private Order order;
    private StoreProductDTO product1;
    private StoreProductDTO product2;
    private StoreProductDTO product3;
    private int storeId;
    private int orderId = 1;
    private int userId = 5;

    @BeforeEach
    void setUp() {
        product1 = new StoreProductDTO(1, "Product1", 10.0, 5, 4.5, 1, PCategory.FASHION);
        product2 = new StoreProductDTO(2, "Product2", 15.0, 3, 4.0, 1, PCategory.FASHION);
        product3 = new StoreProductDTO(3, "Product3", 20.0, 2, 3.5, 1, PCategory.FASHION);
        storeId = 1;
        Basket basket = new Basket(1, Map.of(
                product1.getProductId(), 2,
                product2.getProductId(), 1,
                product3.getProductId(), 1
        ));
        List<StoreProductDTO> products1 = Arrays.asList(product1, product2, product3);
        List<OrderedProduct> orderedProducts1 = products1.stream().map(product -> new OrderedProduct(product, product.getQuantity())).collect(Collectors.toList());
        double totalPrice1 = products1.stream().mapToDouble(product -> product.getBasePrice() * product.getQuantity()).sum();


        order = new Order(orderId,userId,storeId, OrderState.PENDING, orderedProducts1, "123 Main St", PaymentMethod.CREDIT_CARD, totalPrice1);
    }

    @Test
    void givenValidOrder_WhenGetId_ThenReturnsCorrectId() {
        assertEquals(orderId, order.getId());
    }

    @Test
    void givenValidOrder_WhenGetUserId_ThenReturnsCorrectUserId() {
        assertEquals(userId, order.getUserId());
    }

    @Test
    void givenValidOrder_WhenGetStoreId_ThenReturnsCorrectStoreId() {
        assertEquals(storeId, order.getStoreId());
    }

    @Test
    void givenValidOrder_WhenGetState_ThenReturnsCorrectState() {
        assertEquals(OrderState.PENDING, order.getState());
    }

    @Test
    void givenValidOrder_WhenSetState_ThenStateIsUpdated() {
        order.setState(OrderState.SHIPPED);
        assertEquals(OrderState.SHIPPED, order.getState());
    }

    @Test
    void givenValidOrder_WhenGetProductIds_ThenReturnsCorrectProductIds() {
        Collection<Integer> expectedProductIds = Arrays.asList(1, 2, 3);
        assertEquals(expectedProductIds, order.getProductIds());
    }

    @Test
    void givenValidOrder_WhenGetAddress_ThenReturnsCorrectAddress() {
        assertEquals("123 Main St", order.getAddress());
    }

    @Test
    void givenValidOrder_WhenSetAddress_ThenAddressIsUpdated() {
        order.setAddress("456 Elm St");
        assertEquals("456 Elm St", order.getAddress());
    }

    @Test
    void givenValidOrder_WhenGetPaymentMethod_ThenReturnsCorrectPaymentMethod() {
        assertEquals(PaymentMethod.CREDIT_CARD, order.getPaymentMethod());
    }

    @Test
    void givenCashOnDeliveryOrder_WhenSetPaymentMethod_ThenPaymentMethodIsUpdated() {


        List<StoreProductDTO> products1 = Arrays.asList(product1, product2, product3);
        List<OrderedProduct> orderedProducts1 = products1.stream().map(product -> new OrderedProduct(product, product.getQuantity())).collect(Collectors.toList());
        order = new Order(1,storeId, 101, OrderState.PENDING, orderedProducts1, "123 Main St", PaymentMethod.CASH_ON_DELIVERY, 10);        
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, order.getPaymentMethod());
    }

    @Test
    void givenNonCashOnDeliveryOrder_WhenSetPaymentMethod_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        });
        assertEquals("Payment method can only be changed if it is CASH_ON_DELIVERY.", exception.getMessage());}
    @Test
    void testOrderConstructorAndGetProductsAndGetTotalPrice() {
        List<OrderedProduct> orderedProducts = Arrays.asList(
                new OrderedProduct(product1, 2),
                new OrderedProduct(product2, 1)
        );
        double expectedTotal = product1.getBasePrice() * 2 + product2.getBasePrice() * 1;
        Order customOrder = new Order(99, 88, 77, OrderState.PENDING, orderedProducts, "Test Address", PaymentMethod.CREDIT_CARD, expectedTotal);

        // Test getProducts
        List<OrderedProduct> productsFromOrder = customOrder.getProducts();
        assertEquals(2, productsFromOrder.size());
        assertEquals(product1.getProductId(), productsFromOrder.get(0).getProductId());

        // Test getTotalPrice
        assertEquals(expectedTotal, customOrder.getTotalPrice());
    }
}