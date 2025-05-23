package com.fakezone.fakezone;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.StoreService;
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
import java.util.Map;
import java.util.Set;

@Component
public class TempDataLoader implements ApplicationRunner {
    @Autowired
    private ISystemService systemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IStoreService storeService;

    private final String email = "dev@fakezone.bgu.ac.il";
    private final String password = "Devpass1";
    private final String dob = "2000-01-01";
    private final String country = "Israel";

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Step 1: Register dev user (ignore failure if already exists)
            LocalDate tempDate = systemService.parseDate(dob);
            userService.registerUser(email, password, tempDate, country);

            // Step 2: Log in dev user and get userId
            UserDTO userDTO = userService.login(email, password);
            int userId = userDTO.getUserId();
            System.out.println("Logged in user with ID: " + userId);

            // Step 3: Add products to global catalog
            Map<String, Integer> productIds = new HashMap<>();
            productIds.put("Phone", systemService.addProduct("Phone", "Smartphone with 6GB RAM", PCategory.ELECTRONICS.toString()).getData());
            productIds.put("Laptop", systemService.addProduct("Laptop", "Gaming laptop", PCategory.ELECTRONICS.toString()).getData());
            productIds.put("TV", systemService.addProduct("TV", "Science fiction novel", PCategory.ELECTRONICS.toString()).getData());
            System.out.println("Added products: " + productIds);

            // Step 4: Add store
            Response<Integer> storeResponse = systemService.addStore(userId, "Dev Store");
            if (!storeResponse.isSuccess()) {
                throw new RuntimeException("Failed to create store: " + storeResponse.getMessage());
            }
            int storeId = storeResponse.getData();
            System.out.println("Created store with ID: " + storeId);

            Response<Void> storeRatingResponse;
            try {
                storeService.addStoreRating(storeId, userId, 5, "Great store!");
                storeRatingResponse = new Response<>(null, "Store rated successfully", true, null, null);
            } catch (IllegalArgumentException e) {
                storeRatingResponse = new Response<>(null, "this store failed to be rated", false, ErrorType.INVALID_INPUT, null);
                System.err.println("Failed to rate store ID " + storeId + ": " + e.getMessage());
            } catch (Exception e) {
                storeRatingResponse = new Response<>(null, "this store failed to be rated", false, ErrorType.INTERNAL_ERROR, null);
                System.err.println("Failed to rate store ID " + storeId + ": " + e.getMessage());
            }
            // Step 5: Add store products and update product storeIds
            Map<String, String> productDescriptions = new HashMap<>();
            productDescriptions.put("Phone", "Smartphone with 6GB RAM");
            productDescriptions.put("Laptop", "Gaming laptop");
            productDescriptions.put("TV", "Science fiction novel");

            for (Map.Entry<String, Integer> entry : productIds.entrySet()) {
                String name = entry.getKey();
                int productId = entry.getValue();
                String originalDescription = productDescriptions.get(name);
                double price = 99.99 - (productId - 1) * 10.0; // 99.99, 89.99, 79.99

                // Add product to store with the calculated price
                Response<StoreProductDTO> addProductResponse = systemService.addProductToStore(
                        storeId,
                        userId,
                        name,
                        originalDescription,
                        price,
                        10,
                        PCategory.ELECTRONICS.name()
                );
                if (!addProductResponse.isSuccess()) {
                    System.err.println("Failed to add product ID " + productId + " to store ID " + storeId + ": " + addProductResponse.getMessage());
                    continue;
                }
                System.out.println("Added product ID " + productId + " to store ID " + storeId);
                double productRating = 5.0 - (productId - 1) * 1.0; // 5.0, 4.0, 3.0

                // Handle the void method with try-catch to create Response
                Response<Void> productRatingResponse;
                try {
                    storeService.addStoreProductRating(storeId, productId, userId, productRating, "nice product!");
                    productRatingResponse = new Response<>(null, "Product rated successfully", true, null, null);
                    System.out.println("Rated product ID " + productId + " in store ID " + storeId + " with rating " + productRating);
                } catch (IllegalArgumentException e) {
                    productRatingResponse = new Response<>(null, "this product failed to be rated", false, ErrorType.INVALID_INPUT, null);
                    System.err.println("Failed to rate product ID " + productId + " in store ID " + storeId + ": " + e.getMessage());
                } catch (Exception e) {
                    productRatingResponse = new Response<>(null, "this product failed to be rated", false, ErrorType.INTERNAL_ERROR, null);
                    System.err.println("Failed to rate product ID " + productId + " in store ID " + storeId + ": " + e.getMessage());
                }

                // Update product's storeIds
                Set<Integer> storeSet = new HashSet<>();
                storeSet.add(storeId);
                Response<Boolean> updateResponse = systemService.updateProduct(
                        productId,
                        name,
                        "Description for " + name,
                        storeSet
                );
                if (!updateResponse.isSuccess()) {
                    System.err.println("Failed to update product ID " + productId + " with storeIds: " + updateResponse.getMessage());
                } else {
                    System.out.println("Updated product ID " + productId + " with storeIds: " + storeSet);
                }
            }
            userService.logout(email);
            System.out.println("Dev data initialized successfully.");
            userService.logout("dev@fakezone.bgu.ac.il");
        } catch (Exception e) {
            System.err.println("Error initializing dev data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
