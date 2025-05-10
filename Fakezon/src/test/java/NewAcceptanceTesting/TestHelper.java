package NewAcceptanceTesting;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.method.P;

import ApplicationLayer.DTO.ProductDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.PCategory;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Response;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Interfaces.IAuthenticator;
import DomainLayer.Interfaces.IDelivery;
import DomainLayer.Interfaces.IOrderRepository;
import DomainLayer.Interfaces.IPayment;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;


public class TestHelper {
    private final SystemService systemService;

    public TestHelper(SystemService systemService) {
        this.systemService = systemService;
    }

    public String validEmail() {
        return "user@gmail.com";
    }

    public String validEmail2() {
        return "user2@gmail.com";
    }

    public String validPassword() {
        return "StrongPass123";
    }

    public String validPassword2() {
        return "StrongPass1234";
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

    /*/
    public String validStoreDescription() {
        return "This is a test store description.";
    }

    public String validProductName() {
        return "TestProduct" + System.currentTimeMillis();
    }

    public String validProductDescription() {
        return "This is a test product description.";
    }

    */

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

    public Response<UserDTO> register_and_login(){
        String validEmail = validEmail();
        String validPassword = validPassword();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, null, null);
        if(!loginResult.isSuccess()){
            return null;
        }
        return loginResult;
    }

        public Response<UserDTO> register_and_login2(){
        String validEmail2 = validEmail2();
        String validPassword2 = validPassword2();
        String validBirthDay = validBirthDate_Over18();
        String validCountry = validCountry();
        Response<String> result = systemService.guestRegister(validEmail2, validPassword2, validBirthDay, validCountry );

        if(!result.isSuccess()){
            return null;
        }
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail2, validPassword2);
        Response<UserDTO> loginResult = loginResponse.isSuccess() 
            ? new Response<>(loginResponse.getData().getKey(), loginResponse.getMessage(), true, null, null)
            : new Response<>(null, loginResponse.getMessage(), false, null, null);
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




}
