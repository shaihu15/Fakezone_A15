package InfrastructureLayer.Adapters;

import DomainLayer.Interfaces.IDelivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import InfrastructureLayer.Adapters.ExternalDeliverySystem;
@Component

public class DeliveryAdapter implements IDelivery {
    private final ExternalDeliverySystem externalSystem;
    private final Logger logger = LoggerFactory.getLogger(DeliveryAdapter.class);

    
    public DeliveryAdapter() {
        this.externalSystem = new ExternalDeliverySystem();
    }
    public DeliveryAdapter(ExternalDeliverySystem externalSystem) {
        this.externalSystem = externalSystem;
    }
    @Override
    public int deliver(String country, String address, String recipient, String packageDetails) {
        logger.info("Attempting delivery to "+country+" to " + recipient + " at " + address + ": " + packageDetails);
        if (country == null || address == null || recipient == null || packageDetails == null) {
            logger.error("Delivery failed due to missing information for " + recipient);
            return -1;
        }
        int transactionId  = externalSystem.sendPackage(address, recipient, packageDetails);
        if (transactionId != -1) {
            logger.info("Delivery succeeded for " + recipient+ " with transaction ID: " + transactionId);
            return transactionId; 
        } else {
            logger.error("Delivery failed for " + recipient + "External system returned -1 or invalid transaction ID");
            return -1;
        }
    }
}

