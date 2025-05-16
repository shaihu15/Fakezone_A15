package com.fakezone.fakezone;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
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

            // Step 5: Add store products and update product storeIds
            Map<String, String> productDescriptions = new HashMap<>();
            productDescriptions.put("Phone", "Smartphone with 6GB RAM");
            productDescriptions.put("Laptop", "Gaming laptop");
            productDescriptions.put("TV", "Science fiction novel");

            for (Map.Entry<String, Integer> entry : productIds.entrySet()) {
                String name = entry.getKey();
                int productId = entry.getValue();
                String originalDescription = productDescriptions.get(name);

                // Add product to store with the original description
                Response<StoreProductDTO> addProductResponse = systemService.addProductToStore(
                        storeId,
                        userId,
                        name,
                        originalDescription, // Use the original description
                        99.99,
                        10,
                        PCategory.ELECTRONICS.name()
                );
                if (!addProductResponse.isSuccess()) {
                    System.err.println("Failed to add product ID " + productId + " to store ID " + storeId + ": " + addProductResponse.getMessage());
                    continue; // Skip updating storeIds if adding to store fails
                }
                System.out.println("Added product ID " + productId + " to store ID " + storeId);

                // Update product's storeIds only if addProductToStore succeeds
                Set<Integer> storeSet = new HashSet<>();
                storeSet.add(storeId);
                Response<Boolean> updateResponse = systemService.updateProduct(
                        productId,
                        name,
                        "Description for " + name, // This description can differ, as updateProduct doesn't validate it
                        storeSet
                );
                if (!updateResponse.isSuccess()) {
                    System.err.println("Failed to update product ID " + productId + " with storeIds: " + updateResponse.getMessage());
                } else {
                    System.out.println("Updated product ID " + productId + " with storeIds: " + storeSet);
                }
            }

            System.out.println("Dev data initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing dev data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}