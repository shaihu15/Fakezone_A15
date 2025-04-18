package ApplicationLayer.Services;

import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;

public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;

    public SystemService() {
        initialize();
    }

    @Override
    public IDelivery getDeliveryService() {
        return deliveryService;
    }

    @Override
    public IAuthenticator getAuthenticatorService() {
        return authenticatorService;
    }

    @Override
    public IPayment getPaymentService() {
        return paymentService;
    }

    @Override
    public void initialize() {
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter();
        this.paymentService = new PaymentAdapter();
        this.userService = new UserService(new UserRepository());
        this.storeService = new StoreService(new StoreRepository());
        this.productService = new ProductService(new ProductRepository());
    }

    @Override
    public void shutdown() {
        // Clean up resources if needed
        this.deliveryService = null;
        this.authenticatorService = null;
        this.paymentService = null;
    }

    @Override
    public void ratingStore(int storeId, int userId, double rating, String comment) {
        try{
            if(this.userService.didPurchaseStore(userId, storeId))
                this.storeService.addStoreRating(storeId, userId, rating, comment);
            else
                throw new IllegalArgumentException("User did not purchase from this store");
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error during rating store: " + e.getMessage());
        }
    }

    @Override
    public void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try{
            if(this.userService.didPurchaseProduct(userId, storeId, productId))
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
            else
                throw new IllegalArgumentException("User did not purchase from this product");
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error during rating product: " + e.getMessage());
        }
    }
}
