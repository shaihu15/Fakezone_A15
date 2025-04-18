package ApplicationLayer.Interfaces;

import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IPayment;

public interface ISystemService {
    // Access to core services
    IDelivery getDeliveryService();
    IAuthenticator getAuthenticatorService();
    IPayment getPaymentService();

    void ratingStore(int storeId, int userId, double rating, String comment);
    void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment);
    void initialize();
    void shutdown();
}