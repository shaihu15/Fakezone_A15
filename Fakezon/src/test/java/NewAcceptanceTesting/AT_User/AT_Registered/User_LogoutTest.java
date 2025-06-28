package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
import ApplicationLayer.Interfaces.IOrderService;
import ApplicationLayer.Interfaces.IProductService;
import ApplicationLayer.Interfaces.IStoreService;
import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.OrderService;
import ApplicationLayer.Services.ProductService;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import NewAcceptanceTesting.TestHelper;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;


import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class User_LogoutTest {
//Use-case: 3.1 User Logout

     @Autowired
    private SystemService systemService;


    private TestHelper testHelper;

    @BeforeEach
    void setUp() {
        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);
    
    }
 
    @Test
    void testUserLogout_Success() {
        Response<UserDTO> userResponse = testHelper.register_and_login();
        int userId = userResponse.getData().getUserId();

        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess());

       assertFalse(systemService.getUnsignedUserById(userId).isSuccess());
    }

    @Test
    void testUserLogout_Failure() {
        Response<UserDTO> userResponse = testHelper.register_and_login();
        int userId = userResponse.getData().getUserId();

        Response<Void> resultLogout=systemService.userLogout(userId);
        assertTrue(resultLogout.isSuccess());

        // Attempt to logout again
        Response<Void> resultLogoutAgain=systemService.userLogout(userId);
        assertFalse(resultLogoutAgain.isSuccess());
    }



}
