package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Order;
import DomainLayer.Model.Basket;
import ApplicationLayer.DTO.StoreProductDTO;

public class OrderTest {
    private Order order;

    @BeforeEach
    void setUp() {
        Basket basket = new Basket(1, Arrays.asList(
                new StoreProductDTO(1, "Product1", 10.0, 5, 4.5, 1),
                new StoreProductDTO(2, "Product2", 15.0, 3, 4.0, 1),
                new StoreProductDTO(3, "Product3", 20.0, 2, 3.5, 1)
        ));
        order = new Order(1, 101, OrderState.PENDING, basket, "123 Main St", PaymentMethod.CREDIT_CARD);
    }

    @Test
    void givenValidOrder_WhenGetId_ThenReturnsCorrectId() {
        assertEquals(1, order.getId());
    }

    @Test
    void givenValidOrder_WhenGetUserId_ThenReturnsCorrectUserId() {
        assertEquals(101, order.getUserId());
    }

    @Test
    void givenValidOrder_WhenGetStoreId_ThenReturnsCorrectStoreId() {
        assertEquals(1, order.getStoreId());
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
        Basket basket = new Basket(1, Arrays.asList(
                new StoreProductDTO(1, "Product1", 10.0, 0, 0.0, 201),
                new StoreProductDTO(2, "Product2", 15.0, 0, 0.0, 201),
                new StoreProductDTO(3, "Product3", 20.0, 0, 0.0, 201)
        ));
        order = new Order(1, 101, OrderState.PENDING, basket, "123 Main St", PaymentMethod.CASH_ON_DELIVERY);        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, order.getPaymentMethod());
    }

    @Test
    void givenNonCashOnDeliveryOrder_WhenSetPaymentMethod_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        });
        assertEquals("Payment method can only be changed if it is CASH_ON_DELIVERY.", exception.getMessage());}
}