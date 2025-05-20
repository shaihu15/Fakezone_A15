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
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(true);

        boolean result = adapter.pay("1234567890123456", "John Doe", "12/25", "123", 100.0);

        assertTrue(result);
        verify(mockExternal, times(1)).processPayment("1234567890123456", "John Doe", "12/25", "123", 100.0);
    }

    @Test
    void givenInValidCardNumberPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        boolean result = adapter.pay(null, "John Doe", "12/25", "123", 100.0);

        assertFalse(result);
    }
    @Test
    void givenInValidcardHolderPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        boolean result = adapter.pay("1234567890123456", null, "12/25", "123", 100.0);

        assertFalse(result);
    }
    @Test
    void givenInValidexpDatePaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        boolean result = adapter.pay("1234567890123456", "John Doe", null, "123", 100.0);

        assertFalse(result);
    }
    @Test
    void givenInValidcvvPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        boolean result = adapter.pay("1234567890123456", "John Doe", "12/25", null, 100.0);

        assertFalse(result);
    }
    @Test
    void givenValidPaymentDetails_WhenPayFails_ThenReturnsFalse() {
        when(mockExternal.processPayment(anyString(), anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(false);

        boolean result = adapter.pay("1234567890123456", "John Doe", "12/25", "123", 100.0);

        assertFalse(result);
        verify(mockExternal, times(1)).processPayment("1234567890123456", "John Doe", "12/25", "123", 100.0);
    }

    @Test
    void givenValidRefundDetails_WhenRefundSucceeds_ThenReturnsTrue() {
        when(mockExternal.processRefund("1234567890123456", 50.0)).thenReturn(true);

        boolean result = adapter.refund("1234567890123456", 50.0);

        assertTrue(result);
        verify(mockExternal, times(1)).processRefund("1234567890123456", 50.0);
    }

    @Test
    void givenValidRefundDetails_WhenRefundFails_ThenReturnsFalse() {
        when(mockExternal.processRefund("1234567890123456", 50.0)).thenReturn(false);

        boolean result = adapter.refund("1234567890123456", 50.0);

        assertFalse(result);
        verify(mockExternal, times(1)).processRefund("1234567890123456", 50.0);
    }
}