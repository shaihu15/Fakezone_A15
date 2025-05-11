package InfrastructureLayer.Adapters;

import DomainLayer.Interfaces.IDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryAdapter implements IDelivery {
    private final ExternalDeliverySystem externalSystem;
    private final Logger logger = LoggerFactory.getLogger(DeliveryAdapter.class);

    public DeliveryAdapter() {
        this.externalSystem = new ExternalDeliverySystem();
    }

    @Override
    public boolean deliver(String country, String address, String recipient, String packageDetails) {
        logger.info("Attempting delivery to "+country+" to " + recipient + " at " + address + ": " + packageDetails);
        if (country == null || address == null || recipient == null || packageDetails == null) {
            logger.error("Delivery failed due to missing information for " + recipient);
            return false;
        }
        boolean result = externalSystem.sendPackage(address, recipient, packageDetails);
        if (result) {
            logger.info("Delivery succeeded for " + recipient);
            return true;
        } else {
            logger.error("Delivery failed for " + recipient);
            return false;
        }
    }
}
// Mock external system and log for demonstration
class ExternalDeliverySystem {
    public boolean sendPackage(String address, String recipient, String packageDetails) {
        // Simulate always successful delivery
        return true;
    }
    public boolean cancelPackage(int deliveryId) {
        // Simulate always successful cancellation
        return true;
    }
}
