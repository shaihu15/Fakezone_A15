package UnitTesting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.ExternalDeliverySystem;

class DeliveryAdapterTest {


    private ExternalDeliverySystem externalDeliverySystem;
    private  DeliveryAdapter deliveryAdapter; // replace with your actual class name
    private ExternalDeliverySystem mockExternal;

    private final String country = "USA";
    private final String address = "123 Main St*New York*USA*12345";
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
        ExternalDeliverySystem mockExternal = mock(ExternalDeliverySystem.class);
        DeliveryAdapter adapter = new DeliveryAdapter(mockExternal);
        when(mockExternal.sendPackage(anyString(), anyString(), anyString())).thenReturn(12345);
        String validAddress = "123 Main St*New York*USA*12345";
        int result = adapter.deliver("USA", validAddress, recipient, packageDetails);
        assertEquals(12345, result);
    }
    @Test
    void givenInValidcountryDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        int result = deliveryAdapter.deliver(null, address, recipient, packageDetails);
        assertNotEquals(1, result);
    }
    @Test
    void givenInValidaddressDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        int result = deliveryAdapter.deliver(country, null, recipient, packageDetails);
        assertNotEquals(1, result);
    }
    @Test
    void givenInValidrecipientDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        int result = deliveryAdapter.deliver(country, address, null, packageDetails);
        assertNotEquals(1, result);
    }
    @Test
    void givenInValidpackageDetailsDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        int result = deliveryAdapter.deliver(country, address, recipient, null);
        assertNotEquals(1, result);
    }
    @Test
    void givenValidpackageDetailsDeliveryDetails_WhenPDeliverFails_ThenReturnsFalse() {
        when(mockExternal.sendPackage(anyString(), anyString(), anyString())).thenReturn(0);
        DeliveryAdapter newdeliveryAdapter = new DeliveryAdapter(mockExternal);
        int result = newdeliveryAdapter.deliver(country, address, recipient, packageDetails);
        assertNotEquals(1, result);
    }
    
}