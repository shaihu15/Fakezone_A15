package ApplicationLayer.Services;

import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.StoreFounder;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public SystemService() {
        initialize();
    }

    public SystemService(IStoreRepository storeRepository, IUserRepository userRepository, IProductRepository productRepository) {
        this.storeService = new StoreService(storeRepository);
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter();
        this.paymentService = new PaymentAdapter();    }
    
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
            {
                this.storeService.addStoreRating(storeId, userId, rating, comment);
                logger.info("System Service - User rated store: " + storeId + " by user: " + userId + " with rating: " + rating);
            }
            else
            {
                logger.error("System Service - User did not purchase from this store: " + userId + " " + storeId);
                throw new IllegalArgumentException("User did not purchase from this store");
        }
        }
        catch (Exception e){
            logger.error("System Service - Error during rating store: " + e.getMessage());
            throw new IllegalArgumentException("Error during rating store: " + e.getMessage());
        }
    }

    @Override
    public void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try{
            if(this.userService.didPurchaseProduct(userId, storeId, productId))
            {
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
                logger.info("System Service - User rated product: " + productId + " in store: " + storeId + " by user: " + userId + " with rating: " + rating);
            }
            else{
                logger.error("System Service - User did not purchase from this product: " + userId + " " + storeId + " " + productId);
                throw new IllegalArgumentException("User did not purchase from this product");
            }
        }
        catch (Exception e){
            logger.error("System Service - Error during rating product: " + e.getMessage());
            throw new IllegalArgumentException("Error during rating product: " + e.getMessage());
        }
    }

    @Override
    public void openStore(int userId, String storeName) {
        try{
            if(this.userService.isUserLoggedIn(userId)){
                int storeId = this.storeService.openStore(userId, storeName);
                this.userService.addRole(userId, storeId, new StoreFounder());
                logger.info("System Service - User opened store: " + storeId + " by user: " + userId + " with name: " + storeName);
            }
            else
            {   logger.error("System Service - User is not logged in: " + userId); 
                throw new IllegalArgumentException("User is not logged in");
        }
        }
        catch (Exception e){
            logger.error("System Service - Error during opening store: " + e.getMessage());
            throw new IllegalArgumentException("Error during opening store: " + e.getMessage());
        }
    }
}
