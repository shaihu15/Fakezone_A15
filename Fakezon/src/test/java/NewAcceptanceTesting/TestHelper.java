package NewAcceptanceTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;


import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Response;
import ApplicationLayer.Services.SystemService;



public class TestHelper {
    private final SystemService systemService;

    public TestHelper(SystemService systemService) {
        this.systemService = systemService;
    }

    public String validEmail() {
        return "user1234@gmail.com";
    }

    public String validEmail2() {
        return "user234@gmail.com";
    }

    public String validEmail3() {
        return "user345@gmail.com";
    }

    // Added for register_and_login4
    public String validEmail4() {
        return "user456@gmail.com";
    }

    // Added for register_and_login5
    public String validEmail5() {
        return "user567@gmail.com";
    }

    public String validPassword() {
        return "StrongPass123";
    }

    public String validPassword2() {
        return "StrongPass1234";
    }

    public String validPassword3() {
        return "StrongPass12345";
    }

    // Added for register_and_login4
    public String validPassword4() {
        return "StrongPass123456";
    }

    // Added for register_and_login5
    public String validPassword5() {
        return "StrongPass1234567";
    }

    public String invalidEmail() {
        return "not-an-email";
    }

    public String invalidPassword() {
        return "123";
    }

    public String validBirthDate_Over18() {
        return LocalDate.now().minusYears(18).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    public String validBirthDate_under18() {
        return LocalDate.now().minusYears(17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String invalidBirthDate() {
        return "1-1-1111";
    }

    public String validCountry() {
        return "IL";
    }

    public String invalidCountry() {
        return "InvalidCountry";
    }

    public String validStoreName() {
        return "TestStore" + System.currentTimeMillis();
    }


    public Response<String> registerUser(String email, String password, String birthDate, String country) {
        Response<String> result = systemService.guestRegister(email, password, birthDate, country);
        if (result.isSuccess()) {
            System.out.println("User registered successfully: " + result.getMessage());
        } else {
            System.out.println("User registration failed: " + result.getMessage());
        }
        return result;
    }

    public Response<String> success_registerUser(){
        String validEmail = validEmail();
        String validPassword = validPassword();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
            return result;
        }

    public Response<UserDTO> register_and_login() {
        String email = validEmail();
        String password = validPassword();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();

        // Register the guest
        Response<String> registerResponse = systemService.guestRegister(email, password, validBirthDay, validCountry);
        if (registerResponse == null) {
            return new Response<>(null, "Registration failed: response was null", false, ErrorType.INTERNAL_ERROR, null);
        }
        if (!registerResponse.isSuccess()) {
            return new Response<>(null, "Registration failed: " + registerResponse.getMessage(), false, registerResponse.getErrorType(), null);
        }

        // Login the guest
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        if (loginResponse == null) {
            return new Response<>(null, "Login failed: response was null", false, ErrorType.INTERNAL_ERROR, null);
        }
        if (!loginResponse.isSuccess()) {
            return new Response<>(null, "Login failed: " + loginResponse.getMessage(), false, loginResponse.getErrorType(), null);
        }

        // Success
        UserDTO user = loginResponse.getData().getKey();
        return new Response<>(user, "Register and login successful", true, null, null);
    }


    public Response<UserDTO> login1(){
        String email = validEmail();
        String password = validPassword();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }
    // Added register_and_login2
    public Response<UserDTO> register_and_login2(){
        String email = validEmail2();
        String password = validPassword2();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(email, password, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

    public Response<UserDTO> login2(){
        String email = validEmail2();
        String password = validPassword2();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

    public Response<UserDTO> register_and_login3(){
        String email = validEmail3();
        String password = validPassword3();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(email, password, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

    // Added register_and_login4
    public Response<UserDTO> register_and_login4(){
        String email = validEmail4();
        String password = validPassword4();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(email, password, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

    // Added register_and_login5
    public Response<UserDTO> register_and_login5(){
        String email = validEmail5();
        String password = validPassword5();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(email, password, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(email, password);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, ErrorType.INVALID_INPUT, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

    public Response<Integer> openStore(){
        Response<UserDTO> loginResult = register_and_login();
        if(loginResult == null){
            return null;
        }
        int userId = loginResult.getData().getUserId();
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        if(!resultAddStore.isSuccess()){
            return null;
        }
        return resultAddStore;
    }

    public Response<StoreProductDTO> addProductToStore(int storeId, int userId) {
        String productName = "Test Product";
        String productDescription = "Test Description";
        String category = PCategory.ELECTRONICS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse.getData());

        if (storePResponse.isSuccess()) {
            return storePResponse;
        } else {
            return null;
        }
    }

    public Response<StoreProductDTO> addProductToStore2(int storeId, int userId) {
        String productName = "Test Product2";
        String productDescription = "Test Description2";
        String category = PCategory.BOOKS.toString();
        Response<StoreProductDTO> storePResponse = systemService.addProductToStore(storeId, userId, productName, productDescription, 1, 1, category);
        assertNotNull(storePResponse.getData());

        if (storePResponse.isSuccess()) {
            return storePResponse;
        } else {
            return null;
        }
    }

    public Response<Integer> openStore(int userId){
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store");
        if(!resultAddStore.isSuccess()){
            return null;
        }
        return resultAddStore;
    }

      public Response<Integer> openStore2(int userId){
        Response<Integer> resultAddStore = systemService.addStore(userId, "Test Store2");
        if(!resultAddStore.isSuccess()){
            return null;
        }
        return resultAddStore;
    }
}