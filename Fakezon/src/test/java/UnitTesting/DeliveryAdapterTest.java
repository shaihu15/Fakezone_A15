package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.ExternalDeliverySystem;
import InfrastructureLayer.Adapters.ExternalPaymentSystem;

class DeliveryAdapterTest {


    private ExternalDeliverySystem externalDeliverySystem;
    private  DeliveryAdapter deliveryAdapter; // replace with your actual class name
    private ExternalDeliverySystem mockExternal;

    private final String country = "USA";
    private final String address = "123 Main St";
    private final String recipient = "John Doe";
    private final String packageDetails = "Electronics Package";

    @BeforeEach
    void setUp() {
        // Initialize mocks
        externalDeliverySystem = new ExternalDeliverySystem();
        deliveryAdapter = new DeliveryAdapter(externalDeliverySystem);
        mockExternal = mock(ExternalDeliverySystem.class);
    }

    @Test
    void testDeliver_Success() {

        boolean result = deliveryAdapter.deliver(country, address, recipient, packageDetails);
        assertTrue(result);
    }
    @Test
    void givenInValidcountryDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        boolean result = deliveryAdapter.deliver(null, address, recipient, packageDetails);
        assertFalse(result);
    }
        @Test
    void givenInValidaddressDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        boolean result = deliveryAdapter.deliver(country, null, recipient, packageDetails);
        assertFalse(result);
    }
        @Test
    void givenInValidrecipientDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        boolean result = deliveryAdapter.deliver(country, address, null, packageDetails);
        assertFalse(result);
    }
        @Test
    void givenInValidpackageDetailsDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        boolean result = deliveryAdapter.deliver(country, address, recipient, null);
        assertFalse(result);
    }
            @Test
    void givenValidpackageDetailsDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        when(mockExternal.sendPackage(anyString(), anyString(), anyString())).thenReturn(false);
        DeliveryAdapter newdeliveryAdapter = new DeliveryAdapter(mockExternal);
        boolean result = newdeliveryAdapter.deliver(country, address, recipient, packageDetails);
        assertFalse(result);
    }
    
}