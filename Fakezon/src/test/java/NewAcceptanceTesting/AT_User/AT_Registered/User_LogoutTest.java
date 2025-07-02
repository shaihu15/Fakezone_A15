package NewAcceptanceTesting.AT_User.AT_Registered;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Response;

import ApplicationLayer.Services.SystemService;

import NewAcceptanceTesting.TestHelper;


import com.fakezone.fakezone.FakezoneApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FakezoneApplication.class)
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
