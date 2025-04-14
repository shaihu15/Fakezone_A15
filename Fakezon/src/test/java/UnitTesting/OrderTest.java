package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import DomainLayer.Enums.OrderState;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Model.Order;

public class OrderTest {
    private Order order;

    @BeforeEach
    void setUp() {
        Collection<Integer> productIds = Arrays.asList(1, 2, 3);
        order = new Order(1, 101, OrderState.PENDING, productIds, 201, "123 Main St", PaymentMethod.CREDIT_CARD);
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
        assertEquals(201, order.getStoreId());
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
        order = new Order(1, 101, OrderState.PENDING, Arrays.asList(1, 2, 3), 201, "123 Main St", PaymentMethod.CASH_ON_DELIVERY);
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, order.getPaymentMethod());
    }

    @Test
    void givenNonCashOnDeliveryOrder_WhenSetPaymentMethod_ThenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            order.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        });
        assertEquals("Can only change payment method for \"cash on delevery\".", exception.getMessage());
    }
}