package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import DomainLayer.Interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ApplicationLayer.Response;

import ApplicationLayer.DTO.UserDTO;

import ApplicationLayer.Services.SystemService;
import NewAcceptanceTesting.TestHelper;
import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
public class Guest_User_Enters_The_System {
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
        assertEquals(-1, guestUserId);

        Response<UserDTO> response2 = systemService.createUnsignedUser();
        int guestUserId2 = response2.getData().getUserId();
        assertEquals(-2, guestUserId2);
    }

}
