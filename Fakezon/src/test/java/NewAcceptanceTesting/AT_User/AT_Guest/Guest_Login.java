package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootTest(classes = FakezoneApplication.class)

public class Guest_Login {
    //Use-case: 1.4 Guest Login

    private static final Logger logger = LoggerFactory.getLogger(Guest_Login.class);

    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int guestId;
    String validEmail;
    String validPassword;
    String validBirthDay;
    String validCountry;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        try {
            logger.info("Starting test setup");
            testHelper = new TestHelper(systemService);

            validEmail = testHelper.validEmail();
            validPassword = testHelper.validPassword();
            validBirthDay = testHelper.validBirthDate_Over18();
            validCountry = testHelper.validCountry();

            logger.info("Creating unsigned user");
            // Guest enters the system
            Response<UserDTO> guestResponse = systemService.createUnsignedUser();
            assertTrue(guestResponse.isSuccess(), "Failed to create unsigned user: " + guestResponse.getMessage());
            guestId = guestResponse.getData().getUserId();

            logger.info("Registering guest user");
            // Register the guest user
            Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry);
            assertTrue(result.isSuccess(), "Registration failed: " + result.getMessage());
            logger.info("Test setup completed successfully");
        } catch (Exception e) {
            logger.error("Error during test setup", e);
            throw e;
        }
    }

    @Test
    void testLoginUser_validCredentials_Success() {
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword);
        assertTrue(loginResponse.isSuccess(), "Login failed: " + loginResponse.getMessage());
        assertEquals("Successful Login", loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_mismatchEmail_Failure() {
        String validEmail2 = testHelper.validEmail2();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail2, validPassword);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: User not found", loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_mismatchPassword_Failure() {
        String validPassword2 = testHelper.validPassword2();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword2);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: Incorrect password", loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_emptyEmail_Failure() {
        String emptyEmail = "";
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(emptyEmail, validPassword);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: User not found", loginResponse.getMessage());    
    }

    @Test
    void testLoginUser_emptyPassword_Failure() {
        String emptyPassword = "";
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, emptyPassword);
        assertFalse(loginResponse.isSuccess());    
        assertEquals("Login failed: Error during login: Incorrect password", loginResponse.getMessage());    
    }
}
