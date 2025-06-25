package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;


@SpringBootTest(classes = FakezoneApplication.class)

public class Guest_Login {
    //Use-case: 1.4 Guest Login

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
        testHelper = new TestHelper(systemService);

        validEmail = testHelper.validEmail();
        validPassword = testHelper.validPassword();
        validBirthDay = testHelper.validBirthDate_Over18();
        validCountry = testHelper.validCountry();

        // Guest enters the system
        Response<UserDTO> guestResponse = systemService.createUnsignedUser();
        assertTrue(guestResponse.isSuccess());
        guestId = guestResponse.getData().getUserId();

        // Register the guest user
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        assertTrue(result.isSuccess(), "Registration should succeed");
}
    @AfterEach
    void tearDown() {
        Response<Boolean> deleteResponse = systemService.removeUnsignedUser(guestId);
        assertTrue(deleteResponse.isSuccess(), "User deletion should succeed");
        Response<Boolean> deleteUserResponse = systemService.deleteUser(validEmail);
        assertTrue(deleteUserResponse.isSuccess(), "User deletion should succeed");
    }

    @Test
    void testLoginUser_validCredentials_Success() {
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword);
        assertTrue(loginResponse.isSuccess());
        assertEquals("Successful Login",loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_mismatchEmail_Failure() {
        String validEmail2 = testHelper.validEmail2();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail2, validPassword);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: User not found",loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_mismatchPassword_Failure() {
        String validPassword2 = testHelper.validPassword2();
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, validPassword2);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: Incorrect password",loginResponse.getMessage());    
    }
    
    @Test
    void testLoginUser_emptyEmail_Failure() {
        String emptyEmail = "";
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(emptyEmail, validPassword);
        assertFalse(loginResponse.isSuccess());
        assertEquals("Login failed: Error during login: User not found",loginResponse.getMessage());    
    }

    @Test
    void testLoginUser_emptyPassword_Failure() {
        String emptyPassword = "";
        Response<AbstractMap.SimpleEntry<UserDTO, String>> loginResponse = systemService.login(validEmail, emptyPassword);
        assertFalse(loginResponse.isSuccess());    
        assertEquals("Login failed: Error during login: Incorrect password",loginResponse.getMessage());    
    }
    
}
