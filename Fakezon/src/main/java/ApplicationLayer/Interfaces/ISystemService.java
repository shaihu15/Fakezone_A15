package ApplicationLayer.Interfaces;

import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;

public interface ISystemService {
    // Access to core services
    IDelivery getDeliveryService();
    IAuthenticator getAuthenticatorService();
    IPayment getPaymentService();

    void initialize();
    void shutdown();
    Void GuestLogin(String userName, String Password);
}