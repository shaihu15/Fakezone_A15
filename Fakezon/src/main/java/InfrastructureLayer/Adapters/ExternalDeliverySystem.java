package InfrastructureLayer.Adapters;

public class ExternalDeliverySystem {
    public boolean sendPackage(String address, String recipient, String packageDetails) {
        // Simulate always successful delivery
        return true;
    }
    public boolean cancelPackage(int deliveryId) {
        // Simulate always successful cancellation
        return true;
    }
}
