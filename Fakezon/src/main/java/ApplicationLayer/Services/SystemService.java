package ApplicationLayer.Services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
import DomainLayer.Model.Order;
import DomainLayer.Model.StoreFounder;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;

public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public SystemService(IStoreRepository storeRepository, IUserRepository userRepository,
            IProductRepository productRepository) {
        this.storeService = new StoreService(storeRepository);
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter(userService);
        this.paymentService = new PaymentAdapter();
    }

    // Overloaded constructor for testing purposes
    public SystemService(IStoreService storeService, IUserService userService, IProductService productService,
            IDelivery deliveryService, IAuthenticator authenticatorService, IPayment paymentService) {
        this.storeService = storeService;
        this.userService = userService;
        this.productService = productService;
        this.deliveryService = deliveryService;
        this.authenticatorService = authenticatorService;
        this.paymentService = paymentService;
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

    public StoreDTO userAccessStore(String token, int storeId) {
        try {
            logger.info("System Service - User accessed store: " + storeId + " by user with token " + token);
            if (this.authenticatorService.isValid(token))
                logger.info("System Service - Token is valid: " + token);
            else {
                logger.error("System Service - Token is not valid: " + token);
                throw new IllegalArgumentException("Token is not valid");
            }
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
    public void addStore(int userId, String storeName) {
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
    public HashMap<Integer, Order> getOrdersByUser(int userId) {
        try {
            if (!this.userService.isUserLoggedIn(userId)) {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
            HashMap<Integer, Order> orders = this.userService.getOrdersByUser(userId);
            logger.info("System Service - User orders retrieved: " + userId);
            return orders;
        } catch (Exception e) {
            logger.error("System Service - Error during retrieving user orders: " + e.getMessage());
            throw new IllegalArgumentException("Error during retrieving user orders: " + e.getMessage());
        }
    }

    @Override
    public void sendMessageToStore(int userId, int storeId, String message) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                if (this.storeService.isStoreOpen(storeId)) {
                    this.userService.sendMessageToStore(userId, storeId, message);
                    logger.info("System Service - User sent message to store: " + storeId + " by user: " + userId
                            + " with message: " + message);
                    this.storeService.receivingMessage(storeId, userId, message);
                    logger.info("System Service - Store received message from user: " + userId + " to store: " + storeId
                            + " with message: " + message);
                } else {
                    logger.error("System Service - Store is closed: " + storeId);
                    throw new IllegalArgumentException("Store is closed");
                }
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to store: " + e.getMessage());
            throw new IllegalArgumentException("Error during sending message to store: " + e.getMessage());
        }
    }

    @Override
    public void sendMessageToUser(int managerId, int storeId, int userToAnswer, String message) {
        try {
            if (this.userService.isUserLoggedIn(managerId)) {
                if (this.storeService.isStoreOpen(storeId)) {
                    this.storeService.sendMessageToUser(managerId, storeId, userToAnswer, message);
                    logger.info(
                            "System Service - Store sent message to user: " + userToAnswer + " from store: " + storeId
                                    + " with message: " + message);
                    this.userService.receivingMessageFromStore(userToAnswer, storeId, message);
                    logger.info("System Service - User received message from store: " + storeId + " to user: "
                            + userToAnswer
                            + " with message: " + message);
                } else {
                    logger.error("System Service - Store is closed: " + storeId);
                    throw new IllegalArgumentException("Store is closed");
                }
            } else {
                logger.error("System Service - User is not logged in: " + managerId);
                throw new IllegalArgumentException("User is not logged in");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to user: " + e.getMessage());
            throw new IllegalArgumentException("Error during sending message to user: " + e.getMessage());
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
        }
    }

    @Override
    public String guestRegister(String email, String password, String dateOfBirth) {
        logger.info("System service - user trying to register " + email);
        LocalDate dateOfBirthLocalDate = LocalDate.parse(dateOfBirth);
        String token = this.authenticatorService.register(email, password, dateOfBirthLocalDate);
        if (token == null) {
            logger.error("System Service - Error during guest registration: " + email);
        } else {
            logger.info("System service - user registered successfully " + email);
        }
        return token;
    }

    @Override
    public List<ProductDTO> getProduct(String token, String keyword) {
        try {
            if (!this.authenticatorService.isValid(token)) {
                logger.error("System Service - Token is not valid: " + token);
                throw new IllegalArgumentException("Token is not valid");
            } else {
                logger.info("System Service - Token is valid: " + token);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return null;
        }
        try {
            logger.info("System service - user trying to view procuct " + keyword);
            return this.productService.searchProducts(keyword);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
        }
        return null;
    }

    private int addProduct(String productName, String productDescription) {
        try {
            logger.info("System service - user trying to add procuct " + productName);
            return this.productService.addProduct(productName, productDescription);
        } catch (Exception e) {
            logger.error("System Service - Error during adding product: " + e.getMessage());
        }
        return -1;
    }

}
