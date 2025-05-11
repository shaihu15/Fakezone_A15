package UnitTesting;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.ExternalDeliverySystem;

class DeliveryAdapterTest {


    private ExternalDeliverySystem externalDeliverySystem;
    private  DeliveryAdapter deliveryAdapter; // replace with your actual class name

    private final String country = "USA";
    private final String address = "123 Main St";
    private final String recipient = "John Doe";
    private final String packageDetails = "Electronics Package";

    @BeforeEach
    void setUp() {
        // Initialize mocks
        externalDeliverySystem = new ExternalDeliverySystem();
        deliveryAdapter = new DeliveryAdapter(externalDeliverySystem);
    }

    @Test
    void testDeliver_Success() {

        boolean result = deliveryAdapter.deliver(country, address, recipient, packageDetails);
        assertTrue(result);
    }

    @Test
    void cancelPackage_Success() {
        int deliveryId = 12345;
        boolean result = externalDeliverySystem.cancelPackage(deliveryId);
        assertTrue(result);
    }
}