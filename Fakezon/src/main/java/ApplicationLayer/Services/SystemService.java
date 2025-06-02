package ApplicationLayer.Services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import DomainLayer.Interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import ApplicationLayer.DTO.AuctionProductDTO;
import ApplicationLayer.DTO.CartItemInfoDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.ProductRatingDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IRegisteredRole;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;

import DomainLayer.Model.Basket;

import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.Cart;
import DomainLayer.Model.ProductRating;
import DomainLayer.Model.Registered;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.User;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;

@Service
public class SystemService implements ISystemService {
    private IDelivery deliveryService;
    private IAuthenticator authenticatorService;
    private IPayment paymentService;
    private IUserService userService;
    private IStoreService storeService;
    private IProductService productService;
    private IOrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(SystemService.class);
    private final ApplicationEventPublisher publisher;
    private final INotificationWebSocketHandler notificationWebSocketHandler;

     @Autowired 
    public SystemService(IStoreRepository storeRepository, IUserRepository userRepository,
                         IProductRepository productRepository, IOrderRepository orderRepository,
                         ApplicationEventPublisher publisher, INotificationWebSocketHandler notificationWebSocketHandler) {
        this.publisher = publisher;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.storeService = new StoreService(storeRepository, publisher);
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
        this.orderService = new OrderService(orderRepository);
        this.deliveryService = new DeliveryAdapter();
        this.authenticatorService = new AuthenticatorAdapter(userService);
        this.paymentService = new PaymentAdapter();
        // USED BY UI - PUT IN A COMMENT IF NOT NEEDED
        //init();
    }

    // Overloaded constructor for testing purposes
    public SystemService(IStoreService storeService, IUserService userService, IProductService productService,
            IOrderService orderService,
            IDelivery deliveryService, IAuthenticator authenticatorService, IPayment paymentService,
            ApplicationEventPublisher publisher, INotificationWebSocketHandler notificationWebSocketHandler) {
        this.publisher = publisher;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
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
    public Response<Void> addToBasket(int userId, int productId, int storeId, int quantity) {
        StoreProductDTO product;
        try {
            if (this.storeService.isStoreOpen(storeId)) {
                logger.info("System Service - Store is open: " + storeId);
            } else {
                logger.error("System Service - Store is closed: " + storeId);
                return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.isUserLoggedIn(userId) || this.userService.isUnsignedUser(userId)) {
                logger.info("System Service - User is logged in  or Guest: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in or Guest", false, ErrorType.INVALID_INPUT, null);
            }
            product = this.storeService.getProductFromStore(productId, storeId);
        } catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            return new Response<>(null, "Error during adding to basket: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        if (product == null) {
            logger.error("System Service - Product not found: " + productId + " in store: " + storeId);
            return new Response<>(null, "Product not found", false, ErrorType.INVALID_INPUT, null);
        }

        if (product.getQuantity() < quantity) {
            logger.error("System Service - Not enough product in stock: " + productId + " in store: " + storeId);
            return new Response<>(null, "Not enough product in stock", false, ErrorType.INVALID_INPUT, null);
        }
        try {
            this.userService.addToBasket(userId, storeId, productId, quantity);
            logger.info(
                    "System Service - User added product to basket: " + productId + " from store: " + storeId
                            + " by user: "
                            + userId + " with quantity: " + quantity);
            return new Response<>(null, "Product added to basket successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            return new Response<>(null, "Error during adding to basket: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> ratingStore(int storeId, int userId, double rating, String comment) {
        try {
            if (!storeService.isStoreOpen(storeId)) {
                logger.error("System Service - Store is close: " + storeId);
                return new Response<>(null, "Store is close", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.didPurchaseStore(userId, storeId)) {
                if (rating < 0 || rating > 5) {
                    logger.error("System Service - Invalid rating value (rating should be between 0 to 5): " + rating);
                    return new Response<>(null, "Invalid rating value", false, ErrorType.INVALID_INPUT, null);
                }
                this.storeService.addStoreRating(storeId, userId, rating, comment);
                logger.info("System Service - User rated store: " + storeId + " by user: " + userId + " with rating: "
                        + rating);
                return new Response<>(null, "Store rated successfully", true, null, null);
            } else {
                logger.error("System Service - User did not purchase from this store: " + userId + " " + storeId);
                return new Response<>(null, "User did not purchase from this store", false, ErrorType.INVALID_INPUT,
                        null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating store: " + e.getMessage());
            return new Response<>(null, "Error during rating store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<Void> ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try {
            if (!storeService.isStoreOpen(storeId)) {
                logger.error("System Service - Store is close: " + storeId);
                return new Response<>(null, "Store is close", false, ErrorType.INVALID_INPUT, null);
            }
            if (rating < 0 || rating > 5) {
                logger.error("System Service - Invalid rating value (rating should be between 0 to 5): " + rating);
                return new Response<>(null, "Invalid rating value", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.productService.getProduct(productId) == null) {
                logger.error("System Service - Product not found: " + productId);
                return new Response<>(null, "Product not found", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.storeService.getProductFromStore(productId, storeId) == null) {
                logger.error("System Service - Product not found in store: " + productId + " in store: " + storeId);
                return new Response<>(null, "Product not found in store", false, ErrorType.INVALID_INPUT, null);
            }
            if(this.userService.isUserLoggedIn(userId)) {
                logger.info("System Service - User is logged in: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.didPurchaseProduct(userId, storeId, productId)) {
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
                logger.info("System Service - User rated product: " + productId + " in store: " + storeId + " by user: "
                        + userId + " with rating: " + rating);
                return new Response<>(null, "Product rated successfully", true, null, null);
            } else {
                logger.error("System Service - User did not purchase from this store that product: " + userId + " " + storeId + " " + productId);
                return new Response<>(null, "User did not purchase that productfrom the store", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating product: " + e.getMessage());
            return new Response<>(null, "Error during rating product: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    public Response<StoreDTO> userAccessStore(int storeId) {
        try {
            logger.info("System Service - User accessed store: " + storeId);
            
            StoreDTO s = this.storeService.viewStore(storeId);
            if (s.isOpen()) {
                return new Response<StoreDTO>(s, "Store retrieved successfully", true, null, null);
            }
            logger.error("System Service - Store is closed: " + storeId);
            return new Response<StoreDTO>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            // Handle exception if needed
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return new Response<StoreDTO>(null, "Error during user access store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Integer> addStore(int userId, String storeName) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                int storeId = this.storeService.addStore(userId, storeName);
                this.userService.addRole(userId, storeId, new StoreFounder());
                logger.info("System Service - User opened store: " + storeId + " by user: " + userId + " with name: "
                        + storeName);
                return new Response<>(storeId, "Store opened successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during opening store: " + e.getMessage());
            return new Response<>(null, "Error during opening store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> sendMessageToStore(int userId, int storeId, String message) {
        try {
            if (message == null || message.trim().isEmpty()) {
                logger.error("System Service - Message is empty");
                return new Response<>(null, "Message cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.isUserLoggedIn(userId)) {
                if (this.storeService.isStoreOpen(storeId)) {
                    this.userService.sendMessageToStore(userId, storeId, message);
                    logger.info("System Service - User sent message to store: " + storeId + " by user: " + userId
                            + " with message: " + message);
                    this.storeService.receivingMessage(storeId, userId, message);
                    logger.info("System Service - Store received message from user: " + userId + " to store: " + storeId
                            + " with message: " + message);
                    return new Response<>(null, "Message sent successfully", true, null, null);
                } else {
                    logger.error("System Service - Store is closed: " + storeId);
                    return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
                }
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to store: " + e.getMessage());
            return new Response<>(null, "Error during sending message to store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> sendMessageToUser(int managerId, int storeId, int userToAnswer, String message) {
        try {
            if (this.userService.isUserLoggedIn(managerId)) {
                if (this.storeService.isStoreOpen(storeId)) {
                    this.storeService.sendMessageToUser(managerId, storeId, userToAnswer, message);
                    logger.info(
                            "System Service - Store sent message to user: " + userToAnswer + " from store: " + storeId
                                    + " with message: " + message);
                    return new Response<>(null, "Message sent successfully", true, null, null);
                } else {
                    logger.error("System Service - Store is closed: " + storeId);
                    return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
                }
            } else {
                logger.error("System Service - User is not logged in: " + managerId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to user: " + e.getMessage());
            return new Response<>(null, "Error during sending message to user: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }
    @Override
    public LocalDate parseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }

    @Override
    public Response<StoreProductDTO> getProductFromStore(int productId, int storeId) {
        try {
            logger.info("System service - user trying to view product " + productId + " in store: " + storeId);
            if (!this.storeService.isStoreOpen(storeId)) {
                logger.error("System Service - Store is closed: " + storeId);
                return new Response<StoreProductDTO>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
            }
            StoreDTO s = this.storeService.viewStore(storeId);
            return new Response<StoreProductDTO>(s.getStoreProductById(productId), "Product retrieved successfully",
                    true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<StoreProductDTO>(null, "Error during getting product: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<ProductDTO> getProduct(int productId) {
        try {
            logger.info("System service - user trying to view product " + productId);
            ProductDTO productDTO = this.productService.viewProduct(productId);
            return new Response<ProductDTO>(productDTO, "Product retrieved successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Invalid input: " + e.getMessage());
            return new Response<ProductDTO>(null, "Invalid input", false, ErrorType.INVALID_INPUT, null);
        } catch (NullPointerException e) {
            logger.error("System Service - Null pointer encountered: " + e.getMessage());
            return new Response<ProductDTO>(null, "Unexpected null value", false, ErrorType.INTERNAL_ERROR, null);
        } catch (Exception e) {
            logger.error("System Service - General error: " + e.getMessage());
            return new Response<ProductDTO>(null, "An unexpected error occurred", false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<Boolean> updateProduct(int productId, String productName, String productDescription,
            Set<Integer> storesIds) {
        try {
            logger.info("System service - user trying to update product " + productId);
            this.productService.updateProduct(productId, productName, productDescription, storesIds);
            return new Response<>(true, "Product updated successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during updating product: " + e.getMessage());
            return new Response<>(false, "Error during updating product", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> deleteProduct(int productId) {
        try {
            logger.info("System service - user trying to delete product " + productId);
            this.productService.deleteProduct(productId);
            return new Response<>(true, "Product deleted successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during deleting product: " + e.getMessage());
            return new Response<>(false, "Error during deleting product", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> guestRegister(String email, String password, String dateOfBirth, String country) {
        logger.info("System service - user trying to register " + email);
        LocalDate dateOfBirthLocalDate;
        try {
            dateOfBirthLocalDate = parseDate(dateOfBirth);
        } catch (Exception e) {
            logger.error("System Service - Error during guest registration: " + e.getMessage());
            return new Response<>(null, "Invalid date of birth format. Expected format: YYYY-MM-DD", false,
                    ErrorType.INVALID_INPUT, null);
        }
        if (isValidCountryCode(country)) {
            logger.info("System Service - Country code is valid: " + country);
        } else {
            logger.error("System Service - Invalid country code: " + country);
            return new Response<>(null, "Invalid country code", false, ErrorType.INVALID_INPUT, null);
        }
        if (!isValidPassword(password)) {
            logger.error("System Service - Invalid password: " + password);
            return new Response<>(null, "Invalid password", false, ErrorType.INVALID_INPUT, null);
        }
        Response<String> response = this.authenticatorService.register(email, password, dateOfBirthLocalDate, country);
        if (!response.isSuccess()) {
            logger.error("System Service - Error during guest registration: " + response.getMessage());
            return new Response<>(null, response.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        String token = response.getData();
        if (token == null) {
            logger.error("System Service - Error during guest registration: " + email);
            return new Response<>(null, "Error during guest registration", false, ErrorType.INTERNAL_ERROR, null);
        }
        logger.info("System Service - User got token successfully: " + email);
        logger.info("System Service - User registered successfully: " + email);

        return new Response<>(null, "Guest registered successfully", true, null, null);

    }

    @Override
    public Response<List<ProductDTO>> searchByKeyword(String keyword) {
        try {
            logger.info("System service - user trying to view product " + keyword);
            return new Response<>(this.productService.searchProducts(keyword), "Products retrieved successfully", true,
                    null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<>(null, "Error during getting product: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }
    //addProduct method should be with amount and store?
    @Override
    public Response<Integer> addProduct(String productName, String productDescription, String category) {
        try {
            if (productName == null || productDescription == null || category == null) {
                logger.error(
                        "System Service - Invalid input: " + productName + " " + productDescription + " " + category);
                return new Response<>(-1, "Invalid input", false, ErrorType.INVALID_INPUT, null);
            }
            PCategory categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(-1, "Invalid category", false, ErrorType.INVALID_INPUT, null);
            }
            logger.info("System service - user trying to add product " + productName);
            int productId = this.productService.addProduct(productName, productDescription, categoryEnum);
            return new Response<>(productId, "Product added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding product: " + e.getMessage());
            return new Response<>(-1, "Error during adding product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
        // return -1;
    }

    private PCategory isCategoryValid(String category) {
        try {
            for (PCategory c : PCategory.values()) {
                if (c.name().equalsIgnoreCase(category)) {
                    return c;
                }
            }
            return null;
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Invalid category: " + category);
            return null;
        }
    }

    @Override
    public Response<StoreRolesDTO> getStoreRoles(int storeId, int userId) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                StoreRolesDTO storeRolesDTO = this.storeService.getStoreRoles(storeId, userId);
                return new Response<>(storeRolesDTO, "Store roles retrieved successfully", true, null, null);

            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting store roles: " + e.getMessage());
            return new Response<>(null, "Error during getting store roles: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreManagerPermissions(int storeId, int managerId, int requesterId, List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user trying to add permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
                storeService.addStoreManagerPermissions(storeId, requesterId, managerId, perms);
                return new Response<>(null, "Permissions added successfully", true, null, null);
        } catch (Exception e) {
            return new Response<>(null, "Error during adding store manager permissions: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    // add details to response
    @Override
    public Response<Void> removeStoreManagerPermissions(int storeId, int requesterId, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            storeService.removeStoreManagerPermissions(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Permissions removed successfully", true, null, null);
        } catch (Exception e) {
            return new Response<>(null, "Error during removing store manager permissions: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> removeStoreManager(int storeId, int requesterId, int managerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove manager " + managerId
                    + " from store: " + storeId);
            userService.removeRole(managerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreManager role from user " + e.getMessage());
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.removeStoreManager(storeId, requesterId, managerId);
            return new Response<>(null, "Store manager removed successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - removeStoreManager failed" + e.getMessage());
            userService.addRole(managerId, storeId, new StoreManager()); // reverting
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> removeStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove owner " + ownerId + " from store: "
                    + storeId);
            userService.removeRole(ownerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreOwner role from user " + e.getMessage());
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.removeStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner removed successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - removeStoreOwner failed" + e.getMessage());
            userService.addRole(ownerId, storeId, new StoreOwner()); // reverting
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreManager(int storeId, int requesterId, int managerId,
            List<StoreManagerPermission> perms) {
        if(!userService.isUserLoggedIn(requesterId)) {
            logger.error("System service - user " + requesterId + " is not logged in, cannot add as manager");
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
        }
        if(userService.isUnsignedUser(managerId)) {
            logger.error("System service - user " + managerId + " is not registered, cannot add as manager");
            return new Response<>(null, "User is not registered", false, ErrorType.INVALID_INPUT, null);
        }
        try {
            logger.info("System service - user " + requesterId + " trying to add manager " + managerId + " to store: " + storeId);
            storeService.addStoreManager(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Store manager added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to add manager to store " + e.getMessage());
            return new Response<>(null, "Error during adding store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to add owner " + ownerId + " to store: "
                    + storeId);
            storeService.addStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to add owner to store " + e.getMessage());
            return new Response<>(null, "Error during adding store owner: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    public Response<Void> addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int MinutesToEnd) {
        try {
            logger.info("System service - user " + requesterId + " trying to add auction product " + productID + " to store: " + storeId);
            this.storeService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, MinutesToEnd);

            return new Response<>(null, "Auction product added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding auction product to store: " + e.getMessage());
            return new Response<>(null, "Error during adding auction product to store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid) {
        try {
            if (this.userService.isUserLoggedIn(requesterId)) {
                logger.info("System service - user " + requesterId + " trying to add bid " + bid + " to auction product "
                    + productID + " in store: " + storeId);
                this.storeService.addBidOnAuctionProductInStore(storeId, requesterId, productID, bid);
                return new Response<>(null, "Bid added successfully", true, null, null);
            }
            else{
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during adding bid to auction product in store: " + e.getMessage());
            return new Response<>(null, "Error during adding bid to auction product in store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> closeStoreByFounder(int storeId, int userId) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                this.storeService.closeStore(storeId, userId);
                logger.info("System Service - User closed store: " + storeId + " by user: " + userId);
                return new Response<String>("Store closed successfully", "Store closed successfully", true, null, null);
            } else {
                    logger.error("System Service - User is not logged in: " + userId);
                    return new Response<String>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during closing store: " + e.getMessage());
            return new Response<String>(null, "Error during closing store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<StoreProductDTO> addProductToStore(int storeId, int requesterId, String productName,
            String description, double basePrice, int quantity, String category) {
        String name = null;
        int productId;
        PCategory categoryEnum = null;
        boolean isNewProd = false; //used for reverting if the operation fails
        try{
            logger.info("System service - user " + requesterId + " trying to add product " + productName + " to store " + storeId);
            if (!storeService.isStoreOpen(storeId)) {
            logger.error("System Service - Invalid store ID: " + storeId);
            return new Response<>(null, "Invalid store ID", false, ErrorType.INVALID_INPUT, null);
            }
            if (!storeService.getStoreOwners(storeId, requesterId).contains(requesterId)) {
            logger.error("System Service - User " + requesterId + " is not a owner of store " + storeId);
            return new Response<>(null, "User is not a owner of this store", false, ErrorType.INVALID_INPUT, null);
            }
            if (description == null || description.trim().isEmpty()) {
            logger.error("System Service - Product description is empty");
            return new Response<>(null, "Product description must not be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (productName == null || productName.trim().isEmpty()) {
            logger.error("System Service - Product name is empty");
            return new Response<>(null, "Product name must not be empty", false, ErrorType.INVALID_INPUT, null);
            }
            List<ProductDTO> products = productService.getAllProducts();
            ProductDTO product = null;
            for (ProductDTO prod : products) {
                if (prod.getName().equals(productName)) {
                    product = prod;
                    description = prod.getDescription();
                    break;
                }
            }
            categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(null, "Invalid category", false, ErrorType.INVALID_INPUT, null);
            }
            if (product == null) {
                isNewProd = true;
                productId = productService.addProduct(productName, description, categoryEnum);
                productService.addProductsToStore(storeId, List.of(productId));
            } else {
                productId = product.getId();
                if (!product.getDescription().equals(description)
                        || !product.getCategory().toString().equals(category)) {
                    throw new IllegalArgumentException("Product description/category is different than expected");
                }
            }

        } catch (Exception e) {
            logger.error("System service - failed to fetch product " + e.getMessage());
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {

            StoreProductDTO spDTO = storeService.addProductToStore(storeId, requesterId, productId, productName,
                    basePrice, quantity, categoryEnum);
            return new Response<>(spDTO, "Product added to store successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to add product to store " + e.getMessage());
            if (isNewProd) { // reverting
                productService.removeStoreFromProducts(storeId, List.of(productId));
                productService.deleteProduct(productId);
            }
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> updateProductInStore(int storeId, int requesterId, int productId, double basePrice,
            int quantity) {
        String name = null;
        try {
            logger.info("System service - user " + requesterId + " trying to update product " + productId + " in store "
                    + storeId);
            name = productService.viewProduct(productId).getName();
        } catch (Exception e) {
            logger.error("System service - failed to fetch product " + e.getMessage());
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.updateProductInStore(storeId, requesterId, productId, name, basePrice, quantity);
            return new Response<>(null, "Product updated in store successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to update product in store " + e.getMessage());
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> removeProductFromStore(int storeId, int requesterId, int productId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove product " + productId
                    + " from store " + storeId);
            storeService.removeProductFromStore(storeId, requesterId, productId);
            return new Response<>(null, "Product removed from store successfully", true, null, null);
        } catch (Exception e) {
            logger.info("System service - failed to remove product from store " + e.getMessage());
            return new Response<>(null, "Error during removing product from store: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    private Map<Integer, Map<Integer, Integer>> convertStoreCartToUserCatt(
            Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart) {
        Map<Integer, Map<Integer, Integer>> newCart = new HashMap<>();
        for (Map.Entry<StoreDTO, Map<StoreProductDTO, Boolean>> entry : cart.entrySet()) {
            int storeId = entry.getKey().getStoreId();
            Map<StoreProductDTO, Boolean> products = entry.getValue();
            Map<Integer, Integer> newProducts = new HashMap<>();
            for (Map.Entry<StoreProductDTO, Boolean> productEntry : products.entrySet()) {
                StoreProductDTO storeProduct = productEntry.getKey();
                newProducts.put(storeProduct.getProductId(), storeProduct.getQuantity());
            }
            newCart.put(storeId, newProducts);
        }
        return newCart;
    }

    @Override
    public Response<List<CartItemInfoDTO>> viewCart(int userId) { 
        try {
            logger.info("System service - user " + userId + " trying to view cart");
            if (this.userService.isUserLoggedIn(userId) || this.userService.isUnsignedUser(userId)) {
                Map<Integer, Map<Integer, Integer>> cart = this.userService.viewCart(userId);
                if (cart.isEmpty()) {
                    logger.info("System Service - Cart is empty: " + userId);
                    return new Response<>(new ArrayList<>(), "Cart is empty", true, null, null);
                }
                Map<StoreDTO, Map<StoreProductDTO, Boolean>> validCart = storeService.checkIfProductsInStores(userId,
                        cart);
                this.userService.setCart(userId, convertStoreCartToUserCatt(validCart));
                
                List<CartItemInfoDTO> items = cartToList(validCart);
                return new Response<>(items, "Cart retrieved successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during viewing cart: " + e.getMessage());
            return new Response<>(null, "Error during viewing cart: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getAllStoreMessages(int storeId, int userId) {
        try{
            if (this.storeService.isStoreOpen(storeId)) {
                return this.storeService.getAllStoreMessages(storeId, userId);
            } else {
                logger.error("System Service - Store is closed: " + storeId);
                return new Response<HashMap<Integer, String>>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }

    }


	@Override
	public Response<HashMap<Integer, String>> getAllMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAllMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false,
                        ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(),
                    false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getAssignmentMessages(int userID) {
        try {
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAssignmentMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false,
                        ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(),
                    false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getAuctionEndedMessages(int userID) {
        try {
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAuctionEndedMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false,
                        ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(),
                    false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> purchaseCart(int userId, String country, LocalDate dob, PaymentMethod paymentMethod,
            String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails) {
        Map<Integer, Double> prices = null;// storeId,price from store
        double totalPrice = 0;
        Cart cart = null;
        boolean returnProductInCaseOfError = false;//so the return will happen only ones
        
        Map<StoreDTO, Map<StoreProductDTO, Boolean>> validCartDTO = null;
        try {
            logger.info("System service - user " + userId + " trying to purchase cart");
            cart = this.userService.getUserCart(userId);
            if (cart.getAllProducts().isEmpty()) {
                logger.error("System Service - Cart is empty: " + userId);
                return new Response<String>(null, "Cart is empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (isValidCountryCode(country)) {
                logger.info("System Service - Country code is valid: " + country);
            } else {
                logger.error("System Service - Invalid country code: " + country);
                return new Response<String>(null, "Invalid country code", false, ErrorType.INVALID_INPUT, null);
            }
            Optional<User> user = this.userService.getAnyUserById(userId);
            if (!user.isPresent()) {
                logger.error("System Service - User not found: " + userId);
                return new Response<String>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during purchase cart: " + e.getMessage());
            return new Response<String>(null, "Error during purchase cart: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            validCartDTO = this.storeService.decrementProductsInStores(userId, cart.getAllProducts());
        } catch (Exception e) {
            logger.error("System Service - Error during purchase cart - decrementProductsInStores: " + e.getMessage());
            return new Response<String>(null, "Error during purchase cart: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        Map<Integer, Map<Integer, Integer>> validCart = convertStoreCartToUserCatt(validCartDTO);
        for (Map.Entry<StoreDTO, Map<StoreProductDTO, Boolean>> entry : validCartDTO.entrySet()) {
            Map<StoreProductDTO, Boolean> products = entry.getValue();
            for (Map.Entry<StoreProductDTO, Boolean> productEntry : products.entrySet()) {
                StoreProductDTO storeProduct = productEntry.getKey();
                if (productEntry.getValue() == false) {
                    logger.error("System Service - Product is not available: " + storeProduct.getName());
                    userService.setCart(userId, validCart);
                    returnProductInCaseOfError = true;
                    return new Response<String>(null, "Product is not available: " + storeProduct.getName(), false,
                            ErrorType.INVALID_INPUT, null);
                }
            }
        }
        //if payment/delivery won't work
        userService.setCart(userId, validCart);

        prices = this.storeService.calcAmount(userId, cart, dob);
        totalPrice = prices.values().stream().mapToDouble(Double::doubleValue).sum();
        logger.info("System Service - User " + userId + "cart price: " + totalPrice);
        try {
            if (this.paymentService.pay(cardNumber, cardHolder, expDate, cvv, totalPrice))
                logger.info("System Service - User " + userId + " cart purchased successfully, payment method: "
                        + paymentMethod);
            else {
                if (!returnProductInCaseOfError) {
                    storeService.returnProductsToStores(userId, validCart);
                    returnProductInCaseOfError = true;
                }

                return new Response<String>(null, "Payment failed", false, ErrorType.INVALID_INPUT, null);
        }} catch (Exception e) {
            logger.error("System Service - Error during payment: " + e.getMessage());
            if (!returnProductInCaseOfError) {
                storeService.returnProductsToStores(userId, validCart);
                returnProductInCaseOfError = true;
            }
            return new Response<String>(null, "Error during payment: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            if (this.deliveryService.deliver(country, address, recipient, packageDetails))
                logger.info("System Service - User " + userId + " cart delivered to: " + recipient + " at address: "
                        + address);
            else {
                if (!returnProductInCaseOfError) {
                    storeService.returnProductsToStores(userId, validCart);
                    returnProductInCaseOfError = true;
                }
                return new Response<String>(null, "Delivery failed", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            this.paymentService.refund(cardNumber, totalPrice);
            if (!returnProductInCaseOfError) {
                storeService.returnProductsToStores(userId, validCart);
                returnProductInCaseOfError = true;
            }
            logger.error("System Service - Error during delivery: " + e.getMessage());
            logger.info("System Service - User " + userId + " cart purchase failed, refund issued to: " + cardHolder
                    + " at card number: " + cardNumber);
        }

        this.orderService.addOrderCart(validCartDTO, prices, userId, address, paymentMethod);
        this.userService.clearUserCart(userId);

        return new Response<String>("Cart purchased successfully", "Cart purchased successfully", true, null, null);
    }

    @Override
    public List<Integer> extractPurchasedProductIds(Map<StoreDTO, Map<StoreProductDTO, Boolean>> validCart) {
    List<Integer> productIds = new ArrayList<>();
    for (Map<StoreProductDTO, Boolean> products : validCart.values()) {
        for (Map.Entry<StoreProductDTO, Boolean> entry : products.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                productIds.add(entry.getKey().getProductId());
            }
        }
    }
    return productIds;
}

    @Override
    public Response<String> sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept) {
        try {
            if (this.userService.isUserLoggedIn(requesterId)) {
                this.storeService.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept);
                logger.info("System Service - User sent response for auction: " + productId + " in store: " + storeId
                        + " by user: " + requesterId + " with accept: " + accept);
                return new Response<>("Response sent successfully", "Response sent successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending response for auction: " + e.getMessage());
            return new Response<>(null, "Error during sending response for auction: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<StoreProductDTO>> getTopRatedProducts(int limit) {
        try {
            logger.info("System service - fetching top " + limit + " rated products");

            // 1. Fetch all products
            List<ProductDTO> allProducts = productService.getAllProducts();

            // 2. Create a list to store products with their ratings
            List<StoreProductDTO> ratedProducts = new ArrayList<>();

            // 3. For each product, get its StoreProductDTO from each store it's in
            for (ProductDTO product : allProducts) {
                for (Integer storeId : product.getStoreIds()) {
                    try {
                        StoreProductDTO storeProduct = storeService.getProductFromStore(product.getId(), storeId);
                        // Only add products that have ratings
                        if (!Double.isNaN(storeProduct.getAverageRating())) {
                            ratedProducts.add(storeProduct);
                        }
                    } catch (Exception e) {
                        // Skip if product not found in store or other errors
                        logger.warn("Could not retrieve product " + product.getId() + " from store " + storeId + ": "
                                + e.getMessage());
                    }
                }
            }

            // 4. Sort products by average rating (highest first)
            ratedProducts.sort((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()));

            // 5. Return top 'limit' products (or all if there are fewer than 'limit')
            int resultSize = Math.min(limit, ratedProducts.size());
            List<StoreProductDTO> topRatedProducts = ratedProducts.subList(0, resultSize);

            return new Response<>(topRatedProducts, "Top rated products retrieved successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error while fetching top rated products: " + e.getMessage());
            return new Response<>(null, "Error fetching top rated products: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    public OrderDTO createOrderDTO(IOrder order) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (int productId : order.getProductIds()) {
            ProductDTO productDTO = this.productService.viewProduct(productId);
            productDTOS.add(productDTO);
        }
        return new OrderDTO(order.getId(), order.getUserId(), order.getStoreId(), productDTOS,
                order.getState().toString(), order.getAddress(), order.getPaymentMethod().toString());

    }

    @Override
    public Response<List<OrderDTO>> getAllStoreOrders(int storeId, int userId) {
        try {
            logger.info("System service - user " + userId + " trying to get all orders from " + storeId);
            if (!storeService.canViewOrders(storeId, userId)) {
                return new Response<List<OrderDTO>>(null,
                        "user " + userId + " has insufficient permissions to view orders from store " + storeId, false,
                        ErrorType.INVALID_INPUT, null);
            }
            List<IOrder> storeOrders = orderService.getOrdersByStoreId(storeId);
            List<OrderDTO> storeOrdersDTOs = new ArrayList<>();
            for (IOrder order : storeOrders) {
                storeOrdersDTOs.add(createOrderDTO(order));
            }
            return new Response<List<OrderDTO>>(storeOrdersDTOs, "success", true, null, null);
        } catch (Exception e) {
            return new Response<List<OrderDTO>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> acceptAssignment(int storeId, int userId) {
        try {
            boolean isowner;
            logger.info("system service - user " + userId + " trying to accept assignment for store " + storeId);
            isowner = storeService.acceptAssignment(storeId, userId);
            if (isowner) {
                userService.addRole(userId, storeId, new StoreOwner());
                logger.info("system service - user " + userId + " is now an owner of store " + storeId);
            }
            else{
                userService.addRole(userId, storeId, new StoreManager());
                logger.info("system service - user " + userId + " is now a manager of store " + storeId);
            }
            userService.removeAssignmentMessage(storeId, userId);
            logger.info("system service - user " + userId + " assignment message removed for store " + storeId);
            return new Response<String>("success", "success", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> declineAssignment(int storeId, int userId) {
        try {
            logger.info("system service - user " + userId + " trying to decline assignment for store " + storeId);
            storeService.declineAssignment(storeId, userId);
            userService.removeAssignmentMessage(storeId, userId);
            logger.info("system service - user " + userId + " assignment message removed for store " + storeId);
            return new Response<String>("success", "success", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<Integer>> getPendingOwners(int storeId, int requesterId) {
        try {
            logger.info("system service - user " + requesterId + " trying to get pending owners for store " + storeId);
            List<Integer> pending = storeService.getPendingOwners(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<Integer>> getPendingManagers(int storeId, int requesterId) {
        try {
            logger.info(
                    "system service - user " + requesterId + " trying to get pending managers for store " + storeId);
            List<Integer> pending = storeService.getPendingManagers(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    // county Validation method
    private boolean isValidCountryCode(String code) {
        String[] isoCountries = Locale.getISOCountries();
        return Arrays.asList(isoCountries).contains(code);
    }


    public Response<Boolean> deleteOrder(int orderId, int userId) {
        try {
            if(this.userService.isUserLoggedIn(userId)) {
                this.orderService.deleteOrder(orderId);
                return new Response<>(true, "Order deleted successfully", true, null, null);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(false, "User is not logged in", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            logger.error("System Service - Error during deleting order: " + e.getMessage());
            return new Response<>(false, "Error during deleting order", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<OrderDTO> viewOrder(int orderId, int userId) {
        try {
            if(this.userService.isUserLoggedIn(userId)) {
                IOrder order = this.orderService.viewOrder(orderId);
                OrderDTO orderDTO = createOrderDTO(order);
                return new Response<>(orderDTO, "Order retrieved successfully", true, null, null);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            logger.error("System Service - Error during viewing order: " + e.getMessage());
            return new Response<>(null, "Error during viewing order", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<OrderDTO>> searchOrders(String keyword, int userId) {
        try {
            if(this.userService.isUserLoggedIn(userId)) {
                List<IOrder> orders = this.orderService.searchOrders(keyword);
                List<OrderDTO> orderDTOS = new ArrayList<>();
                for (IOrder order : orders) {
                    OrderDTO orderDTO = createOrderDTO(order);
                    orderDTOS.add(orderDTO);
                }
                return new Response<>(orderDTOS, "Orders retrieved successfully", true, null, null);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            logger.error("System Service - Error during searching orders: " + e.getMessage());
            return new Response<>(null, "Error during searching orders", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<OrderDTO>> getOrdersByStoreId(int storeId, int userId) {
        try {

            if(this.userService.isUserLoggedIn(userId)) {
                List<IOrder> orders = this.orderService.getOrdersByStoreId(storeId);
                List<OrderDTO> orderDTOS = new ArrayList<>();
                for (IOrder order : orders) {
                    OrderDTO orderDTO = createOrderDTO(order);
                    orderDTOS.add(orderDTO);
                }
                return new Response<>(orderDTOS, "Orders retrieved successfully", true, null, null);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            logger.error("System Service - Error during getting orders by store id: " + e.getMessage());
            return new Response<>(null, "Error during getting orders by store id", false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<AbstractMap.SimpleEntry<UserDTO, String>> login(String email, String password) {
        try {
            // Check if the user exists first
            Optional<Registered> optionalUser = userService.getUserByUserName(email);
            if (optionalUser.isPresent()) {
                Registered user = optionalUser.get();
                // Check if the user is suspended before login
                if (userService.isUserSuspended(user.getUserId())) {
                    logger.error("System Service - Login failed: User is suspended: " + email);
                    return new Response<>(null, "Login failed: User is suspended", false, ErrorType.INVALID_INPUT,
                            null);
                }
            }

            String token = this.authenticatorService.login(email, password);
            UserDTO user = this.userService.login(email, password);
            return new Response<>(new AbstractMap.SimpleEntry<>(user, token), "Successful Login", true, null, token);
        } catch (Exception e) {
            logger.error("System Service - Login failed: " + e.getMessage());
            return new Response<>(null, "Login failed: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    // User suspension management methods (admin only)

    @Override
    public Response<Void> suspendUser(int requesterId, int userId, LocalDate endOfSuspension) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to suspend user: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            userService.suspendUser(requesterId, userId, endOfSuspension);

            if (endOfSuspension == null) {
                logger.info("System Service - User ID " + userId + " permanently suspended by admin ID " + requesterId);
                return new Response<>(null, "User permanently suspended", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId + " suspended until " + endOfSuspension
                        + " by admin ID " + requesterId);
                return new Response<>(null, "User suspended until " + endOfSuspension, true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error during suspension: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during suspension: " + e.getMessage());
            return new Response<>(null, "Error during suspension: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<Boolean> unsuspendUser(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to unsuspend user: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(false, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            boolean wasUnsuspended = userService.unsuspendUser(requesterId, userId);

            if (wasUnsuspended) {
                logger.info("System Service - User ID " + userId + " unsuspended by admin ID " + requesterId);
                return new Response<>(true, "User unsuspended successfully", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId + " was not suspended (unsuspend request by admin ID "
                        + requesterId + ")");
                return new Response<>(false, "User was not suspended", true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error during unsuspension: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during unsuspension: " + e.getMessage());
            return new Response<>(false, "Error during unsuspension: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> isUserSuspended(int userId) {
        try {
            boolean isSuspended = userService.isUserSuspended(userId);
            logger.info("System Service - Checked suspension status for User ID " + userId + ": "
                    + (isSuspended ? "Suspended" : "Not suspended"));
            return new Response<>(isSuspended, "Suspension status checked successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error checking suspension status: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error checking suspension status: " + e.getMessage());
            return new Response<>(false, "Error checking suspension status: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<LocalDate> getSuspensionEndDate(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get suspension end date: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            LocalDate endDate = userService.getSuspensionEndDate(requesterId, userId);

            String message = endDate == null ? "User is permanently suspended" : "User is suspended until " + endDate;
            logger.info("System Service - " + message + " (checked by admin ID " + requesterId + ")");
            return new Response<>(endDate, message, true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error getting suspension end date: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error getting suspension end date: " + e.getMessage());
            return new Response<>(null, "Error getting suspension end date: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<Registered>> getAllSuspendedUsers(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get suspended users: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            List<Registered> suspendedUsers = userService.getAllSuspendedUsers(requesterId);

            logger.info("System Service - Retrieved " + suspendedUsers.size()
                    + " suspended users (requested by admin ID " + requesterId + ")");
            return new Response<>(suspendedUsers, suspendedUsers.size() + " suspended users found", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error retrieving suspended users: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error retrieving suspended users: " + e.getMessage());
            return new Response<>(null, "Error retrieving suspended users: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Integer> cleanupExpiredSuspensions(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to cleanup suspensions: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(-1, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            int removedCount = userService.cleanupExpiredSuspensions(requesterId);

            logger.info("System Service - Cleaned up " + removedCount + " expired suspensions (requested by admin ID "
                    + requesterId + ")");
            return new Response<>(removedCount, "Cleaned up " + removedCount + " expired suspensions", true, null,
                    null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error during cleanup: " + e.getMessage());
            return new Response<>(-1, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during cleanup: " + e.getMessage());
            return new Response<>(-1, "Error during cleanup: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    // System admin management methods

    @Override
    public Response<Void> addSystemAdmin(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to add system admin: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            userService.addSystemAdmin(userId);

            logger.info("System Service - User ID " + userId + " added as system admin by admin ID " + requesterId);
            return new Response<>(null, "User added as system admin successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error adding system admin: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error adding system admin: " + e.getMessage());
            return new Response<>(null, "Error adding system admin: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<Boolean> removeSystemAdmin(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to remove system admin: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(false, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            // Prevent removing yourself as an admin
            if (requesterId == userId) {
                logger.error("System Service - Admin ID " + requesterId + " attempted to remove themselves as admin");
                return new Response<>(false, "Cannot remove yourself as admin", false, ErrorType.INVALID_INPUT, null);
            }

            boolean wasRemoved = userService.removeSystemAdmin(userId);

            if (wasRemoved) {
                logger.info("System Service - User ID " + userId + " removed from system admins by admin ID "
                        + requesterId);
                return new Response<>(true, "User removed from system admins successfully", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId
                        + " was not a system admin (remove request by admin ID " + requesterId + ")");
                return new Response<>(false, "User was not a system admin", true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error removing system admin: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error removing system admin: " + e.getMessage());
            return new Response<>(false, "Error removing system admin: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> isSystemAdmin(int userId) {
        try {
            boolean isAdmin = userService.isSystemAdmin(userId);
            logger.info("System Service - Checked admin status for User ID " + userId + ": "
                    + (isAdmin ? "Admin" : "Not admin"));
            return new Response<>(isAdmin, "Admin status checked successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error checking admin status: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error checking admin status: " + e.getMessage());
            return new Response<>(false, "Error checking admin status: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<Registered>> getAllSystemAdmins(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get system admins: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            List<Registered> admins = userService.getAllSystemAdmins();

            logger.info("System Service - Retrieved " + admins.size() + " system admins (requested by admin ID "
                    + requesterId + ")");
            return new Response<>(admins, admins.size() + " system admins found", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error retrieving system admins: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error retrieving system admins: " + e.getMessage());
            return new Response<>(null, "Error retrieving system admins: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Integer> getSystemAdminCount(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get admin count: Admin privileges required for user ID "
                                + requesterId);
                return new Response<>(-1, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            int count = userService.getSystemAdminCount();

            logger.info("System Service - Current system admin count: " + count + " (requested by admin ID "
                    + requesterId + ")");
            return new Response<>(count, "Current system admin count: " + count, true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error getting admin count: " + e.getMessage());
            return new Response<>(-1, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error getting admin count: " + e.getMessage());
            return new Response<>(-1, "Error getting admin count: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
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

    @Override
    public Response<List<ProductDTO>> searchByCategory(String category) {
        try {
            PCategory categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                List<ProductDTO> npProducts = new ArrayList<>();
                return new Response<>(npProducts, "Invalid category", true, null, null);
            }
            List<ProductDTO> products = this.productService.getProductsByCategory(categoryEnum);
            return new Response<>(products, "Products retrieved successfully", true, null, null);

        } catch (Exception e) {
            logger.error("System Service - Error during searching products by category: " + e.getMessage());
            return new Response<>(null, "Error during searching products by category", false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    @Override
    public Response<Void> userLogout(int userID) {
        try {
            logger.info("System service - user " + userID + " trying to logout");
            Optional<Registered> user = this.userService.getUserById(userID);

            if (user == null) {
                logger.error("System Service - User not found: " + userID);
                return new Response<>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.isUserLoggedIn(userID)) {
                String email = user.get().getEmail();
                this.authenticatorService.logout(email);
                this.userService.logout(email);
                return new Response<>(null, "Logout successful", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during logout: " + e.getMessage());
            return new Response<>(null, "Error during logout: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR,
                    null);
        }
    }

    // Unsigned (guest) user management methods

    @Override
    public Response<UserDTO> createUnsignedUser() {
        try {
            User unsignedUser = userService.createUnsignedUser(); 
            int userId = unsignedUser.getUserId();
            logger.info("System Service - created unsigned user with ID: " + unsignedUser.getUserId());
            return new Response<>(unsignedUser.toDTO(), "Unsigned user created successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to create unsigned user: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding unsigned user: " + e.getMessage());
            return new Response<>(null, "Error adding unsigned user: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<UserDTO> getUnsignedUserById(int userId) {
        try {
            Optional<User> optionalUser = userService.getUnsignedUserById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                UserDTO userDTO = user.toDTO(); //email = null , age = -1
                logger.info("System Service - Retrieved unsigned user with ID: " + userId);
                return new Response<>(userDTO, "Unsigned user retrieved successfully", true, null, null);
            } else {
                logger.error("System Service - Unsigned user not found: " + userId);
                return new Response<>(null, "Unsigned user not found", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get unsigned user: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting unsigned user: " + e.getMessage());
            return new Response<>(null, "Error getting unsigned user: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<UserDTO>> getAllUnsignedUsers(int adminId) {
        try {
            if (!userService.isSystemAdmin(adminId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get all unsigned users: Admin privileges required for user ID "
                                + adminId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            List<UserDTO> users = userService.getAllUnsignedUsersDTO();
            logger.info("System Service - Retrieved " + users.size() + " unsigned users");
            return new Response<>(users, "Retrieved " + users.size() + " unsigned users", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get all unsigned users: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting all unsigned users: " + e.getMessage());
            return new Response<>(null, "Error getting all unsigned users: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> removeUnsignedUser(int userId) {
        try {
            boolean removed = userService.removeUnsignedUser(userId);
            if (removed) {
                logger.info("System Service - Removed unsigned user with ID: " + userId);
                return new Response<>(true, "Unsigned user removed successfully", true, null, null);
            } else {
                logger.info("System Service - No unsigned user with ID " + userId + " to remove");
                return new Response<>(false, "No unsigned user with that ID found", true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to remove unsigned user: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during removing unsigned user: " + e.getMessage());
            return new Response<>(false, "Error removing unsigned user: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> isUnsignedUser(int userId) {
        try {
            boolean isUnsigned = userService.isUnsignedUser(userId);
            logger.info("System Service - Checked if user ID " + userId + " is unsigned: " + isUnsigned);
            return new Response<>(isUnsigned, "User's unsigned status checked successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to check if user is unsigned: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during checking if user is unsigned: " + e.getMessage());
            return new Response<>(false, "Error checking if user is unsigned: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Integer> getUnsignedUserCount(int adminId) {
        try {
            if (!userService.isSystemAdmin(adminId)) {
                logger.error(
                        "System Service - Unauthorized attempt to get unsigned user count: Admin privileges required for user ID "
                                + adminId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }

            int count = userService.getUnsignedUserCount();
            logger.info("System Service - Retrieved unsigned user count: " + count);
            return new Response<>(count, "Retrieved unsigned user count: " + count, true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get unsigned user count: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting unsigned user count: " + e.getMessage());
            return new Response<>(null, "Error getting unsigned user count: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }
    @Override
    public Response<List<ProductDTO>> searchProductsByName(String productName) {
         try {
            logger.info("System Service - Searching for products with name: " + productName);
            List<ProductDTO> products = productService.searchProductsByName(productName);
            return new Response<>(products, "Products retrieved successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting all products: {}", e.getMessage());
            return new Response<>(null, "Error during getting all products", false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    public boolean init(){
        try{
            logger.info("system service init");
            this.login("testNormalUser1004@gmail.com", "a12345");
            this.purchaseCart(1004, "IL", LocalDate.of(1998, 10, 15), PaymentMethod.CREDIT_CARD, "deliver", "1234", "Yuval Bachar", "never", "123","address1004","Yuval Bachar", "details");
            this.ratingStoreProduct(1001, 1001, 1004, 4.5, "Great!");
            this.ratingStoreProduct(1001, 1002, 1004, 2, "Meh");
            this.userLogout(1004);
            return true;
        }
        catch(Exception e){
            logger.error("System Service - Error during init: " + e.getMessage());
            return false;
        }

    }

    private List<CartItemInfoDTO> cartToList(Map<StoreDTO, Map<StoreProductDTO, Boolean>> cart) {
        List<CartItemInfoDTO> items = new ArrayList<>();
        for (StoreDTO store : cart.keySet()) {
            for (StoreProductDTO product : cart.get(store).keySet()) {
                items.add(new CartItemInfoDTO(store.getStoreId(), product.getProductId(), store.getName(), product.getName(), product.getQuantity(), cart.get(store).get(product), product.getBasePrice(), false));
            }
        }
        return items;
    }

    @Override
    public Response<Double> getCartFinalPrice(int userId, LocalDate dob){
        Cart cart = null;
        try {
            logger.info("System service - user " + userId + " checking final cart price");
            cart = this.userService.getUserCart(userId);
            if (cart.getAllProducts().isEmpty()) {
                logger.error("System Service - Cart is empty: " + userId);
                return new Response<Double>(null, "Cart is empty", false, ErrorType.INVALID_INPUT, null);
            }
            Optional<User> user = this.userService.getAnyUserById(userId);
            if (!user.isPresent()) {
                logger.error("System Service - User not found: " + userId);
                return new Response<Double>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during final cart price: " + e.getMessage());
            return new Response<Double>(null, "Error during final cart price: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try{
            Map<Integer, Double> map = this.storeService.calcAmount(userId, cart, dob);
            Double finalPrice = 0.0;
            for(Integer storeId : map.keySet()){
                finalPrice += map.get(storeId);
            }
            return new Response<Double>(finalPrice, "success", true, null, null);
        }
        catch(Exception e){
            return new Response<Double>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public void clearAllData() {
        storeService.clearAllData();
        userService.clearAllData();
        orderService.clearAllData();
        productService.clearAllData();
    }
    @Override
    public Response<List<ProductRatingDTO>> getStoreProductRatings(int storeId, int prodId){
        try{
            logger.info("System Service - request for all store product rating store " + storeId + " prodId "+ prodId);
            List<ProductRating> ratings = storeService.getStoreProductRatings(storeId, prodId);
            List<ProductRatingDTO> ratingsDTOs = new ArrayList<>();
            for(ProductRating r : ratings){
                ratingsDTOs.add(prodRatingToProdRatingDTO(r));
            }
            return new Response<List<ProductRatingDTO>>(ratingsDTOs, null, true, null, null);

        }
        catch(Exception e){
            logger.error("System Service - error during getStoreProductRatings "+ e.getMessage());
            return new Response<>(null, "Error during getStoreProductRatings " + e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        }
    }

    private ProductRatingDTO prodRatingToProdRatingDTO(ProductRating rating){
        Optional<Registered> user = userService.getUserById(rating.getUserID());
        if(user.isPresent()){
            return new ProductRatingDTO(rating.getRating(), rating.getComment(), user.get().getEmail());
        }
        else{
            throw new IllegalArgumentException("user " + rating.getUserID() + " does not exist - prodRatingToProdRatingDTO");
        }
    }

    @Override
    public Response<Void> removeFromBasket(int userId, int productId, int storeId) {
        try {
            if (this.userService.isUserLoggedIn(userId) || this.userService.isUnsignedUser(userId)) {
                logger.info("System Service - User is logged in: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during removing from basket: " + e.getMessage());
            return new Response<>(null, "Error during removing from basket: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
        try {
            this.userService.removeFromBasket(userId, storeId, productId);
            logger.info(
                    "System Service - User Removed basket product: " + productId + " from store: " + storeId
                            + " by user: "
                            + userId);
            return new Response<>(null, "Product removed from basket successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during removing from basket: " + e.getMessage());
            return new Response<>(null, "Error during removing from basket: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }
    @Override
    public Response<HashMap<Integer, IRegisteredRole>> getUserRoles(int requesterId){
        try {
            if(this.userService.isUserLoggedIn(requesterId)) {
                HashMap<Integer, IRegisteredRole> roles = this.userService.getAllRoles(requesterId);
                return new Response<>(roles, "User roles retrieved successfully", true, null, null);
            }
            logger.error("System Service - User is not logged in: " + requesterId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);

        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get user roles: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting user roles: " + e.getMessage());
            return new Response<>(null, "Error getting user roles: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> isStoreOwner(int storeId, int userId){
        try{
            logger.info("checking if user " + userId + " is owner in store "+ storeId);
            return new Response<Boolean>(storeService.isStoreOwner(storeId, userId), null, true, null, null);
        }
        catch (Exception e){
            logger.info("isStoreOwner failed");
            return new Response<Boolean>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override // returns null if not manager
    public Response<List<StoreManagerPermission>> isStoreManager(int storeId, int userId){
        try{
            logger.info("checking if user " + userId + " is owner in store "+ storeId);
            return new Response<List<StoreManagerPermission>>(storeService.isStoreManager(storeId, userId), null, true, null, null);
        }
        catch (Exception e){
            logger.info("isStoreManager failed");
            return new Response<List<StoreManagerPermission>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<AuctionProductDTO>> getAuctionProductsFromStore(int storeId, int userId){
        try{
            logger.info("User " + userId + " Trying to fetch auction products from store " + storeId);
            if(userService.isUnsignedUser(userId)){
                return new Response<List<AuctionProductDTO>>(null, "User is not logged in",false, ErrorType.UNAUTHORIZED, null);
            }
            List<AuctionProductDTO> prods = storeService.getAuctionProductsFromStore(storeId);
            logger.info("success getAuctionProductsFromStore");
            return new Response<List<AuctionProductDTO>>(prods, null, true, null, null);
        }
        catch(Exception e){
            return new Response<List<AuctionProductDTO>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<OrderDTO>> getOrdersByUserId(int userId) {
        try {
            if (!userService.isUserLoggedIn(userId)) {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            List<IOrder> orders = orderService.getOrdersByUserId(userId);
            List<OrderDTO> orderDTOs = orders.stream().map(iorder -> createOrderDTO(iorder)).collect(Collectors.toList());
            return new Response<>(orderDTOs, "Orders retrieved successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting all orders by user ID: " + e.getMessage());
            return new Response<>(null, "Error during getting all orders by user ID: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public boolean isStoreOpen(int storeId) {
        return storeService.isStoreOpen(storeId);
    }


    // Discount Policy System Service Methods

    @Override
    public Response<Void> addSimpleDiscountWithProductsScope(int storeId, int requesterId, List<Integer> productIDs, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (productIDs == null || productIDs.isEmpty()) {
                logger.error("System Service - Product IDs list is null or empty");
                return new Response<>(null, "Product IDs list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addSimpleDiscountWithProductsScope(storeId, requesterId, productIDs, percentage);
            logger.info("System Service - Simple discount with products scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "Simple discount with products scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding simple discount with products scope: " + e.getMessage());
            return new Response<>(null, "Error during adding simple discount with products scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addSimpleDiscountWithStoreScope(int storeId, int requesterId, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addSimpleDiscountWithStoreScope(storeId, requesterId, percentage);
            logger.info("System Service - Simple discount with store scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "Simple discount with store scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding simple discount with store scope: " + e.getMessage());
            return new Response<>(null, "Error during adding simple discount with store scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addConditionDiscountWithProductsScope(int storeId, int requesterId, int cartId, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (productIDs == null || productIDs.isEmpty()) {
                logger.error("System Service - Product IDs list is null or empty");
                return new Response<>(null, "Product IDs list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addConditionDiscountWithProductsScope(storeId, requesterId, productIDs, conditions, percentage);
            logger.info("System Service - Condition discount with products scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "Condition discount with products scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding condition discount with products scope: " + e.getMessage());
            return new Response<>(null, "Error during adding condition discount with products scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addConditionDiscountWithStoreScope(int storeId, int requesterId, int cartId, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addConditionDiscountWithStoreScope(storeId, requesterId, conditions, percentage);
            logger.info("System Service - Condition discount with store scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "Condition discount with store scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding condition discount with store scope: " + e.getMessage());
            return new Response<>(null, "Error during adding condition discount with store scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addAndDiscountWithProductsScope(int storeId, int requesterId, int cartId, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (productIDs == null || productIDs.isEmpty()) {
                logger.error("System Service - Product IDs list is null or empty");
                return new Response<>(null, "Product IDs list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addAndDiscountWithProductsScope(storeId, requesterId, productIDs, conditions, percentage);
            logger.info("System Service - AND discount with products scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "AND discount with products scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding AND discount with products scope: " + e.getMessage());
            return new Response<>(null, "Error during adding AND discount with products scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addAndDiscountWithStoreScope(int storeId, int requesterId, int cartId, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addAndDiscountWithStoreScope(storeId, requesterId, conditions, percentage);
            logger.info("System Service - AND discount with store scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "AND discount with store scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding AND discount with store scope: " + e.getMessage());
            return new Response<>(null, "Error during adding AND discount with store scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addOrDiscountWithProductsScope(int storeId, int requesterId, int cartId, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (productIDs == null || productIDs.isEmpty()) {
                logger.error("System Service - Product IDs list is null or empty");
                return new Response<>(null, "Product IDs list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addOrDiscountWithProductsScope(storeId, requesterId, productIDs, conditions, percentage);
            logger.info("System Service - OR discount with products scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "OR discount with products scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding OR discount with products scope: " + e.getMessage());
            return new Response<>(null, "Error during adding OR discount with products scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addOrDiscountWithStoreScope(int storeId, int requesterId, int cartId, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addOrDiscountWithStoreScope(storeId, requesterId, conditions, percentage);
            logger.info("System Service - OR discount with store scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "OR discount with store scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding OR discount with store scope: " + e.getMessage());
            return new Response<>(null, "Error during adding OR discount with store scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addXorDiscountWithProductsScope(int storeId, int requesterId, int cartId, List<Integer> productIDs, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (productIDs == null || productIDs.isEmpty()) {
                logger.error("System Service - Product IDs list is null or empty");
                return new Response<>(null, "Product IDs list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addXorDiscountWithProductsScope(storeId, requesterId, productIDs, conditions, percentage);
            logger.info("System Service - XOR discount with products scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "XOR discount with products scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding XOR discount with products scope: " + e.getMessage());
            return new Response<>(null, "Error during adding XOR discount with products scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addXorDiscountWithStoreScope(int storeId, int requesterId, int cartId, List<Predicate<Cart>> conditions, double percentage) {
        try {
            if (!userService.isUserLoggedIn(requesterId)) {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            if (percentage < 0 || percentage > 100) {
                logger.error("System Service - Invalid percentage: " + percentage);
                return new Response<>(null, "Percentage must be between 0 and 100", false, ErrorType.INVALID_INPUT, null);
            }
            if (conditions == null || conditions.isEmpty()) {
                logger.error("System Service - Conditions list is null or empty");
                return new Response<>(null, "Conditions list cannot be empty", false, ErrorType.INVALID_INPUT, null);
            }

            storeService.addXorDiscountWithStoreScope(storeId, requesterId, conditions, percentage);
            logger.info("System Service - XOR discount with store scope added to store: " + storeId + " by user: " + requesterId);
            return new Response<>(null, "XOR discount with store scope added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding XOR discount with store scope: " + e.getMessage());
            return new Response<>(null, "Error during adding XOR discount with store scope: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<HashMap<Integer, String>> getMessagesFromStore(int userID) {
        try {
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getMessagesFromStore(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false,
                        ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(),
                    false, ErrorType.INTERNAL_ERROR, null);
        }
    }


}
