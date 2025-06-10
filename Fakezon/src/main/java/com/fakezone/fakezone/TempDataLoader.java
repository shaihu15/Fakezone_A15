package com.fakezone.fakezone;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.StoreService;
import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
@Component
public class TempDataLoader implements ApplicationRunner {
    @Autowired
    private ISystemService systemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IStoreService storeService;

    private final String devEmail = "dev@fakezone.bgu.ac.il";
    private final String devPassword = "Devpass1";
    private final String devDob = "2000-01-01";
    private final String devCountry = "IL";

    @Override
    public void run(ApplicationArguments args) {
        try {
            Response<String> devRegisterResponse = systemService.guestRegister(devEmail, devPassword, devDob, devCountry);
            if (!devRegisterResponse.isSuccess()) {
                System.out.println("Dev user registration failed: " + devRegisterResponse.getMessage() + ". Proceeding to login (assuming user might already exist).");
            } else {
                System.out.println("Dev user registered successfully.");
            }

            Response<AbstractMap.SimpleEntry<UserDTO, String>> devLoginResponse = systemService.login(devEmail, devPassword);
            if (!devLoginResponse.isSuccess() || devLoginResponse.getData() == null || devLoginResponse.getData().getKey() == null || devLoginResponse.getData().getKey().getUserId() == 0) {
                throw new RuntimeException("Failed to login dev user: " + (devLoginResponse.getMessage() != null ? devLoginResponse.getMessage() : "Unknown error"));
            }
            UserDTO devUserDTO = devLoginResponse.getData().getKey();
            int devUserId = devUserDTO.getUserId();
            System.out.println("Logged in dev user (u1) with ID: " + devUserId);

            Response<Void> addSystemAdminResponse = systemService.addSystemAdmin(devUserId, devUserId);
            if (!addSystemAdminResponse.isSuccess()) {
                System.err.println("Failed to set dev user as system admin: " + addSystemAdminResponse.getMessage());
            } else {
                System.out.println("Dev user (u1) set as system admin.");
            }

            Map<String, UserDTO> loggedInUsers = new HashMap<>();
            Map<String, Integer> userIds = new HashMap<>();

            UserDTO u2DTO = registerAndLoginUserThroughSystemService("u2", "u2@fakezone.bgu.ac.il", "Passu2", "1995-02-02", "US");
            loggedInUsers.put("u2", u2DTO);
            userIds.put("u2", u2DTO.getUserId());

            UserDTO u3DTO = registerAndLoginUserThroughSystemService("u3", "u3@fakezone.bgu.ac.il", "Passu3", "1996-03-03", "CA");
            loggedInUsers.put("u3", u3DTO);
            userIds.put("u3", u3DTO.getUserId());

            UserDTO u4DTO = registerAndLoginUserThroughSystemService("u4", "u4@fakezone.bgu.ac.il", "Passu4", "1997-04-04", "GB");
            loggedInUsers.put("u4", u4DTO);
            userIds.put("u4", u4DTO.getUserId());

            UserDTO u5DTO = registerAndLoginUserThroughSystemService("u5", "u5@fakezone.bgu.ac.il", "Passu5", "1998-05-05", "DE");
            loggedInUsers.put("u5", u5DTO);
            userIds.put("u5", u5DTO.getUserId());

            UserDTO u6DTO = registerAndLoginUserThroughSystemService("u6", "u6@fakezone.bgu.ac.il", "Passu6", "1999-06-06", "FR");
            loggedInUsers.put("u6", u6DTO);
            userIds.put("u6", u6DTO.getUserId());

            int u2Id = userIds.get("u2");
            System.out.println("Current active user for store creation/product adding: u2 (ID: " + u2Id + ")");

            String s1Name = "s1";
            Response<Integer> s1Response = systemService.addStore(u2Id, s1Name);
            if (!s1Response.isSuccess()) {
                throw new RuntimeException("Failed to create store " + s1Name + ": " + s1Response.getMessage());
            }
            int s1Id = s1Response.getData();
            System.out.println("Opened store '" + s1Name + "' with ID: " + s1Id + " by u2 (ID: " + u2Id + ")");

            // --- Store s1: Bamba Product ---
            String bambaName = "Bamba";
            String bambaDescription = "Classic Israeli peanut snack";
            double bambaPrice = 30.0;
            int bambaQuantity = 20;
            Response<StoreProductDTO> addBambaResponse = systemService.addProductToStore(
                    s1Id,
                    u2Id,
                    bambaName,
                    bambaDescription,
                    bambaPrice,
                    bambaQuantity,
                    PCategory.FOOD.name()
            );
            if (!addBambaResponse.isSuccess()) {
                throw new RuntimeException("Failed to add Bamba to store " + s1Name + ": " + addBambaResponse.getMessage());
            }
            int bambaProductId = addBambaResponse.getData().getProductId();
            System.out.println("Added Bamba (Product ID: " + bambaProductId + ") to store " + s1Name + " with price " + bambaPrice + " and quantity " + bambaQuantity);

            // --- Store s1: Phone, Laptop, TV Products ---
            Map<String, String> productDetails = new HashMap<>();
            productDetails.put("Phone", "Smartphone with 6GB RAM");
            productDetails.put("Laptop", "Gaming laptop with RTX 4080");
            productDetails.put("TV", "65-inch 4K Smart TV");

            double initialPrice = 999.99; // Starting price for Phone
            Map<String, Integer> productIdsInStore = new HashMap<>(); // To store Product IDs for later purchase/rating

            for (Map.Entry<String, String> entry : productDetails.entrySet()) {
                String productName = entry.getKey();
                String description = entry.getValue();
                PCategory category = PCategory.ELECTRONICS;

                double price = initialPrice;

                Response<StoreProductDTO> addProductResponse = systemService.addProductToStore(
                        s1Id,
                        u2Id,
                        productName,
                        description,
                        price,
                        10, // Default quantity
                        category.name()
                );

                if (!addProductResponse.isSuccess()) {
                    System.err.println("Failed to add " + productName + " to store " + s1Name + ": " + addProductResponse.getMessage());
                    initialPrice -= 100.0;
                    continue;
                }

                int currentProductId = addProductResponse.getData().getProductId();
                productIdsInStore.put(productName, currentProductId); // Store product ID for later
                System.out.println("Added " + productName + " (Product ID: " + currentProductId + ") to store " + s1Name + " with price " + price + " and quantity 10.");

                initialPrice -= 100.0;
            }

            // --- Purchasing All Three New Products by u6 for Rating ---
            int u6Id = userIds.get("u6");
            System.out.println("\n--- Initiating purchase of Phone, Laptop, TV by u6 (ID: " + u6Id + ") to enable product ratings ---");

            // Add Phone, Laptop, TV to u6's shopping cart
            for (Map.Entry<String, Integer> entry : productIdsInStore.entrySet()) {
                String productName = entry.getKey();
                int productId = entry.getValue();
                Response<Void> addToCartResponse = systemService.addToBasket(u6Id, productId, s1Id, 1); // Buy 1 of each
                if (!addToCartResponse.isSuccess()) {
                    System.err.println("Failed to add " + productName + " to u6's cart: " + addToCartResponse.getMessage());
                } else {
                    System.out.println("Added " + productName + " (Product ID: " + productId + ") to u6's cart in store " + s1Id + ".");
                }
            }

            // Purchase all items in u6's cart
            Response<String> purchaseResponse = systemService.purchaseCart(u6Id, "FR", LocalDate.of(1999, 6, 6), PaymentMethod.CREDIT_CARD, "Standard", "123456789", "French user", "12/25", "123", "French Address*French-City*France*123456", "French Recipient", "French Package");
            if (!purchaseResponse.isSuccess()) {
                System.err.println("Failed to complete purchase for u6: " + purchaseResponse.getMessage());
            } else {
                System.out.println("u6 (ID: " + u6Id + ") successfully completed purchase from store " + s1Id + ".");

                // --- Now, Rate All Three Purchased Products by u6 ---
                System.out.println("\n--- Rating products by u6 (ID: " + u6Id + ") ---");
                double currentRating = 4.0; // Start ratings for purchased items from u6

                for (Map.Entry<String, Integer> entry : productIdsInStore.entrySet()) {
                    String productName = entry.getKey();
                    int productId = entry.getValue();
                    Response<Void> productRatingByU6 = systemService.ratingStoreProduct(s1Id, productId, u6Id, currentRating, "Good " + productName + "!");
                    if (!productRatingByU6.isSuccess()) {
                        System.err.println("Failed to rate " + productName + " by u6: " + productRatingByU6.getMessage());
                    } else {
                        System.out.println("u6 rated " + productName + " (Product ID " + productId + ") in store " + s1Id + " with " + currentRating + " stars.");
                    }
                    currentRating -= 0.5;
                }

                // --- u6 rates the store s1 ---
                Response<Void> storeRatingByU6 = systemService.ratingStore(s1Id, u6Id, 4, "A good store overall!");
                if (!storeRatingByU6.isSuccess()) {
                    System.err.println("Failed to rate store ID " + s1Id + " by u6: " + storeRatingByU6.getMessage());
                } else {
                    System.out.println("Store ID " + s1Id + " rated successfully by u6 (ID: " + u6Id + ").");
                }
            }

            // Appointing u3 as an administrator in store s1 with permissions to edit product inventory (by u2)
            int u3Id = userIds.get("u3");
            Response<Void> requestAppointU3ManagerResponse = systemService.addStoreManager(s1Id, u2Id, u3Id, Collections.singletonList(StoreManagerPermission.INVENTORY));
            if (!requestAppointU3ManagerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u3 as manager in store s1: " + requestAppointU3ManagerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u3 (ID: " + u3Id + ") as manager in store s1: " + s1Id + " by u2.");
                Response<String> acceptU3ManagerResponse = systemService.acceptAssignment(s1Id, u3Id);
                if (!acceptU3ManagerResponse.isSuccess()) {
                    System.err.println("u3 failed to accept manager assignment: " + acceptU3ManagerResponse.getMessage());
                } else {
                    System.out.println("u3 (ID: " + u3Id + ") successfully accepted manager assignment in store s1.");
                }
            }

            // Appointing u4 and u5 as owners of store s1 by u2
            int u4Id = userIds.get("u4");
            int u5Id = userIds.get("u5");

            Response<Void> requestAppointU4OwnerResponse = systemService.addStoreOwner(s1Id, u2Id, u4Id);
            if (!requestAppointU4OwnerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u4 as owner in store s1: " + requestAppointU4OwnerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u4 (ID: " + u4Id + ") as owner in store s1: " + s1Id + " by u2.");
                Response<String> acceptU4OwnerResponse = systemService.acceptAssignment(s1Id, u4Id);
                if (!acceptU4OwnerResponse.isSuccess()) {
                    System.err.println("u4 failed to accept owner assignment: " + acceptU4OwnerResponse.getMessage());
                } else {
                    System.out.println("u4 (ID: " + u4Id + ") successfully accepted owner assignment in store s1.");
                }
            }

            Response<Void> requestAppointU5OwnerResponse = systemService.addStoreOwner(s1Id, u2Id, u5Id);
            if (!requestAppointU5OwnerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u5 as owner in store s1: " + requestAppointU5OwnerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u5 (ID: " + u5Id + ") as owner in store s1: " + s1Id + " by u2.");
                Response<String> acceptU5OwnerResponse = systemService.acceptAssignment(s1Id, u5Id);
                if (!acceptU5OwnerResponse.isSuccess()) {
                    System.err.println("u5 failed to accept owner assignment: " + acceptU5OwnerResponse.getMessage());
                } else {
                    System.out.println("u5 (ID: " + u5Id + ") successfully accepted owner assignment in store s1.");
                }
            }

            // --- Logout all users for a clean state ---
            try {
                systemService.userLogout(u2Id);
                System.out.println("Logged out u2 (ID: " + u2Id + ")");
            } catch (Exception e) {
                System.err.println("Error logging out u2 (ID: " + u2Id + "): " + e.getMessage());
            }

            try {
                systemService.userLogout(devUserId);
                System.out.println("Logged out dev user (ID: " + devUserId + ")");
            } catch (Exception e) {
                System.err.println("Error logging out dev user (ID: " + devUserId + "): " + e.getMessage());
            }

            loggedInUsers.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("u2")) // u2 already attempted above
                .forEach(entry -> {
                    try {
                        systemService.userLogout(entry.getValue().getUserId());
                        System.out.println("Logged out user with ID: " + entry.getValue().getUserEmail());
                    } catch (Exception e) {
                        System.err.println("Error logging out user " + entry.getValue().getUserEmail() + ": " + e.getMessage());
                    }
                });

            System.out.println("Initial state loaded successfully.");

        } catch (Exception e) {
            System.err.println("Error initializing initial state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UserDTO registerAndLoginUserThroughSystemService(String alias, String email, String password, String dob, String country) throws Exception {
        Response<String> registerResponse = systemService.guestRegister(email, password, dob, country);
        if (!registerResponse.isSuccess()) {
            System.out.println("User " + alias + " (" + email + ") registration failed with message: " + registerResponse.getMessage() + ". Attempting to login assuming user might already exist.");
        } else {
            System.out.println("Registered user: " + alias + " (" + email + ")");
        }

        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        if (!loginResponse.isSuccess() || loginResponse.getData() == null || loginResponse.getData().getKey() == null || loginResponse.getData().getKey().getUserId() == 0) {
            throw new RuntimeException("Failed to login user: " + alias + " (" + email + ")" + (loginResponse.getMessage() != null ? ": " + loginResponse.getMessage() : ""));
        }
        UserDTO userDTO = loginResponse.getData().getKey();
        System.out.println("Logged in user: " + alias + " (" + email + ") with ID: " + userDTO.getUserId());
        return userDTO;
    }
}