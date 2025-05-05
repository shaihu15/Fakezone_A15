package ApplicationLayer.Services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ApplicationLayer.DTO.*;
import ApplicationLayer.Response;


import InfrastructureLayer.Repositories.StoreRepository;
import javassist.bytecode.LineNumberAttribute.Pc;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Interfaces.*;
import DomainLayer.Model.*;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import DomainLayer.Interfaces.IOrder;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import DomainLayer.Model.StoreFounder;
import DomainLayer.Model.StoreManager;
import DomainLayer.Model.StoreOwner;

import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;

import org.springframework.context.ApplicationEventPublisher;
import ApplicationLayer.DTO.StoreRolesDTO;

import java.util.Arrays;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;


import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;

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
                return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT);
            }
            if (this.userService.isUserLoggedIn(userId)) {
                logger.info("System Service - User is logged in: " + userId);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            product = this.storeService.decrementProductQuantity(productId, storeId, quantity);
        } catch (Exception e) {
            logger.error("System Service - Error during adding to basket: " + e.getMessage());
            return new Response<>(null, "Error during adding to basket: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        this.userService.addToBasket(userId, storeId, product);
        logger.info(
                "System Service - User added product to basket: " + productId + " from store: " + storeId + " by user: "
                        + userId + " with quantity: " + quantity);
        return new Response<>(null, "Product added to basket successfully", true);
    }

    @Override
    public Response<Void> ratingStore(int storeId, int userId, double rating, String comment) {
        try {
            if (this.userService.didPurchaseStore(userId, storeId)) {
                this.storeService.addStoreRating(storeId, userId, rating, comment);
                logger.info("System Service - User rated store: " + storeId + " by user: " + userId + " with rating: " + rating);
                return new Response<>(null, "Store rated successfully", true);
            } else {
                logger.error("System Service - User did not purchase from this store: " + userId + " " + storeId);
                return new Response<>(null, "User did not purchase from this store", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating store: " + e.getMessage());
            return new Response<>(null, "Error during rating store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> ratingStoreProduct(int storeId, int productId, int userId, double rating, String comment) {
        try {
            if (this.userService.didPurchaseProduct(userId, storeId, productId)) {
                this.storeService.addStoreProductRating(storeId, productId, userId, rating, comment);
                logger.info("System Service - User rated product: " + productId + " in store: " + storeId + " by user: " + userId + " with rating: " + rating);
                return new Response<>(null, "Product rated successfully", true);
            } else {
                logger.error("System Service - User did not purchase from this product: " + userId + " " + storeId + " " + productId);
                return new Response<>(null, "User did not purchase from this product", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during rating product: " + e.getMessage());
            return new Response<>(null, "Error during rating product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    public Response<StoreDTO> userAccessStore(String token, int storeId) {
        try {
            logger.info("System Service - User accessed store: " + storeId + " by user with token " + token);
            
            if (this.authenticatorService.isValid(token)){
                logger.info("System Service - Token is valid: " + token);
            }else {
                logger.error("System Service - Token is not valid: " + token);
                return new Response<StoreDTO>(null, "Token is not valid", false, ErrorType.INVALID_INPUT);
            }
            StoreDTO s = this.storeService.viewStore(storeId);
            if (s.isOpen()) {
                return new Response<StoreDTO>(s, "Store retrieved successfully", true);
            }
            logger.error("System Service - Store is closed: " + storeId);
            return new Response<StoreDTO>(null, "Store is closed", false, ErrorType.INVALID_INPUT);

        } catch (Exception e) {
            // Handle exception if needed
            logger.error("System Service - Error during user access store: " + e.getMessage());
            return new Response<StoreDTO>(null, "Error during user access store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> addStore(int userId, String storeName) {
        try {
            if (this.userService.isUserLoggedIn(userId)) {
                int storeId = this.storeService.addStore(userId, storeName);
                this.userService.addRole(userId, storeId, new StoreFounder());
                logger.info("System Service - User opened store: " + storeId + " by user: " + userId + " with name: " + storeName);
                return new Response<>(null, "Store opened successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during opening store: " + e.getMessage());
            return new Response<>(null, "Error during opening store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
                    return new Response<>(null, "Message sent successfully", true);
                } else {
                    logger.error("System Service - Store is closed: " + storeId);
                    return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT);
                }
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to store: " + e.getMessage());
            return new Response<>(null, "Error during sending message to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
                    return new Response<>(null, "Message sent successfully", true);
                } 
                else {
                    logger.error("System Service - Store is closed: " + storeId);
                    return new Response<>(null, "Store is closed", false, ErrorType.INVALID_INPUT);
                }
            } else {
                logger.error("System Service - User is not logged in: " + managerId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending message to user: " + e.getMessage());
            return new Response<>(null, "Error during sending message to user: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    public LocalDate parseDate(String dateString) {
    try {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
    } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid date format: " + dateString);
    }
}

    @Override
    public Response<StoreProductDTO> getProductFromStore(int productId, int storeId) {
        try {
            logger.info("System service - user trying to view procuct " + productId + " in store: " + storeId);
            StoreDTO s = this.storeService.viewStore(storeId);
            return new Response<StoreProductDTO>(s.getStoreProductById(productId), "Product retrieved successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during getting product: " + e.getMessage());
            return new Response<StoreProductDTO>(null, "Error during getting product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
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
    public Response<String> guestRegister(String email, String password, String dateOfBirth, String country) {
        logger.info("System service - user trying to register " + email);
        LocalDate dateOfBirthLocalDate;
        try {
            dateOfBirthLocalDate = parseDate(dateOfBirth);
        } catch (Exception e) {
            logger.error("System Service - Error during guest registration: " + e.getMessage());
            return new Response<>(null, "Invalid date of birth format. Expected format: YYYY-MM-DD", false, ErrorType.INVALID_INPUT);
        }
        if(isValidCountryCode(country)) {
            logger.info("System Service - Country code is valid: " + country);
        } else {
            logger.error("System Service - Invalid country code: " + country);
            return new Response<>(null, "Invalid country code", false, ErrorType.INVALID_INPUT);
        }
        String token = this.authenticatorService.register(email, password, dateOfBirthLocalDate, country);
        if (token == null) {
            logger.error("System Service - Error during guest registration: " + email);
            return new Response<>(null, "Error during guest registration", false, ErrorType.INTERNAL_ERROR);
        } else {
            logger.info("System service - user registered successfully " + email);
            return new Response<>(token, "Guest registered successfully", true);
        }
        
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

    //addProduct method should be with amount and store?
    private Response<Integer> addProduct(String productName, String productDescription, String category) {
        try {
            if (productName == null || productDescription == null || category == null) {
                logger.error("System Service - Invalid input: " + productName + " " + productDescription + " " + category);
                return new Response<>(-1, "Invalid input", false, ErrorType.INVALID_INPUT);
            }
            PCategory categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(-1, "Invalid category", false, ErrorType.INVALID_INPUT);
            }
            logger.info("System service - user trying to add procuct " + productName);
            int productId = this.productService.addProduct(productName, productDescription,categoryEnum);
            return new Response<>(productId, "Product added successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during adding product: " + e.getMessage());
            return new Response<>(-1, "Error during adding product: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
                return new Response<>(storeRolesDTO, "Store roles retrieved successfully", true);

            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting store roles: " + e.getMessage());
            return new Response<>(null, "Error during getting store roles: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> addStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("Systrem service - user sessionToken: " + sessionToken + " trying to add permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            if (this.authenticatorService.isValid(sessionToken)) {
                int requesterId = this.authenticatorService.getUserId(sessionToken);
                storeService.addStoreManagerPermissions(storeId, requesterId, managerId, perms);
                return new Response<>(null, "Permissions added successfully", true);
            } else {
                return new Response<>(null, "Invalid session token: " + sessionToken, false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            return new Response<>(null, "Error during adding store manager permissions: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    //maby add details to response
    @Override
    public Response<Void> removeStoreManagerPermissions(int storeId, String sessionToken, int managerId,
            List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user sessionToken: " + sessionToken + " trying to remove permissions: "
                    + perms.toString() + " to manager: " + managerId + " in store: " + storeId);
            int requesterId = this.authenticatorService.getUserId(sessionToken);
            storeService.removeStoreManagerPermissions(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Permissions removed successfully", true);
        } catch (Exception e) {
            return new Response<>(null, "Error during removing store manager permissions: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            storeService.removeStoreManager(storeId, requesterId, managerId);
            return new Response<>(null, "Store manager removed successfully", true);
        } catch (Exception e) {
            logger.error("System service - removeStoreManager failed" + e.getMessage());
            userService.addRole(managerId, storeId, new StoreManager()); // reverting
            return new Response<>(null, "Error during removing store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> removeStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to remove owner " + ownerId + " from store: " + storeId);
            userService.removeRole(ownerId, storeId);
        } catch (Exception e) {
            logger.error("System service - failed to remove StoreOwner role from user " + e.getMessage());
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            storeService.removeStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner removed successfully", true);
        } catch (Exception e) {
            logger.error("System service - removeStoreOwner failed" + e.getMessage());
            userService.addRole(ownerId, storeId, new StoreOwner()); // reverting
            return new Response<>(null, "Error during removing store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> addStoreManager(int storeId, int requesterId, int managerId, List<StoreManagerPermission> perms) {
        try {
            logger.info("System service - user " + requesterId + " trying to add manager " + managerId + " to store: " + storeId);
            userService.addRole(managerId, storeId, new StoreManager());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreManager role to user " + e.getMessage());
            return new Response<>(null, "Error during adding store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);}
        try {
            storeService.addStoreManager(storeId, requesterId, managerId, perms);
            return new Response<>(null, "Store manager added successfully", true);
        } catch (Exception e) {
            logger.error("System service - failed to add manager to store " + e.getMessage());
            userService.removeRole(managerId, storeId); // reverting
            return new Response<>(null, "Error during adding store manager: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> addStoreOwner(int storeId, int requesterId, int ownerId) {
        try {
            logger.info("System service - user " + requesterId + " trying to add owner " + ownerId + " to store: " + storeId);
            userService.addRole(ownerId, storeId, new StoreOwner());
        } catch (Exception e) {
            logger.error("System service - failed to add StoreOwner role to user " + e.getMessage());
            return new Response<>(null, "Error during adding store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            storeService.addStoreOwner(storeId, requesterId, ownerId);
            return new Response<>(null, "Store owner added successfully", true);
        } catch (Exception e) {
            logger.error("System service - failed to add owner to store " + e.getMessage());
            userService.removeRole(ownerId, storeId); // reverting
            return new Response<>(null, "Error during adding store owner: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    
    @Override
    public Response<Void> addAuctionProductToStore(int storeId, int requesterId, int productID, double basePrice, int daysToEnd) {
        try {
            logger.info("System service - user " + requesterId + " trying to add auction product " + productID + " to store: " + storeId);
            this.storeService.addAuctionProductToStore(storeId, requesterId, productID, basePrice, daysToEnd);
            return new Response<>(null, "Auction product added successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during adding auction product to store: " + e.getMessage());
            return new Response<>(null, "Error during adding auction product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    
    @Override
    public Response<Void> addBidOnAuctionProductInStore(int storeId, int requesterId, int productID, double bid) {
        try {
            logger.info("System service - user " + requesterId + " trying to add bid " + bid + " to auction product " + productID + " in store: " + storeId);
            this.storeService.addBidOnAuctionProductInStore(storeId, requesterId, productID, bid);
            return new Response<>(null, "Bid added successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error during adding bid to auction product in store: " + e.getMessage());
            return new Response<>(null, "Error during adding bid to auction product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
    public Response<Void> addProductToStore(int storeId, int requesterId, int productId, double basePrice, int quantity, String category) {
        String name = null;
        try{
            logger.info("System service - user " + requesterId + " trying to add product " + productId + " to store " + storeId);
            name = productService.viewProduct(productId).getName();
        }
        catch (Exception e){
            logger.error("System service - failed to fetch product " + e.getMessage());
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try{
            PCategory categoryEnum = isCategoryValid(category);
            if (categoryEnum == null) {
                logger.error("System Service - Invalid category: " + category);
                return new Response<>(null, "Invalid category", false, ErrorType.INVALID_INPUT);
            }
            storeService.addProductToStore(storeId, requesterId, productId, name, basePrice, quantity, categoryEnum);
            return new Response<>(null, "Product added to store successfully", true);
        }
        catch (Exception e){
            logger.error("System service - failed to add product to store " + e.getMessage());
            return new Response<>(null, "Error during adding product to store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try{
            storeService.updateProductInStore(storeId, requesterId, productId, name, basePrice, quantity);
            return new Response<>(null, "Product updated in store successfully", true);
        }
        catch (Exception e){
            logger.error("System service - failed to update product in store " + e.getMessage());
            return new Response<>(null, "Error during updating product in store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> removeProductFromStore(int storeId, int requesterId, int productId){
        try {
            logger.info("System service - user " + requesterId + " trying to remove product " + productId + " from store " + storeId);
            storeService.removeProductFromStore(storeId, requesterId, productId);
            return new Response<>(null, "Product removed from store successfully", true);
        } 
        catch (Exception e) {
            logger.info("System service - failed to remove product from store " + e.getMessage());
            return new Response<>(null, "Error during removing product from store: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Void> addStoreAuctionProductDays(int storeId, int requesterId, int productId, int daysToAdd){
        try{
            logger.info("System service - user " + requesterId + " trying to add store auction product days");
            storeService.addStoreAuctionProductDays(storeId, requesterId, productId, daysToAdd);
            return new Response<>(null, "Product auction days added successfully", true);}
        catch (Exception e) {
            logger.info("System service - failed to add auction product days  " + e.getMessage());
            return new Response<>(null, "Error during adding auction product days: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<List<StoreProductDTO>> viewCart(int userId) {
        try {
            logger.info("System service - user " + userId + " trying to view cart");
            if (this.userService.isUserLoggedIn(userId)) {
                List<StoreProductDTO> cart = this.userService.viewCart(userId);
                return new Response<>(cart, "Cart retrieved successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during viewing cart: " + e.getMessage());
            return new Response<>(null, "Error during viewing cart: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

	@Override
	public Response<HashMap<Integer, String>> getAllMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAllMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
	}

	@Override
	public Response<HashMap<Integer, String>> getAssignmentMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAssignmentMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }	}

	@Override
	public Response<HashMap<Integer, String>> getAuctionEndedtMessages(int userID) {
		try{
            if (this.userService.isUserLoggedIn(userID)) {
                return this.userService.getAuctionEndedtMessages(userID);
            } else {
                logger.error("System Service - User is not logged in: " + userID);
                return new Response<HashMap<Integer, String>>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during getting all messages: " + e.getMessage());
            return new Response<HashMap<Integer, String>>(null, "Error during getting all messages: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    	}

    @Override
    public Response<String> purchaseCart(int userId, String country, LocalDate dob, PaymentMethod paymentMethod, String deliveryMethod,
            String cardNumber, String cardHolder, String expDate, String cvv, String address,
            String recipient, String packageDetails) {
        double price = 0;
        Cart cart = null;
        try{
            logger.info("System service - user " + userId + " trying to purchase cart");
            cart = this.userService.getUserCart(userId);
            if(cart.getAllProducts().isEmpty()){
                logger.error("System Service - Cart is empty: " + userId);
                return new Response<String>(null, "Cart is empty", false, ErrorType.INVALID_INPUT);
            }
            if(isValidCountryCode(country)) {
                logger.info("System Service - Country code is valid: " + country);
            } else {
                logger.error("System Service - Invalid country code: " + country);
                return new Response<String>(null, "Invalid country code", false, ErrorType.INVALID_INPUT);
            }
            Optional<User> user = this.userService.getAnyUserById(userId);
            if(!user.isPresent()){
                logger.error("System Service - User not found: " + userId);
                return new Response<String>(null, "User not found", false, ErrorType.INVALID_INPUT);
            }
            price = this.storeService.calcAmount(cart,dob);
           logger.info("System Service - User "+userId + "cart price: " + price);

        }
        catch (Exception e) {
            logger.error("System Service - Error during purchase cart: " + e.getMessage());
            return new Response<String>(null, "Error during purchase cart: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            this.paymentService.pay(cardNumber, cardHolder, expDate, cvv, price);
            logger.info("System Service - User " + userId + " cart purchased successfully, payment method: " + paymentMethod);
        } catch (Exception e) {
            logger.error("System Service - Error during payment: " + e.getMessage());
            return new Response<String>(null, "Error during payment: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
        try {
            this.deliveryService.deliver(country, address, recipient, packageDetails);
            logger.info("System Service - User " + userId + " cart delivered to: " + recipient + " at address: " + address);

        } catch (Exception e) {
            this.paymentService.refund(cardNumber,price);
            logger.error("System Service - Error during delivery: " + e.getMessage());
            logger.info("System Service - User " + userId + " cart purchase failed, refund issued to: " + cardHolder + " at card number: " + cardNumber);
        }
        this.orderService.addOrderCart(cart, userId, address, paymentMethod);
        return new Response<String>("Cart purchased successfully", "Cart purchased successfully", true);

    }

    @Override
    public Response<String> sendResponseForAuctionByOwner(int storeId, int requesterId, int productId, boolean accept) {
        try {
            if (this.userService.isUserLoggedIn(requesterId)) {
                this.storeService.sendResponseForAuctionByOwner(storeId, requesterId, productId, accept);
                logger.info("System Service - User sent response for auction: " + productId + " in store: " + storeId
                        + " by user: " + requesterId + " with accept: " + accept);
                return new Response<>("Response sent successfully", "Response sent successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + requesterId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during sending response for auction: " + e.getMessage());
            return new Response<>(null, "Error during sending response for auction: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
            
            return new Response<>(topRatedProducts, "Top rated products retrieved successfully", true);
        } catch (Exception e) {
            logger.error("System Service - Error while fetching top rated products: " + e.getMessage());
            return new Response<>(null, "Error fetching top rated products: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
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
                return new Response<List<OrderDTO>>(null, "user " + userId + " has insufficient permissions to view orders from store " + storeId, false, ErrorType.INVALID_INPUT);
            }
            List<IOrder> storeOrders = orderService.getOrdersByStoreId(storeId);
            List<OrderDTO> storeOrdersDTOs = new ArrayList<>();
            for(IOrder order : storeOrders){
                storeOrdersDTOs.add(createOrderDTO(order));
            }
            return new Response<List<OrderDTO>>(storeOrdersDTOs, "success", true);
        }
        catch(Exception e){
            return new Response<List<OrderDTO>>(null, e.getMessage(), false,ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<String> acceptAssignment(int storeId, int userId){
        try{
            logger.info("system service - user " + userId + " trying to accept assignment for store " + storeId);
            storeService.acceptAssignment(storeId, userId);
            return new Response<String>("success", "success", true);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT);
        }
        catch(Exception e){
            logger.error("system service - acceptAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<String> declineAssignment(int storeId, int userId){
        try{
            logger.info("system service - user " + userId + " trying to decline assignment for store " + storeId);
            storeService.declineAssignment(storeId, userId);
            return new Response<String>("success", "success", true);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INVALID_INPUT);
        }
        catch(Exception e){
            logger.error("system service - declineAssignment failed: " + e.getMessage());
            return new Response<String>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    
    @Override
    public Response<List<Integer>> getPendingOwners(int storeId, int requesterId){
        try{
            logger.info("system service - user " + requesterId + " trying to get pending owners for store " + storeId);
            List<Integer> pending = storeService.getPendingOwners(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT);
        }
        catch(Exception e){
            logger.error("system service - getPendingOwners failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<List<Integer>> getPendingManagers(int storeId, int requesterId){
        try{
            logger.info("system service - user " + requesterId + " trying to get pending managers for store " + storeId);
            List<Integer> pending = storeService.getPendingManagers(storeId, requesterId);
            return new Response<List<Integer>>(pending, "success", true);
        }
        catch(IllegalArgumentException e){
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INVALID_INPUT);
        }
        catch(Exception e){
            logger.error("system service - getPendingManagers failed: " + e.getMessage());
            return new Response<List<Integer>>(null, e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }
    // county Validation method
    private boolean isValidCountryCode(String code) {
        String[] isoCountries = Locale.getISOCountries();
        return Arrays.asList(isoCountries).contains(code);
    }

    @Override
    public Response<Integer> addOrder(int userId, BasketDTO basketDTO, String address, String paymentMethod, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(-1, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            logger.info("System service - user " + userId + " trying to add order to store " + basketDTO.getStoreId());
            if (this.userService.isUserLoggedIn(userId)) {
                for (Integer productId : basketDTO.getProducts().stream().map(product -> product.getProductId()).toList()) {
                    StoreProductDTO storeProduct = this.storeService.getProductFromStore(basketDTO.getStoreId(), productId);
                    if (storeProduct == null) {
                        logger.error("System Service - Product not found in store: " + productId + " in store: " + basketDTO.getStoreId());
                        return new Response<>(null, "Product not found in store", false, ErrorType.INVALID_INPUT);
                    }

                }
                PaymentMethod payment = PaymentMethod.valueOf(paymentMethod);
                int orderId = this.orderService.addOrder(new Basket(basketDTO.getStoreId(), basketDTO.getProducts()), userId, address, payment);
                logger.info("System service - order " + orderId + " added successfully");
                return new Response<>(orderId, "Order added successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during adding order: " + e.getMessage());
            return new Response<>(null, "Error during adding order: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<Integer> updateOrder(int orderId, BasketDTO basket, Integer userId, String address, String paymentMethod, String token){
        try {
            if(!this.isAuth(token)){
                return new Response<>(-1, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            logger.info("System service - user " + userId + " trying to update order " + orderId);
            List<Integer> products =  basket.getProducts().stream().map(product -> product.getProductId()).toList();
            if (this.userService.isUserLoggedIn(userId)) {
                for(Integer id : products){
                    IProduct product = this.productService.getProduct(id);
                    if (product == null) {
                        logger.error("System Service - Product not found: " + id);
                        return new Response<>(null, "Product not found", false, ErrorType.INVALID_INPUT);
                    }
                }
                int updatedOrderId = this.orderService.updateOrder(orderId, new Basket(basket.getStoreId(), basket.getProducts()), userId, address, PaymentMethod.valueOf(paymentMethod));
                return new Response<>(updatedOrderId, "Order updated successfully", true);
            } else {
                logger.error("System Service - User is not logged in: " + userId);
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
        } catch (Exception e) {
            logger.error("System Service - Error during updating order: " + e.getMessage());
            return new Response<>(null, "Error during updating order: " + e.getMessage(), false, ErrorType.INTERNAL_ERROR);
        }
    }

    public Response<Boolean> deleteOrder(int orderId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(false, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            int userId = this.authenticatorService.getUserId(token);
            if(this.userService.isUserLoggedIn(userId)) {
                this.orderService.deleteOrder(orderId);
                return new Response<>(true, "Order deleted successfully", true);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(false, "User is not logged in", false, ErrorType.INVALID_INPUT);

        } catch (Exception e) {
            logger.error("System Service - Error during deleting order: " + e.getMessage());
            return new Response<>(false, "Error during deleting order", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<OrderDTO> viewOrder(int orderId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            int userId = this.authenticatorService.getUserId(token);
            if(this.userService.isUserLoggedIn(userId)) {
                IOrder order = this.orderService.viewOrder(orderId);
                OrderDTO orderDTO = createOrderDTO(order);
                return new Response<>(orderDTO, "Order retrieved successfully", true);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);

        } catch (Exception e) {
            logger.error("System Service - Error during viewing order: " + e.getMessage());
            return new Response<>(null, "Error during viewing order", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<List<OrderDTO>> searchOrders(String keyword, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            int userId = this.authenticatorService.getUserId(token);
            if(this.userService.isUserLoggedIn(userId)) {
                List<IOrder> orders = this.orderService.searchOrders(keyword);
                List<OrderDTO> orderDTOS = new ArrayList<>();
                for (IOrder order : orders) {
                    OrderDTO orderDTO = createOrderDTO(order);
                    orderDTOS.add(orderDTO);
                }
                return new Response<>(orderDTOS, "Orders retrieved successfully", true);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);

        } catch (Exception e) {
            logger.error("System Service - Error during searching orders: " + e.getMessage());
            return new Response<>(null, "Error during searching orders", false, ErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public Response<List<OrderDTO>> getOrdersByStoreId(int storeId, String token) {
        try {
            if(!this.isAuth(token)){
                return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);
            }
            int userId = this.authenticatorService.getUserId(token);
            if(this.userService.isUserLoggedIn(userId)) {
                List<IOrder> orders = this.orderService.getOrdersByStoreId(storeId);
                List<OrderDTO> orderDTOS = new ArrayList<>();
                for (IOrder order : orders) {
                    OrderDTO orderDTO = createOrderDTO(order);
                    orderDTOS.add(orderDTO);
                }
                return new Response<>(orderDTOS, "Orders retrieved successfully", true);
            }
            logger.error("System Service - User is not logged in: " + userId);
            return new Response<>(null, "User is not logged in", false, ErrorType.INVALID_INPUT);

        } catch (Exception e) {
            logger.error("System Service - Error during getting orders by store id: " + e.getMessage());
            return new Response<>(null, "Error during getting orders by store id", false, ErrorType.INTERNAL_ERROR);
        }
    }

    private boolean isAuth(String token){
        return this.authenticatorService.isValid(token);
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
