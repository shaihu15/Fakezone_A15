package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class Guest_User_Exiting_The_System {
     @Autowired
    private SystemService systemService;


    private TestHelper testHelper;

    int guestUserId;

    int storeOwnerId;
    int storeId;
    int productId;

    @BeforeEach
    void setUp() {

        systemService.clearAllData(); //should be removed when there's a DB and we exclude the tests!!!
        testHelper = new TestHelper(systemService);

        // Act: Create a guest (unsigned) user
        Response<UserDTO> response = systemService.createUnsignedUser();
        guestUserId = response.getData().getUserId();

        // Assert: The response should indicate success
        assertTrue(response.isSuccess(), "Guest user should be created successfully");
        assertEquals("Unsigned user created successfully", response.getMessage());
        assertNotNull(guestUserId, "Guest user ID should not be null");
        assertEquals(-1, guestUserId );
    }

    @Test
    void testGuestUserExitsSuccessfully() {
        // Act: Guest user exits the system
        Response<Boolean> response = systemService.removeUnsignedUser(guestUserId);

        // Assert: The response should indicate success
        assertTrue(response.isSuccess(), "Guest user should exit the system successfully");
        assertEquals("Unsigned user removed successfully", response.getMessage());
        assertTrue(response.getData(), "Guest user should be removed successfully");
    }

}
