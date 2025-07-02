package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.*;

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

public class User_Registration {
    //Use-case: 1.3 User Registration
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
        systemService.clearAllData(); // Clear data before each test to ensure isolation
        testHelper = new TestHelper(systemService);

        validEmail = testHelper.validEmail();
        validPassword = testHelper.validPassword();
        validBirthDay = testHelper.validBirthDate_Over18();
        validCountry = testHelper.validCountry();

        // Guest enters the system
        Response<UserDTO> guestResponse = systemService.createUnsignedUser();
        assertTrue(guestResponse.isSuccess());
        guestId = guestResponse.getData().getUserId();
    }

    
    @Test
    void testGuestUserRegistersSuccessfully() {
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        assertTrue(result.isSuccess(), "Registration should succeed");

        assertEquals("Guest registered successfully", result.getMessage());
        Response<Boolean> deleteUserResponse = systemService.deleteUser(validEmail);
        assertTrue(deleteUserResponse.isSuccess(), "User deletion should succeed");
    }

    @Test
    void testRegisterUser_invalidEmail_Failure() {
        String invalidEmail = testHelper.invalidEmail();
        Response<String> result = systemService.guestRegister(invalidEmail, validPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Registration failed: Invalid email format", result.getMessage());
    }
 
    @Test
    void testRegisterUser_invalidPassword_Failure() {
        String invalidPassword = testHelper.invalidPassword();
        Response<String> result = systemService.guestRegister(validEmail, invalidPassword, validBirthDay, validCountry );
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid password", result.getMessage());
    }
   
    @Test
    void testRegisterUser_invalidBirthDate_Failure() {
        String invalidBirthDay = testHelper.invalidBirthDate();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, invalidBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid date of birth format. Expected format: YYYY-MM-DD", result.getMessage());
    }
    
    @Test
    void testRegisterUser_invalidCountry_Failure() {
        String invalidCountry = testHelper.invalidCountry();
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, invalidCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid country code", result.getMessage());
    }

    @Test
    void testRegisterUser_AlreadyRegistered_Failure() {
        systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        Response<String> user1 = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );
        Response<String> user2 = systemService.guestRegister(validEmail, validPassword, validBirthDay, validCountry );

        assertFalse(user2.isSuccess());
        assertEquals("Registration failed: User already exists", user2.getMessage());
    }

    @Test
    void testRegisterUser_EmptyEmail_Failure() {
        String emptyEmail = "";
        Response<String> result = systemService.guestRegister(emptyEmail, validPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Registration failed: Invalid email format", result.getMessage());
    }

    @Test
    void testRegisterUser_EmptyPassword_Failure() {
        String emptyPassword = "";

        Response<String> result = systemService.guestRegister(validEmail, emptyPassword, validBirthDay, validCountry );

        assertFalse(result.isSuccess());
        assertEquals("Invalid password", result.getMessage());
    }
    @Test

    void testRegisterUser_EmptyBirthDate_Failure() {
        String emptyBirthDay = "";
        Response<String> result = systemService.guestRegister(validEmail, validPassword, emptyBirthDay, validCountry );
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid date of birth format. Expected format: YYYY-MM-DD", result.getMessage());
    }
    
    @Test
    void testRegisterUser_EmptyCountry_Failure() {
        String emptyCountry = "";
        Response<String> result = systemService.guestRegister(validEmail, validPassword, validBirthDay, emptyCountry );
        
        assertFalse(result.isSuccess());
        assertEquals("Invalid country code", result.getMessage());
    }
    
}
