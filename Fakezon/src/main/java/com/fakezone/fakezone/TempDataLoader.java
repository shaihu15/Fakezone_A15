package com.fakezone.fakezone;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.ISystemService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.StoreService;
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
    private ISystemService systemService; // This will be the primary service for calls

    @Autowired
    private IUserService userService; // Still needed for specific user actions like accept assignments

    @Autowired
    private IStoreService storeService; // Still needed for store-specific actions like adding ratings if systemService doesn't wrap them directly

    // Dev user details (retained for initial setup)
    private final String devEmail = "dev@fakezone.bgu.ac.il";
    private final String devPassword = "Devpass1";
    private final String devDob = "2000-01-01";
    private final String devCountry = "IL";

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Original Dev User Setup (retained for initial system admin)
            // Step 1: Register dev user (u1)
            Response<String> devRegisterResponse = systemService.guestRegister(devEmail, devPassword, devDob, devCountry);
            if (!devRegisterResponse.isSuccess()) {
                System.out.println("Dev user registration failed: " + devRegisterResponse.getMessage() + ". Proceeding to login (assuming user might already exist).");
            } else {
                System.out.println("Dev user registered successfully.");
            }

            // Step 2: Log in dev user and get userId
            Response<AbstractMap.SimpleEntry<UserDTO, String>> devLoginResponse = systemService.login(devEmail, devPassword);
            if (!devLoginResponse.isSuccess() || devLoginResponse.getData() == null || devLoginResponse.getData().getKey() == null || devLoginResponse.getData().getKey().getUserId() == 0) {
                throw new RuntimeException("Failed to login dev user: " + (devLoginResponse.getMessage() != null ? devLoginResponse.getMessage() : "Unknown error"));
            }
            UserDTO devUserDTO = devLoginResponse.getData().getKey();
            int devUserId = devUserDTO.getUserId();
            System.out.println("Logged in dev user (u1) with ID: " + devUserId);

            // Step 3: Set dev user as system admin (u1)
            // IMPORTANT: For the very first system admin, the systemService.addSystemAdmin method
            // is expected to have special logic to allow the first user to become an admin,
            // even if no existing admin performs the action. This is a common bootstrapping pattern.
            Response<Void> addSystemAdminResponse = systemService.addSystemAdmin(devUserId, devUserId); // devUserId acts as both requester and target
            if (!addSystemAdminResponse.isSuccess()) {
                System.err.println("Failed to set dev user as system admin: " + addSystemAdminResponse.getMessage());
            } else {
                System.out.println("Dev user (u1) set as system admin.");
            }
            systemService.userLogout(devUserId); // Logout dev user after setting as admin for a clean state

            // --- New Initial State Setup ---
            // Registering users u2, u3, u4, u5, u6
            Map<String, UserDTO> loggedInUsers = new HashMap<>(); // Stores currently logged-in user DTOs
            Map<String, Integer> userIds = new HashMap<>(); // Stores user IDs

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

            // Connecting with u2 (already logged in from registerAndLoginUserThroughSystemService)
            int u2Id = userIds.get("u2");
            System.out.println("Current active user: u2 (ID: " + u2Id + ")");

            // Opening a store named s1 with u2
            String s1Name = "s1";
            Response<Integer> s1Response = systemService.addStore(u2Id, s1Name);
            if (!s1Response.isSuccess()) {
                throw new RuntimeException("Failed to create store " + s1Name + ": " + s1Response.getMessage());
            }
            int s1Id = s1Response.getData();
            System.out.println("Opened store '" + s1Name + "' with ID: " + s1Id + " by u2 (ID: " + u2Id + ")");

            // Adding a Bamba product to store s1 by u2 at a price of 30 and a quantity of 20.
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

            // Update global product with storeId (assuming addProductToStore might create a new global product if not exists)
            Set<Integer> bambaStoreSet = new HashSet<>();
            bambaStoreSet.add(s1Id);
            Response<Boolean> updateBambaGlobalProductResponse = systemService.updateProduct(
                    bambaProductId,
                    bambaName,
                    bambaDescription,
                    bambaStoreSet
            );
            if (!updateBambaGlobalProductResponse.isSuccess()) {
                System.err.println("Failed to update global product 'Bamba' with store ID " + s1Id + ": " + updateBambaGlobalProductResponse.getMessage());
            } else {
                System.out.println("Updated global product 'Bamba' with store ID: " + s1Id);
            }

            // Appointing u3 as an administrator in store s1 with permissions to edit product inventory (by u2)
            int u3Id = userIds.get("u3");
            Response<Void> requestAppointU3ManagerResponse = systemService.addStoreManager(s1Id, u2Id, u3Id, Collections.singletonList(StoreManagerPermission.INVENTORY)); // Assuming storeId is the first parameter
            if (!requestAppointU3ManagerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u3 as manager in store s1: " + requestAppointU3ManagerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u3 (ID: " + u3Id + ") as manager in store s1 (ID: " + s1Id + ") by u2.");
                // Simulate u3 accepting the appointment
                Response<String> acceptU3ManagerResponse = systemService.acceptAssignment(s1Id, u3Id);
                if (!acceptU3ManagerResponse.isSuccess()) {
                    System.err.println("u3 failed to accept manager assignment: " + acceptU3ManagerResponse.getMessage());
                } else {
                    System.out.println("u3 (ID: " + u3Id + ") successfully accepted manager assignment in store s1.");
                    // Assuming permissions are handled during acceptance or via another call
                    System.out.println("Assumed u3 has permissions to edit product inventory.");
                }
            }

            // Appointing u4 and u5 as owners of store s1 by u2
            int u4Id = userIds.get("u4");
            int u5Id = userIds.get("u5");

            // Appoint u4 as owner
            Response<Void> requestAppointU4OwnerResponse = systemService.addStoreOwner(s1Id, u2Id, u4Id); // Assuming storeId is the first parameter
            if (!requestAppointU4OwnerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u4 as owner in store s1: " + requestAppointU4OwnerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u4 (ID: " + u4Id + ") as owner in store s1 (ID: " + s1Id + ") by u2.");
                // Simulate u4 accepting the appointment
                Response<String> acceptU4OwnerResponse = systemService.acceptAssignment(s1Id, u4Id);
                if (!acceptU4OwnerResponse.isSuccess()) {
                    System.err.println("u4 failed to accept owner assignment: " + acceptU4OwnerResponse.getMessage());
                } else {
                    System.out.println("u4 (ID: " + u4Id + ") successfully accepted owner assignment in store s1.");
                }
            }

            // Appoint u5 as owner (Assuming u2 is still the one making the appointment)
            Response<Void> requestAppointU5OwnerResponse = systemService.addStoreOwner(s1Id, u2Id, u5Id);
            if (!requestAppointU5OwnerResponse.isSuccess()) {
                System.err.println("Failed to request appointment of u5 as owner in store s1: " + requestAppointU5OwnerResponse.getMessage());
            } else {
                System.out.println("Requested appointment of u5 (ID: " + u5Id + ") as owner in store s1 (ID: " + s1Id + ") by u2.");
                // Simulate u5 accepting the appointment
                Response<String> acceptU5OwnerResponse = systemService.acceptAssignment(s1Id, u5Id);
                if (!acceptU5OwnerResponse.isSuccess()) {
                    System.err.println("u5 failed to accept owner assignment: " + acceptU5OwnerResponse.getMessage());
                } else {
                    System.out.println("u5 (ID: " + u5Id + ") successfully accepted owner assignment in store s1.");
                }
            }

            // Logging out of u2
            systemService.userLogout(u2Id);
            System.out.println("Logged out u2 (ID: " + u2Id + ")");

            System.out.println("Initial state loaded successfully.");

            // Logout all other users who were logged in during registration for a clean state
            loggedInUsers.values().forEach(user -> {
                try {
                    if (user.getUserId() != u2Id) { // Only logout if they are still logged in and not u2 (already logged out)
                        systemService.userLogout(user.getUserId());
                        System.out.println("Logged out user with ID: " + user.getUserEmail());
                    }
                } catch (Exception e) {
                    System.err.println("Error logging out user " + user.getUserEmail() + ": " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error initializing initial state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to register and then log in a user, returning their UserDTO.
     * All calls are made through systemService.
     */
    private UserDTO registerAndLoginUserThroughSystemService(String alias, String email, String password, String dob, String country) throws Exception {
        // guestRegister expects dateOfBirth as a String
        Response<String> registerResponse = systemService.guestRegister(email, password, dob, country);
        if (!registerResponse.isSuccess()) {
            // If registration fails for any reason (including user already existing),
            // log the message and proceed to attempt login.
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