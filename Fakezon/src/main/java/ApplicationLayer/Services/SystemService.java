package ApplicationLayer.Services;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;

import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.StoreFounder;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
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
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public SystemService() {
        initialize();
    }

    public SystemService(IStoreRepository storeRepository, IUserRepository userRepository,
            IProductRepository productRepository) {
        this.storeService = new StoreService(storeRepository);
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter();
        this.paymentService = new PaymentAdapter();
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
        try {
            if (this.userService.didPurchaseStore(userId, storeId)) {
                this.storeService.addStoreRating(storeId, userId, rating, comment);
                logger.info("System Service - User rated store: " + storeId + " by user: " + userId + " with rating: "
                        + rating);
            } else {
                logger.error("System Service - User did not purchase from this store: " + userId + " " + storeId);
                throw new IllegalArgumentException("User did not purchase from this store");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating store: " + e.getMessage());
            throw new IllegalArgumentException("Error during rating store: " + e.getMessage());
        }
    }

    @Override
    public void ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try {
            if (this.userService.didPurchaseProduct(userId, storeId, productId)) {
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
                logger.info("System Service - User rated product: " + productId + " in store: " + storeId + " by user: "
                        + userId + " with rating: " + rating);
            } else {
                logger.error("System Service - User did not purchase from this product: " + userId + " " + storeId + " "
                        + productId);
                throw new IllegalArgumentException("User did not purchase from this product");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating product: " + e.getMessage());
            throw new IllegalArgumentException("Error during rating product: " + e.getMessage());
        }
    }

    public StoreDTO userAccessStore(int userId, int storeId) {
        try {
            logger.info("System Service - User accessed store: " + storeId + " by user: " + userId);
            StoreDTO s = this.storeService.viewStore(storeId);
            if (s.isOpen()) {
                return s;
            }
            logger.error("System Service - Store is closed: " + storeId);

        } catch (Exception e) {
            // Handle exception if needed
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public void openStore(int userId, String storeName) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                int storeId = this.storeService.addStore(userId, storeName);
                this.userService.addRole(userId, storeId, new StoreFounder());
                logger.info("System Service - User opened store: " + storeId + " by user: " + userId + " with name: "
                        + storeName);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during opening store: " + e.getMessage());
            throw new IllegalArgumentException("Error during opening store: " + e.getMessage());
        }
    }

    @Override
    public StoreProductDTO getProductFromStore(int productId, int storeId) {
        try {
            logger.info("System service - user trying to view procuct " + productId + " in store: " + storeId);
            StoreDTO s = this.storeService.viewStore(storeId);
            return s.getStoreProductById(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ProductDTO getProduct(int productId) {
        try {
            logger.info("System service - user trying to view procuct " + productId);
            return this.productService.viewProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int addProduct(String productName, String productDescription) {
        try {
            logger.info("System service - user trying to add procuct " + productName);
            return this.productService.addProduct(productName, productDescription);
        } catch (Exception e) {
            logger.error("System Service - Error during adding product: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public void updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        try {
            logger.info("System service - user trying to update procuct " + productId);
            this.productService.updateProduct(productId, productName, productDescription, storesIds);
        } catch (Exception e) {
            logger.error("System Service - Error during updating product: " + e.getMessage());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
        }try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
        }try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
        }try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
        }try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
        }
    }

}
