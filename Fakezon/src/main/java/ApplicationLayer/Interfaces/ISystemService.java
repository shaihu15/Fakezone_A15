package ApplicationLayer.Interfaces;


import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;

import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;

public interface ISystemService {
    // Access to core services
    IDelivery getDeliveryService();

    IAuthenticator getAuthenticatorService();

    IPayment getPaymentService();

    StoreDTO userAccessStore(int userId, int storeId);

    void ratingStore(int storeId, int userId, double rating, String comment);

    void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment);

    void openStore(int userId, String storeName);

    StoreProductDTO getProductFromStore(int productId, int storeId);

    void initialize();

    void shutdown();
}