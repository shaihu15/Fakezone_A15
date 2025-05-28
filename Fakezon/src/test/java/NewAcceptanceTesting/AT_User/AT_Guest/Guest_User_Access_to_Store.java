package NewAcceptanceTesting.AT_User.AT_Guest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Services.SystemService;

@SpringBootTest(classes = FakezoneApplication.class)

public class Guest_User_Access_to_Store {
    // Use-case: 2.1 Guest User Access to Store
     @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int storeId;
    int userId;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        testHelper = new TestHelper(systemService);

        Response<UserDTO> resultRegister = testHelper.register_and_login();
        assertTrue(resultRegister.isSuccess());
        userId = resultRegister.getData().getUserId();
        // StoreFounder is registered and logged in

        Response<Integer> resultAddStore = testHelper.openStore(userId);
        assertTrue(resultAddStore.isSuccess());
        // StoreFounder opened a store
        storeId = resultAddStore.getData();
    }
                    
    @Test
    void testGuestUserAccessStore_Succsses() {
        Response<StoreDTO> result = systemService.userAccessStore(storeId);
        assertTrue(result.isSuccess());
        assertEquals(storeId, result.getData().getStoreId());
    }

    @Test
    void testGuestUserAccessStore_StoreIsClose_Fail() {
        systemService.closeStoreByFounder(userId, storeId);
        //the store is closed
         
        Response<StoreDTO> accessStoreResponse = systemService.userAccessStore(storeId); 
        assertFalse(accessStoreResponse.isSuccess());
        assertEquals("Store is closed", accessStoreResponse.getMessage());
    }

    @Test
    void testGuestUserAccessStore_StoreNotExist_Fail() {
        Response<StoreDTO> accessStoreResponse = systemService.userAccessStore(9999); // Non-existing store ID
        assertFalse(accessStoreResponse.isSuccess());
        assertEquals("Error during user access store: Store not found", accessStoreResponse.getMessage());
    }
     
}
