package ApplicationLayer.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ApplicationLayer.Response;
import InfrastructureLayer.Repositories.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Order;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Enums.ErrorType;

public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private IOrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    private final ApplicationEventPublisher publisher;

    public SystemService(IStoreRepository storeRepository, IUserRepository userRepository,
            IProductRepository productRepository, IOrderRepository orderRepository,ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.storeService = new StoreService(storeRepository, publisher);
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
        this.orderService = new OrderService(orderRepository);
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter(userService);
        this.paymentService = new PaymentAdapter();
    }

    // Overloaded constructor for testing purposes
    public SystemService(IStoreService storeService, IUserService userService, IProductService productService, IOrderService orderService,
            IDelivery deliveryService, IAuthenticator authenticatorService, IPayment paymentService,
            ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.storeService = storeService;
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
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
    public void addToBasket(int userId, int productId, int storeId, int quantity) {
        StoreProductDTO product;
        try {
            if (this.storeService.isStoreOpen(storeId)) {
                logger.info("System Service - Store is open: " + storeId);
            } else {
                logger.error("System Service - Store is closed: " + storeId);
                throw new IllegalArgumentException("Store is closed");
            }
            if (!this.userService.isUserLoggedIn(userId)) {
                logger.info("System Service - User is logged in: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
            product = this.storeService.decrementProductQuantity(productId, storeId, quantity);
        } catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            throw new IllegalArgumentException("Error during adding to basket: " + e.getMessage());
        }
        this.userService.addToBasket(userId, storeId, product);
        logger.info(
                "System Service - User added product to basket: " + productId + " from store: " + storeId + " by user: "
                        + userId + " with quantity: " + quantity);
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
    public Response<List<OrderDTO>> getOrdersByUser(int userId) {
        try {
            if (!this.userService.isUserLoggedIn(userId)) {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<List<OrderDTO>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            logger.info("System Service - User orders retrieved: " + userId);
            return this.userService.getOrdersByUser(userId);
        } catch (Exception e) {
            logger.error("System Service - Error during retrieving user orders: " + e.getMessage());
            return new Response<List<OrderDTO>>(null, "Error during retrieving user orders: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR);
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
    public Response<ProductDTO> getProduct(int productId) {
        try {
            logger.info("System service - user trying to view procuct " + productId);
            ProductDTO productDTO = this.productService.viewProduct(productId);
            return new Response<ProductDTO>(productDTO, "Product retrieved successfully", true);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Invalid input: " + e.getMessage());
            return new Response<ProductDTO>(null, "Invalid input", false, ErrorType.INVALID_INPUT);
        } catch (NullPointerException e) {
            logger.error("System Service - Null pointer encountered: " + e.getMessage());
            return new Response<ProductDTO>(null, "Unexpected null value", false, ErrorType.INTERNAL_ERROR);
        } catch (Exception e) {
            logger.error("System Service - General error: " + e.getMessage());
            return new Response<ProductDTO>(null, "An unexpected error occurred", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Boolean> updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
        try {
            logger.info("System service - user trying to update procuct " + productId);
            this.productService.updateProduct(productId, productName, productDescription, storesIds);
            return new Response<>(true, "Product updated successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during updating product: " + e.getMessage());
            return new Response<>(false, "Error during updating product", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Boolean> deleteProduct(int productId) {
        try {
            logger.info("System service - user trying to delete procuct " + productId);
            this.productService.deleteProduct(productId);
            return new Response<>(true, "Product deleted successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
            return new Response<>(false, "Error during deleting product", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public String guestRegister(String email, String password, String dateOfBirth) {
        logger.info("System service - user trying to register " + email);
        LocalDate dateOfBirthLocalDate;
        try {
            dateOfBirthLocalDate = LocalDate.parse(dateOfBirth);
        } catch (Exception e) {
            logger.error("System Service - Error during guest registration: " + e.getMessage());
            throw new java.time.format.DateTimeParseException(
                    "Invalid date of birth format. Expected format: YYYY-MM-DD", dateOfBirth, 0);
        }
        String token = this.authenticatorService.register(email, password, dateOfBirthLocalDate);
        if (token == null) {
            logger.error("System Service - Error during guest registration: " + email);
        } else {
            logger.info("System service - user registered successfully " + email);
        }
        return token;
    }

    @Override
    public Response<List<ProductDTO>> searchByKeyword(String token, String keyword) {
        try {
            if (!this.authenticatorService.isValid(token)) {
                logger.error("System Service - Token is not valid: " + token);
                throw new IllegalArgumentException("Token is not valid");
            } else {
                logger.info("System Service - Token is valid: " + token);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return new Response<>(null, "Error during user access store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            logger.info("System service - user trying to view procuct " + keyword);
            return new Response<>(this.productService.searchProducts(keyword), "Products retrieved successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<>(null, "Error during getting product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
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

    @Override
    public StoreRolesDTO getStoreRoles(int storeId, int userId) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                return this.storeService.getStoreRoles(storeId, userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting store roles: " + e.getMessage());
            throw new IllegalArgumentException("Error during getting store roles: " + e.getMessage());
        }
    }

    @Override
    public void addStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("Systrem service - user sessionToken: " + sessionToken + " trying to add permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            if (this.authenticatorService.isValid(sessionToken)) {
                int requesterId = this.authenticatorService.getUserId(sessionToken);
                storeService.addStoreManagerPermissions(storeId, requesterId, managerId, perms);
            } else {
                throw new IllegalArgumentException("Invalid session token: " + sessionToken);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during adding store manager permissions: " + e.getMessage());
        }
    }

    @Override
    public void removeStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user sessionToken: " + sessionToken + " trying to remove permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            int requesterId = this.authenticatorService.getUserId(sessionToken);
            storeService.removeStoreManagerPermissions(storeId, requesterId, managerId, perms);
        } catch (Exception e) {
            logger.error("System Service - Error during removing store manager permissions: " + e.getMessage());
        }
    }

    @Override
    public void removeStoreManager(int storeId, int requesterId, int managerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove manager " + managerId
                    + " from store: " + storeId);
            userService.removeRole(managerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreManager role from user " + e.getMessage());
            throw e;
        }
        try {
            storeService.removeStoreManager(storeId, requesterId, managerId);
        } catch (Exception e) {
            logger.error("System service - removeStoreManager failed" + e.getMessage());
            userService.addRole(managerId, storeId, new StoreManager()); // reverting
            throw e;
        }
    }

    @Override
    public void removeStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove owner " + ownerId + " from store: "
                    + storeId);
            userService.removeRole(ownerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreOwner role from user " + e.getMessage());
            throw e;
        }
        try {
            storeService.removeStoreOwner(storeId, requesterId, ownerId);
        } catch (Exception e) {
            logger.error("System service - removeStoreOwner failed" + e.getMessage());
            userService.addRole(ownerId, storeId, new StoreOwner()); // reverting
            throw e;
        }
    }

    @Override
    public void addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user " + requesterId + " trying to add manager " + managerId + " to store: "
                    + storeId);
            userService.addRole(managerId, storeId, new StoreManager());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreManager role to user " + e.getMessage());
            throw e;
        }
        try {
            storeService.addStoreManager(storeId, requesterId, managerId, perms);
        } catch (Exception e) {
            logger.error("System service - failed to add manager to store " + e.getMessage());
            userService.removeRole(managerId, storeId); // reverting
            throw e;
        }
    }

    @Override
    public void addStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to add owner " + ownerId + " to store: "
                    + storeId);
            userService.addRole(ownerId, storeId, new StoreOwner());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreOwner role to user " + e.getMessage());
            throw e;
        }
        try {
            storeService.addStoreOwner(storeId, requesterId, ownerId);
        } catch (Exception e) {
            logger.error("System service - failed to add owner to store " + e.getMessage());
            userService.removeRole(ownerId, storeId); // reverting
            throw e;
        }
    }
    @Override
    public void addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd) {
        try {
            logger.info("System service - user " + requesterId + " trying to add auction product " + productID
                    + " to store: " + storeId);
            this.storeService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, daysToEnd);
        } catch (Exception e) {
            logger.error("System Service - Error during adding auction product to store: " + e.getMessage());
        }
    }
    @Override
    public void addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid) {
        try {
            logger.info("System service - user " + requesterId + " trying to add bid " + bid + " to auction product "
                    + productID + " in store: " + storeId);
            this.storeService.addBidOnAuctionProductInStore(storeId, requesterId, productID, bid);
        } catch (Exception e) {
            logger.error("System Service - Error during adding bid to auction product in store: " + e.getMessage());
        }
    }

    @Override
    public Response<String> closeStoreByFounder(int storeId, int userId) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                this.storeService.closeStore(storeId, userId);
                logger.info("System Service - User closed store: " + storeId + " by user: " + userId);
                return new Response<String>("Store closed successfully","Store closed successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<String>(null, "User is not logged in",false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during closing store: " + e.getMessage());
            return new Response<String>(null, "Error during closing store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    @Override
    public void addProductToStore(int storeId, int requesterId, int productId, double basePrice, int quantity){
        String name = null;
        try{
            logger.info("System service - user " + requesterId + " trying to add product " + productId + " to store " + storeId);
            name = productService.viewProduct(productId).getName();
        }
        catch (Exception e){
            logger.error("System service - failed to fetch product " + e.getMessage());
            throw e;
        }
        try{
            storeService.addProductToStore(storeId, requesterId, productId, name, basePrice, quantity);
        }
        catch (Exception e){
            logger.error("System service - failed to add product to store " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateProductInStore(int storeId, int requesterId, int productId, double basePrice, int quantity){
        String name = null;
        try{
            logger.info("System service - user " + requesterId + " trying to update product " + productId +" in store " + storeId);
            name = productService.viewProduct(productId).getName();
        }
        catch (Exception e){
            logger.error("System service - failed to fetch product " + e.getMessage());
            throw e;
        }
        try{
            storeService.addProductToStore(storeId, requesterId, productId, name, basePrice, quantity);
        }
        catch (Exception e){
            logger.error("System service - failed to update product in store " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void removeProductFromStore(int storeId, int requesterId, int productId){
        try {
            logger.info("System service - user " + requesterId + " trying to remove product " + productId + " from store " + storeId);
            storeService.removeProductFromStore(storeId, requesterId, productId);
        } 
        catch (Exception e) {
            logger.info("System service - failed to remove product from store " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd){
        try{
            logger.info("System service - user " + requesterId + " trying to add store auction product days");
            storeService.addStoreAuctionProductDays(storeId, requesterId, productId, daysToAdd);
        }
        catch (Exception e) {
            logger.info("System service - failed to add auction product days  " + e.getMessage());
            throw e;
        }
    }

   

    @Override
    public List<StoreProductDTO> viewCart(int userId) {
        try {
            logger.info("System service - user " + userId + " trying to view cart");
            if (this.userService.isUserLoggedIn(userId)) {
                return this.userService.viewCart(userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                throw new IllegalArgumentException("User is not logged in");
            }
        } catch (Exception e) {
            logger.error("System Service - Error during viewing cart: " + e.getMessage());
            throw new IllegalArgumentException("Error during viewing cart: " + e.getMessage());
        }
    }

    private OrderDTO createOrderDTO(Order order) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (int productId : order.getProductIds()) {
            ProductDTO productDTO = this.productService.viewProduct(productId);
            productDTOS.add(productDTO);
        }
        return new OrderDTO(order.getId(), order.getUserId(), order.getStoreId(), productDTOS,
                order.getState().toString(), order.getAddress(), order.getPaymentMethod().toString());

    }

    // // Example of a system service method that uses the authenticator service
    // public void SystemServiceMethod(String sessionToken) {
    // if (authenticatorService.isValid(sessionToken)) {
    // int userId = authenticatorService.getUserId(sessionToken);
    // storeService.doSomething(userId);
    // } else {
    // logger.error("System Service - Invalid session token: " + sessionToken);
    // throw new IllegalArgumentException("Invalid session token");
    // }
    // }
}
