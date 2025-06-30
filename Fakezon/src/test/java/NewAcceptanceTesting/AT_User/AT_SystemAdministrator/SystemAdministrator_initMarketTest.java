package NewAcceptanceTesting.AT_User.AT_SystemAdministrator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Interfaces.*;
import ApplicationLayer.Response;
import ApplicationLayer.Services.*;
import DomainLayer.IRepository.*;
import DomainLayer.Interfaces.*;
import InfrastructureLayer.Adapters.*;
import InfrastructureLayer.Repositories.*;
import NewAcceptanceTesting.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class SystemAdministrator_initMarketTest {

     @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    int storeId;
    int userId;
    String storeName;

    @BeforeEach
    void setUp() {
        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultUser = testHelper.register_and_login();
        userId = resultUser.getData().getUserId();
        storeName = "Test Store";

        Response<Integer> resultAddStore = systemService.addStore(userId, storeName);
        storeId = resultAddStore.getData();
    }

    @Test
    void testSuccessfulSystemInitialization() {
        // Setup
        Response<UserDTO> adminResponse = testHelper.register_and_login();
        assertNotNull(adminResponse);
        int adminId = adminResponse.getData().getUserId();
        systemService.addSystemAdmin(userId, adminId);

        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        assertTrue(systemService.isSystemAdmin(adminId).getData());

    }

    @Test
    void testNoAdminSystemInitialization() {


        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        Response<Integer> adminCount = systemService.getSystemAdminCount(userId);
        assertTrue(adminCount.getData() > 0);

    }

    @Test
    void testNoServicesSystemInitialization() {
        // Setup
        Response<UserDTO> adminResponse = testHelper.register_and_login();
        assertNotNull(adminResponse);
        int adminId = adminResponse.getData().getUserId();
        systemService.addSystemAdmin(userId, adminId);

        // Activate system
        boolean response = systemService.init();

        // Assertions
        assertTrue(response);
        assertTrue(systemService.isSystemAdmin(adminId).getData());

    }
}