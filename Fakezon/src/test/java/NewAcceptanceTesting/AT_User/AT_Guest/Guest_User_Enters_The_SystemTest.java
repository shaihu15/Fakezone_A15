package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreProductDTO;

import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.INotificationWebSocketHandler;
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
import InfrastructureLayer.Adapters.AuthenticatorAdapter;
import InfrastructureLayer.Adapters.DeliveryAdapter;
import InfrastructureLayer.Adapters.PaymentAdapter;
import InfrastructureLayer.Repositories.OrderRepository;
import InfrastructureLayer.Repositories.ProductRepository;
import InfrastructureLayer.Repositories.StoreRepository;
import InfrastructureLayer.Repositories.UserRepository;
import InfrastructureLayer.Adapters.NotificationWebSocketHandler;
import InfrastructureLayer.Security.TokenService;

import NewAcceptanceTesting.TestHelper;
import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
@ActiveProfiles("test")

public class Guest_User_Enters_The_SystemTest {
    // Use-Case: 1.1 Guest user enters the system

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int storeOwnerId;
    int storeId;
    int productId;

    @BeforeEach
    void setUp() {

        systemService.clearAllData(); // should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);
    }

    @Test
    void testGuestUserCreatedSuccessfully() {
        // Act: Create a guest (unsigned) user
        Response<UserDTO> response = systemService.createUnsignedUser();
        int guestUserId = response.getData().getUserId();

        // Assert: The response should indicate success
        assertTrue(response.isSuccess(), "Guest user should be created successfully");
        assertEquals("Unsigned user created successfully", response.getMessage());
        assertNotNull(guestUserId, "Guest user ID should not be null");
        assertEquals(1, guestUserId);

        Response<UserDTO> response2 = systemService.createUnsignedUser();
        int guestUserId2 = response2.getData().getUserId();
        assertEquals(2, guestUserId2);
    }

}
