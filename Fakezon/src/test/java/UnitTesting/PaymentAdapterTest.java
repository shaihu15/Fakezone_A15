package UnitTesting;

import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Adapters.ExternalPaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PaymentAdapterTest {

    private ExternalPaymentSystem mockExternal;
    private PaymentAdapter adapter;

    @BeforeEach
    void setUp() {
        mockExternal = mock(ExternalPaymentSystem.class);
        adapter = new PaymentAdapter(mockExternal);
    }
    

    @Test
    void givenValidPaymentDetails_WhenPayIsCalled_ThenReturnsTrueOnSuccess() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(12345);

        int result = adapter.pay("1234567890123456", "John Doe", "12/25", "123", 100.0, 1);

        assertEquals(12345, result);
        verify(mockExternal, times(1)).processPayment("1234567890123456", "John Doe", "12/25", "123", 100.0, 1);
    }

    @Test
    void givenInValidCardNumberPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(-1);

        int result = adapter.pay(null, "John Doe", "12/25", "123", 100.0, 1);

        assertEquals(-1, result);
    }
    @Test
    void givenInValidcardHolderPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(-1);

        int result = adapter.pay("1234567890123456", null, "12/25", "123", 100.0, 1);

        assertEquals(-1, result);
    }
    @Test
    void givenInValidexpDatePaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(-1);

        int result = adapter.pay("1234567890123456", "John Doe", null, "123", 100.0, 1);

        assertEquals(-1, result);
    }
    @Test
    void givenInValidcvvPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(-1);

        int result = adapter.pay("1234567890123456", "John Doe", "12/25", null, 100.0, 1);

        assertEquals(-1, result);
    }
    @Test
    void givenValidPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble(), anyInt()))
                .thenReturn(-1);

        int result = adapter.pay("1234567890123456", "John Doe", "12/25", "123", 100.0, 1);

        assertEquals(-1, result);
        verify(mockExternal, times(1)).processPayment("1234567890123456", "John Doe", "12/25", "123", 100.0, 1);
    }

    @Test
    void givenValidRefundDetails_WhenRefundSucceeds_ThenReturnsTrue() {
        when(mockExternal.processRefund(12345)).thenReturn(1);

        int result = adapter.refund(12345);

        assertEquals(1, result);
        verify(mockExternal, times(1)).processRefund(12345);
    }

    @Test
    void givenValidRefundDetails_WhenRefundFails_ThenReturnsFalse() {
        when(mockExternal.processRefund(12345)).thenReturn(-1);

        int result = adapter.refund(12345);

        assertEquals(-1, result);
        verify(mockExternal, times(1)).processRefund(12345);
    }
}