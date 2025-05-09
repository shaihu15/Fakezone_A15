package ApplicationLayer.Services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import ApplicationLayer.DTO.BasketDTO;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Interfaces.IProduct;
import DomainLayer.Model.Basket;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;
import DomainLayer.Model.User;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import javassist.bytecode.LineNumberAttribute.Pc;

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
    public Response<Void> addToBasket(int userId, int productId, int storeId, int quantity) {
        StoreProductDTO product;
        try {
            if (this.storeService.isStoreOpen(storeId)) {
                logger.info("System Service - Store is open: " + storeId);
            } else {
                logger.error("System Service - Store is closed: " + storeId);
                return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
            }
            if (this.userService.isUserLoggedIn(userId)) {
                logger.info("System Service - User is logged in: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            product = this.storeService.getProductFromStore(productId, storeId);
        } catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            return new Response<>(null, "Error during adding to basket: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        if (product == null) {
            logger.error("System Service - Product not found: " + productId + " in store: " + storeId);
            return new Response<>(null, "Product not found", false, ErrorType.INVALID_INPUT, null);
        }
        
        if (product.getQuantity() < quantity) {
            logger.error("System Service - Not enough product in stock: " + productId + " in store: " + storeId);
            return new Response<>(null, "Not enough product in stock", false, ErrorType.INVALID_INPUT, null);
        }
        try{
            this.userService.addToBasket(userId, storeId, productId, quantity);
            logger.info(
                "System Service - User added product to basket: " + productId + " from store: " + storeId + " by user: "
                        + userId + " with quantity: " + quantity);
            return new Response<>(null, "Product added to basket successfully", true, null, null);
        }
        catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            return new Response<>(null, "Error during adding to basket: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> ratingStore(int storeId, int userId, double rating, String comment) {
        try {
            if (this.userService.didPurchaseStore(userId, storeId)) {
                this.storeService.addStoreRating(storeId, userId, rating, comment);
                logger.info("System Service - User rated store: " + storeId + " by user: " + userId + " with rating: " + rating);
                return new Response<>(null, "Store rated successfully", true, null, null);
            } else {
                logger.error("System Service - User did not purchase from this store: " + userId + " " + storeId);
                return new Response<>(null, "User did not purchase from this store", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating store: " + e.getMessage());
            return new Response<>(null, "Error during rating store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try {
            if (this.userService.didPurchaseProduct(userId, storeId, productId)) {
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
                logger.info("System Service - User rated product: " + productId + " in store: " + storeId + " by user: " + userId + " with rating: " + rating);
                return new Response<>(null, "Product rated successfully", true, null, null);
            } else {
                logger.error("System Service - User did not purchase from this product: " + userId + " " + storeId + " " + productId);
                return new Response<>(null, "User did not purchase from this product", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating product: " + e.getMessage());
            return new Response<>(null, "Error during rating product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    public Response<StoreDTO> userAccessStore(String token, int storeId) {
        try {
            logger.info("System Service - User accessed store: " + storeId + " by user with token " + token);
            
            if (this.authenticatorService.isValid(token)){
                logger.info("System Service - Token is valid: " + token);
            }else {
                logger.error("System Service - Token is not valid: " + token);
                return new Response<StoreDTO>(null, "Token is not valid", false, ErrorType.INVALID_INPUT, null);
            }
            StoreDTO s = this.storeService.viewStore(storeId);
            if (s.isOpen()) {
                return new Response<StoreDTO>(s, "Store retrieved successfully", true, null, null);
            }
            logger.error("System Service - Store is closed: " + storeId);
            return new Response<StoreDTO>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);

        } catch (Exception e) {
            // Handle exception if needed
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return new Response<StoreDTO>(null, "Error during user access store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Integer> addStore(int userId, String storeName) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                int storeId = this.storeService.addStore(userId, storeName);
                this.userService.addRole(userId, storeId, new StoreFounder());
                logger.info("System Service - User opened store: " + storeId + " by user: " + userId + " with name: " + storeName);
                return new Response<>(storeId, "Store opened successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during opening store: " + e.getMessage());
            return new Response<>(null, "Error during opening store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<OrderDTO>> getOrdersByUser(int userId) {
        try {
            if (!this.userService.isUserLoggedIn(userId)) {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<List<OrderDTO>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            logger.info("System Service - User orders retrieved: " + userId);
            return this.userService.getOrdersByUser(userId);
        } catch (Exception e) {
            logger.error("System Service - Error during retrieving user orders: " + e.getMessage());
            return new Response<List<OrderDTO>>(null, "Error during retrieving user orders: " + e.getMessage(), false,
                    ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> sendMessageToStore(int userId, int storeId, String message) {
        try {
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
            return new Response<>(null, "Error during sending message to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
                } 
                else {
                    logger.error("System Service - Store is closed: " + storeId);
                    return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT, null);
                }
            } else {
                logger.error("System Service - User is not logged in: " + managerId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to user: " + e.getMessage());
            return new Response<>(null, "Error during sending message to user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
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
            StoreDTO s = this.storeService.viewStore(storeId);
            return new Response<StoreProductDTO>(s.getStoreProductById(productId), "Product retrieved successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<StoreProductDTO>(null, "Error during getting product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
            return new Response<ProductDTO>(null, "An unexpected error occurred", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Boolean> updateProduct(int productId, String productName, String productDescription, Set<Integer> storesIds) {
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
            return new Response<>(null, "Invalid date of birth format. Expected format: YYYY-MM-DD", false, ErrorType.INVALID_INPUT, null);
        }
        if(isValidCountryCode(country)) {
            logger.info("System Service - Country code is valid: " + country);
        } else {
            logger.error("System Service - Invalid country code: " + country);
            return new Response<>(null, "Invalid country code", false, ErrorType.INVALID_INPUT, null);
        }
        if(!isValidPassword(password)){
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

        return new Response<> ( null, "Guest registered successfully", true, null, null);
        
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
            return new Response<>(null, "Error during user access store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            logger.info("System service - user trying to view product " + keyword);
            return new Response<>(this.productService.searchProducts(keyword), "Products retrieved successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<>(null, "Error during getting product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    //addProduct method should be with amount and store?
    private Response<Integer> addProduct(String productName, String productDescription, String category) {
        try {
            if (productName == null || productDescription == null || category == null) {
                logger.error("System Service - Invalid input: " + productName + " " + productDescription + " " + category);
                return new Response<>(-1, "Invalid input", false, ErrorType.INVALID_INPUT, null);
            }
            PCategory categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(-1, "Invalid category", false, ErrorType.INVALID_INPUT, null);
            }
            logger.info("System service - user trying to add product " + productName);
            int productId = this.productService.addProduct(productName, productDescription,categoryEnum);
            return new Response<>(productId, "Product added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding product: " + e.getMessage());
            return new Response<>(-1, "Error during adding product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        //return -1;
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
            return new Response<>(null, "Error during getting store roles: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user sessionToken: " + sessionToken + " trying to add permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            if (this.authenticatorService.isValid(sessionToken)) {
                int requesterId = this.authenticatorService.getUserId(sessionToken);
                storeService.addStoreManagerPermissions(storeId, requesterId, managerId, perms);
                return new Response<>(null, "Permissions added successfully", true, null, null);
            } else {
                return new Response<>(null, "Invalid session token: " + sessionToken, false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            return new Response<>(null, "Error during adding store manager permissions: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    //add details to response
    @Override
    public Response<Void> removeStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user sessionToken: " + sessionToken + " trying to remove permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            int requesterId = this.authenticatorService.getUserId(sessionToken);
            storeService.removeStoreManagerPermissions(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Permissions removed successfully", true, null, null);
        } catch (Exception e) {
            return new Response<>(null, "Error during removing store manager permissions: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.removeStoreManager(storeId, requesterId, managerId);
            return new Response<>(null, "Store manager removed successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - removeStoreManager failed" + e.getMessage());
            userService.addRole(managerId, storeId, new StoreManager()); // reverting
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> removeStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove owner " + ownerId + " from store: " + storeId);
            userService.removeRole(ownerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreOwner role from user " + e.getMessage());
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.removeStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner removed successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - removeStoreOwner failed" + e.getMessage());
            userService.addRole(ownerId, storeId, new StoreOwner()); // reverting
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user " + requesterId + " trying to add manager " + managerId + " to store: " + storeId);
            userService.addRole(managerId, storeId, new StoreManager());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreManager role to user " + e.getMessage());
            return new Response<>(null, "Error during adding store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);}
        try {
            storeService.addStoreManager(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Store manager added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to add manager to store " + e.getMessage());
            userService.removeRole(managerId, storeId); // reverting
            return new Response<>(null, "Error during adding store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> addStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to add owner " + ownerId + " to store: " + storeId);
            userService.addRole(ownerId, storeId, new StoreOwner());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreOwner role to user " + e.getMessage());
            return new Response<>(null, "Error during adding store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            storeService.addStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System service - failed to add owner to store " + e.getMessage());
            userService.removeRole(ownerId, storeId); // reverting
            return new Response<>(null, "Error during adding store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Void> addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd) {
        try {
            logger.info("System service - user " + requesterId + " trying to add auction product " + productID + " to store: " + storeId);
            this.storeService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, daysToEnd);
            return new Response<>(null, "Auction product added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding auction product to store: " + e.getMessage());
            return new Response<>(null, "Error during adding auction product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Void> addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid) {
        try {
            logger.info("System service - user " + requesterId + " trying to add bid " + bid + " to auction product " + productID + " in store: " + storeId);
            this.storeService.addBidOnAuctionProductInStore(storeId, requesterId, productID, bid);
            return new Response<>(null, "Bid added successfully", true, null, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding bid to auction product in store: " + e.getMessage());
            return new Response<>(null, "Error during adding bid to auction product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> closeStoreByFounder(int storeId, int userId) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                this.storeService.closeStore(storeId, userId);
                logger.info("System Service - User closed store: " + storeId + " by user: " + userId);
                return new Response<String>("Store closed successfully","Store closed successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<String>(null, "User is not logged in",false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during closing store: " + e.getMessage());
            return new Response<String>(null, "Error during closing store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<StoreProductDTO> addProductToStore(int storeId, int requesterId, String productName, String description, double basePrice, int quantity, String category) {
        String name = null;
        int productId;
        PCategory categoryEnum = null;
        boolean isNewProd = false; //used for reverting if the operation fails
        try{
            logger.info("System service - user " + requesterId + " trying to add product " + productName + " to store " + storeId);
            List<ProductDTO> products = productService.getAllProducts();
            ProductDTO product = null;
            for(ProductDTO prod : products){
                if(prod.getName().equals(productName)){
                    product = prod;
                    break;
                }
            }
            categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(null, "Invalid category", false, ErrorType.INVALID_INPUT, null);
            }
            if(product == null){
                isNewProd = true;
                productId = productService.addProduct(productName, description, categoryEnum);
                productService.addProductsToStore(storeId, List.of(productId));
            }
            else{
                productId = product.getId();
                if(!product.getDescription().equals(description) || !product.getCategory().toString().equals(category)){
                    throw new IllegalArgumentException("Product description/category is different than expected");
                }
            }

        }
        catch (Exception e){
            logger.error("System service - failed to fetch product " + e.getMessage());
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try{
            
            StoreProductDTO spDTO =storeService.addProductToStore(storeId, requesterId, productId, productName, basePrice, quantity, categoryEnum);
            return new Response<>(spDTO, "Product added to store successfully", true, null, null);
        }
        catch (Exception e){
            logger.error("System service - failed to add product to store " + e.getMessage());
            if(isNewProd){ // reverting
                productService.removeStoreFromProducts(storeId, List.of(productId));
                productService.deleteProduct(productId);
            }
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> updateProductInStore(int storeId, int requesterId, int productId, double basePrice, int quantity){
        String name = null;
        try{
            logger.info("System service - user " + requesterId + " trying to update product " + productId +" in store " + storeId);
            name = productService.viewProduct(productId).getName();
        }
        catch (Exception e){
            logger.error("System service - failed to fetch product " + e.getMessage());
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try{
            storeService.updateProductInStore(storeId, requesterId, productId, name, basePrice, quantity);
            return new Response<>(null, "Product updated in store successfully", true, null, null);
        }
        catch (Exception e){
            logger.error("System service - failed to update product in store " + e.getMessage());
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> removeProductFromStore(int storeId, int requesterId, int productId){
        try {
            logger.info("System service - user " + requesterId + " trying to remove product " + productId + " from store " + storeId);
            storeService.removeProductFromStore(storeId, requesterId, productId);
            return new Response<>(null, "Product removed from store successfully", true, null, null);
        } 
        catch (Exception e) {
            logger.info("System service - failed to remove product from store " + e.getMessage());
            return new Response<>(null, "Error during removing product from store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    private Map<Integer, Map<Integer, Integer>> convertStoreCartToUserCatt(Map<StoreDTO,Map<StoreProductDTO,Boolean>> cart) {
        Map<Integer,Map<Integer,Integer>> newCart = new HashMap<>();
                for (Map.Entry<StoreDTO, Map<StoreProductDTO,Boolean>> entry : cart.entrySet()) {
                    int storeId = entry.getKey().getStoreId();
                    Map<StoreProductDTO,Boolean> products = entry.getValue();
                    Map<Integer,Integer> newProducts = new HashMap<>();
                    for (Map.Entry<StoreProductDTO,Boolean> productEntry : products.entrySet()) {
                        StoreProductDTO storeProduct = productEntry.getKey();
                        newProducts.put(storeProduct.getProductId(), storeProduct.getQuantity());
                    }
                    newCart.put(storeId, newProducts);
                }
        return newCart;
    }

    @Override
    public Response<Map<StoreDTO,Map<StoreProductDTO,Boolean>>> viewCart(int userId) {
        try {
            logger.info("System service - user " + userId + " trying to view cart");
            if (this.userService.isUserLoggedIn(userId)) {
                Map<Integer,Map<Integer,Integer>> cart = this.userService.viewCart(userId);
                if (cart.isEmpty()) {
                    logger.error("System Service - Cart is empty: " + userId);
                    return new Response<>(null, "Cart is empty", false, ErrorType.INVALID_INPUT, null);
                }
                Map<StoreDTO, Map<StoreProductDTO,Boolean>> validCart = storeService.checkIfProductsInStores(cart);
                
                this.userService.setCart(userId, convertStoreCartToUserCatt(validCart));

                return new Response<>(validCart, "Cart retrieved successfully", true, null, null);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during viewing cart: " + e.getMessage());
            return new Response<>(null, "Error during viewing cart: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

	@Override
	public Response<HashMap<Integer, String>> getAllMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAllMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
	}

	@Override
	public Response<HashMap<Integer, String>> getAssignmentMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAssignmentMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }	}

	@Override
	public Response<HashMap<Integer, String>> getAuctionEndedMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAuctionEndedMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    	}

    @Override
    public synchronized Response<String> purchaseCart(int userId, String country, LocalDate dob, PaymentMethod paymentMethod, String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails) {
        Map<Integer,Double> prices = null;//storeId,price from store
        double totalPrice = 0;
        Cart cart = null;
        Map<StoreDTO, Map<StoreProductDTO,Boolean>> validCart = null;
        try{
            logger.info("System service - user " + userId + " trying to purchase cart");
            cart = this.userService.getUserCart(userId);
            if(cart.getAllProducts().isEmpty()){
                logger.error("System Service - Cart is empty: " + userId);
                return new Response<String>(null, "Cart is empty", false, ErrorType.INVALID_INPUT, null);
            }
            if(isValidCountryCode(country)) {
                logger.info("System Service - Country code is valid: " + country);
            } else {
                logger.error("System Service - Invalid country code: " + country);
                return new Response<String>(null, "Invalid country code", false, ErrorType.INVALID_INPUT, null);
            }
            Optional<User> user = this.userService.getAnyUserById(userId);
            if(!user.isPresent()){
                logger.error("System Service - User not found: " + userId);
                return new Response<String>(null, "User not found", false, ErrorType.INVALID_INPUT, null);
            }
            validCart = this.storeService.checkIfProductsInStores(cart.getAllProducts());
            for (Map.Entry<StoreDTO, Map<StoreProductDTO,Boolean>> entry : validCart.entrySet()) {
                StoreDTO store = entry.getKey();
                Map<StoreProductDTO,Boolean> products = entry.getValue();
                for (Map.Entry<StoreProductDTO,Boolean> productEntry : products.entrySet()) {
                    StoreProductDTO storeProduct = productEntry.getKey();
                    if (productEntry.getValue() == false) {
                        logger.error("System Service - Product is not available: " + storeProduct.getName());
                        return new Response<String>(null, "Product is not available: " + storeProduct.getName(), false, ErrorType.INVALID_INPUT, null);
                    }
                }
            }
            prices = this.storeService.calcAmount(userId,cart,dob);
            logger.info("System Service - User "+userId + "cart price: " + totalPrice);
            totalPrice = prices.values().stream().mapToDouble(Double::doubleValue).sum();
        }
        catch (Exception e) {
            logger.error("System Service - Error during purchase cart: " + e.getMessage());
            return new Response<String>(null, "Error during purchase cart: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            this.paymentService.pay(cardNumber, cardHolder, expDate, cvv, totalPrice);
            logger.info("System Service - User " + userId + " cart purchased successfully, payment method: " + paymentMethod);
        } catch (Exception e) {
            logger.error("System Service - Error during payment: " + e.getMessage());
            return new Response<String>(null, "Error during payment: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        try {
            this.deliveryService.deliver(country, address, recipient, packageDetails);
            logger.info("System Service - User " + userId + " cart delivered to: " + recipient + " at address: " + address);

        } catch (Exception e) {
            this.paymentService.refund(cardNumber,totalPrice);
            logger.error("System Service - Error during delivery: " + e.getMessage());
            logger.info("System Service - User " + userId + " cart purchase failed, refund issued to: " + cardHolder + " at card number: " + cardNumber);
        }
        //update quantity in store
        try{
            Map<Integer,Map<Integer,Integer>> newCart = convertStoreCartToUserCatt(validCart);
            this.storeService.decrementProductsQuantity(newCart,userId);
            logger.info("System Service - User " + userId + " cart purchased successfully, products quantity updated in store");
        }
        catch (Exception e) {
            logger.error("System Service - Error during updating products quantity in store: " + e.getMessage());
            return new Response<String>(null, "Error during updating products quantity in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
        this.orderService.addOrderCart(validCart,prices, userId, address, paymentMethod);
        return new Response<String>("Cart purchased successfully", "Cart purchased successfully", true, null, null);

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
            return new Response<>(null, "Error during sending response for auction: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
                for (Integer storeId : product.getStoresIds()) {
                    try {
                        StoreProductDTO storeProduct = storeService.getProductFromStore(product.getId(), storeId);
                        // Only add products that have ratings
                        if (!Double.isNaN(storeProduct.getAverageRating())) {
                            ratedProducts.add(storeProduct);
                        }
                    } catch (Exception e) {
                        // Skip if product not found in store or other errors
                        logger.warn("Could not retrieve product " + product.getId() + " from store " + storeId + ": " + e.getMessage());
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
            return new Response<>(null, "Error fetching top rated products: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    private OrderDTO createOrderDTO(IOrder order) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        for (int productId : order.getProductIds()) {
            ProductDTO productDTO = this.productService.viewProduct(productId);
            productDTOS.add(productDTO);
        }
        return new OrderDTO(order.getId(), order.getUserId(), order.getStoreId(), productDTOS,
                order.getState().toString(), order.getAddress(), order.getPaymentMethod().toString());

    }

    @Override
    public Response<List<OrderDTO>> getAllStoreOrders(int storeId, int userId){
        try{
            logger.info("System service - user " + userId + " trying to get all orders from " + storeId);
            if(!storeService.canViewOrders(storeId, userId)){
                return new Response<List<OrderDTO>>(null, "user " + userId + " has insufficient permissions to view orders from store " + storeId, false, ErrorType.INVALID_INPUT, null);
            }
            List<IOrder> storeOrders = orderService.getOrdersByStoreId(storeId);
            List<OrderDTO> storeOrdersDTOs = new ArrayList<>();
            for(IOrder order : storeOrders){
                storeOrdersDTOs.add(createOrderDTO(order));
            }
            return new Response<List<OrderDTO>>(storeOrdersDTOs, "success", true, null, null);
        }
        catch(Exception e){
            return new Response<List<OrderDTO>>(null, e.getMessage(), false,ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> acceptAssignment(int storeId, int userId){
        try{
            logger.info("system service - user " + userId + " trying to accept assignment for store " + storeId);
            storeService.acceptAssignment(storeId, userId);
            return new Response<String>("success", "success", true, null, null);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        }
        catch(Exception e){
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<String> declineAssignment(int storeId, int userId){
        try{
            logger.info("system service - user " + userId + " trying to decline assignment for store " + storeId);
            storeService.declineAssignment(storeId, userId);
            return new Response<String>("success", "success", true, null, null);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        }
        catch(Exception e){
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<List<Integer>> getPendingOwners(int storeId, int requesterId){
        try{
            logger.info("system service - user " + requesterId + " trying to get pending owners for store " + storeId);
            List<Integer> pending = storeService.getPendingOwners(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true, null, null);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        }
        catch(Exception e){
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<List<Integer>> getPendingManagers(int storeId, int requesterId){
        try{
            logger.info("system service - user " + requesterId + " trying to get pending managers for store " + storeId);
            List<Integer> pending = storeService.getPendingManagers(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true, null, null);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        }
        catch(Exception e){
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    // county Validation method
    private boolean isValidCountryCode(String code) {
        String[] isoCountries = Locale.getISOCountries();
        return Arrays.asList(isoCountries).contains(code);
    }


    public Response<Boolean> deleteOrder(int orderId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(false, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            int userId = this.authenticatorService.getUserId(token);
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
    public Response<OrderDTO> viewOrder(int orderId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            int userId = this.authenticatorService.getUserId(token);
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
    public Response<List<OrderDTO>> searchOrders(String keyword, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            int userId = this.authenticatorService.getUserId(token);
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
    public Response<List<OrderDTO>> getOrdersByStoreId(int storeId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT, null);
            }
            int userId = this.authenticatorService.getUserId(token);
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
            return new Response<>(null, "Error during getting orders by store id", false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    @Override
    public Response<AbstractMap.SimpleEntry<UserDTO, String>> login(String email, String password){
        try{
            // Check if the user exists first
            Optional<Registered> optionalUser = userService.getUserByUserName(email);
            if (optionalUser.isPresent()) {
                Registered user = optionalUser.get();
                // Check if the user is suspended before login
                if (userService.isUserSuspended(user.getUserId())) {
                    logger.error("System Service - Login failed: User is suspended: " + email);
                    return new Response<>(null, "Login failed: User is suspended", false, ErrorType.INVALID_INPUT, null);
                }
            }
            
            String token = this.authenticatorService.login(email, password);
            UserDTO user = this.userService.login(email, password);
            return new Response<>(new AbstractMap.SimpleEntry<>(user, token), "Successful Login", true, null, null);
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
                logger.error("System Service - Unauthorized attempt to suspend user: Admin privileges required for user ID " + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            userService.suspendUser(requesterId, userId, endOfSuspension);
            
            if (endOfSuspension == null) {
                logger.info("System Service - User ID " + userId + " permanently suspended by admin ID " + requesterId);
                return new Response<>(null, "User permanently suspended", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId + " suspended until " + endOfSuspension + " by admin ID " + requesterId);
                return new Response<>(null, "User suspended until " + endOfSuspension, true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error during suspension: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during suspension: " + e.getMessage());
            return new Response<>(null, "Error during suspension: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Boolean> unsuspendUser(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to unsuspend user: Admin privileges required for user ID " + requesterId);
                return new Response<>(false, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            boolean wasUnsuspended = userService.unsuspendUser(requesterId, userId);
            
            if (wasUnsuspended) {
                logger.info("System Service - User ID " + userId + " unsuspended by admin ID " + requesterId);
                return new Response<>(true, "User unsuspended successfully", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId + " was not suspended (unsuspend request by admin ID " + requesterId + ")");
                return new Response<>(false, "User was not suspended", true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error during unsuspension: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during unsuspension: " + e.getMessage());
            return new Response<>(false, "Error during unsuspension: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Boolean> isUserSuspended(int userId) {
        try {
            boolean isSuspended = userService.isUserSuspended(userId);
            logger.info("System Service - Checked suspension status for User ID " + userId + ": " + (isSuspended ? "Suspended" : "Not suspended"));
            return new Response<>(isSuspended, "Suspension status checked successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error checking suspension status: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error checking suspension status: " + e.getMessage());
            return new Response<>(false, "Error checking suspension status: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<LocalDate> getSuspensionEndDate(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to get suspension end date: Admin privileges required for user ID " + requesterId);
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
            return new Response<>(null, "Error getting suspension end date: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<List<Registered>> getAllSuspendedUsers(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to get suspended users: Admin privileges required for user ID " + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            List<Registered> suspendedUsers = userService.getAllSuspendedUsers(requesterId);
            
            logger.info("System Service - Retrieved " + suspendedUsers.size() + " suspended users (requested by admin ID " + requesterId + ")");
            return new Response<>(suspendedUsers, suspendedUsers.size() + " suspended users found", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error retrieving suspended users: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error retrieving suspended users: " + e.getMessage());
            return new Response<>(null, "Error retrieving suspended users: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Integer> cleanupExpiredSuspensions(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to cleanup suspensions: Admin privileges required for user ID " + requesterId);
                return new Response<>(-1, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            int removedCount = userService.cleanupExpiredSuspensions(requesterId);
            
            logger.info("System Service - Cleaned up " + removedCount + " expired suspensions (requested by admin ID " + requesterId + ")");
            return new Response<>(removedCount, "Cleaned up " + removedCount + " expired suspensions", true, null, null);
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
                logger.error("System Service - Unauthorized attempt to add system admin: Admin privileges required for user ID " + requesterId);
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
            return new Response<>(null, "Error adding system admin: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Boolean> removeSystemAdmin(int requesterId, int userId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to remove system admin: Admin privileges required for user ID " + requesterId);
                return new Response<>(false, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            // Prevent removing yourself as an admin
            if (requesterId == userId) {
                logger.error("System Service - Admin ID " + requesterId + " attempted to remove themselves as admin");
                return new Response<>(false, "Cannot remove yourself as admin", false, ErrorType.INVALID_INPUT, null);
            }
            
            boolean wasRemoved = userService.removeSystemAdmin(userId);
            
            if (wasRemoved) {
                logger.info("System Service - User ID " + userId + " removed from system admins by admin ID " + requesterId);
                return new Response<>(true, "User removed from system admins successfully", true, null, null);
            } else {
                logger.info("System Service - User ID " + userId + " was not a system admin (remove request by admin ID " + requesterId + ")");
                return new Response<>(false, "User was not a system admin", true, null, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error removing system admin: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error removing system admin: " + e.getMessage());
            return new Response<>(false, "Error removing system admin: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Boolean> isSystemAdmin(int userId) {
        try {
            boolean isAdmin = userService.isSystemAdmin(userId);
            logger.info("System Service - Checked admin status for User ID " + userId + ": " + (isAdmin ? "Admin" : "Not admin"));
            return new Response<>(isAdmin, "Admin status checked successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error checking admin status: " + e.getMessage());
            return new Response<>(false, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error checking admin status: " + e.getMessage());
            return new Response<>(false, "Error checking admin status: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<List<Registered>> getAllSystemAdmins(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to get system admins: Admin privileges required for user ID " + requesterId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            List<Registered> admins = userService.getAllSystemAdmins();
            
            logger.info("System Service - Retrieved " + admins.size() + " system admins (requested by admin ID " + requesterId + ")");
            return new Response<>(admins, admins.size() + " system admins found", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error retrieving system admins: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error retrieving system admins: " + e.getMessage());
            return new Response<>(null, "Error retrieving system admins: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Integer> getSystemAdminCount(int requesterId) {
        try {
            if (!userService.isSystemAdmin(requesterId)) {
                logger.error("System Service - Unauthorized attempt to get admin count: Admin privileges required for user ID " + requesterId);
                return new Response<>(-1, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            int count = userService.getSystemAdminCount();
            
            logger.info("System Service - Current system admin count: " + count + " (requested by admin ID " + requesterId + ")");
            return new Response<>(count, "Current system admin count: " + count, true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Error getting admin count: " + e.getMessage());
            return new Response<>(-1, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error getting admin count: " + e.getMessage());
            return new Response<>(-1, "Error getting admin count: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    private boolean isAuth(String token){
        return this.authenticatorService.isValid(token);
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
                return new Response<>(null, "Invalid category", false, ErrorType.INVALID_INPUT, null);
            }
                List<ProductDTO> products = this.productService.getProductsByCategory(categoryEnum);
                return new Response<>(products, "Products retrieved successfully", true, null, null);

        } catch (Exception e) {
            logger.error("System Service - Error during searching products by category: " + e.getMessage());
            return new Response<>(null, "Error during searching products by category", false, ErrorType.INTERNAL_ERROR, null);
        }
    }

    @Override
    public Response<Void> userLogout(int userID) {
        try {
            logger.info("System service - user " + userID + " trying to logout");
            Optional<Registered> user = this.userService.getUserById(userID);

            if(user == null) {
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
            return new Response<>(null, "Error during logout: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    


    // Unsigned (guest) user management methods
    
    @Override
    public Response<Void> addUnsignedUser(User user) {
        try {
            userService.addUnsignedUser(user);
            logger.info("System Service - Added unsigned user with ID: " + user.getUserId());
            return new Response<>(null, "Unsigned user added successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to add unsigned user: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during adding unsigned user: " + e.getMessage());
            return new Response<>(null, "Error adding unsigned user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<User> getUnsignedUserById(int userId) {
        try {
            Optional<User> optionalUser = userService.getUnsignedUserById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                logger.info("System Service - Retrieved unsigned user with ID: " + userId);
                return new Response<>(user, "Unsigned user retrieved successfully", true, null, null);
            } else {
                logger.error("System Service - Unsigned user not found: " + userId);
                return new Response<>(null, "Unsigned user not found", false, ErrorType.INVALID_INPUT, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get unsigned user: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting unsigned user: " + e.getMessage());
            return new Response<>(null, "Error getting unsigned user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<List<User>> getAllUnsignedUsers(int adminId) {
        try {
            if (!userService.isSystemAdmin(adminId)) {
                logger.error("System Service - Unauthorized attempt to get all unsigned users: Admin privileges required for user ID " + adminId);
                return new Response<>(null, "Admin privileges required", false, ErrorType.INVALID_INPUT, null);
            }
            
            List<User> users = userService.getAllUnsignedUsers();
            logger.info("System Service - Retrieved " + users.size() + " unsigned users");
            return new Response<>(users, "Retrieved " + users.size() + " unsigned users", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to get all unsigned users: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during getting all unsigned users: " + e.getMessage());
            return new Response<>(null, "Error getting all unsigned users: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Void> updateUnsignedUser(User user) {
        try {
            userService.updateUnsignedUser(user);
            logger.info("System Service - Updated unsigned user with ID: " + user.getUserId());
            return new Response<>(null, "Unsigned user updated successfully", true, null, null);
        } catch (IllegalArgumentException e) {
            logger.error("System Service - Failed to update unsigned user: " + e.getMessage());
            return new Response<>(null, e.getMessage(), false, ErrorType.INVALID_INPUT, null);
        } catch (Exception e) {
            logger.error("System Service - Error during updating unsigned user: " + e.getMessage());
            return new Response<>(null, "Error updating unsigned user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
            return new Response<>(false, "Error removing unsigned user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
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
            return new Response<>(false, "Error checking if user is unsigned: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
    
    @Override
    public Response<Integer> getUnsignedUserCount(int adminId) {
        try {
            if (!userService.isSystemAdmin(adminId)) {
                logger.error("System Service - Unauthorized attempt to get unsigned user count: Admin privileges required for user ID " + adminId);
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
            return new Response<>(null, "Error getting unsigned user count: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR, null);
        }
    }
}
