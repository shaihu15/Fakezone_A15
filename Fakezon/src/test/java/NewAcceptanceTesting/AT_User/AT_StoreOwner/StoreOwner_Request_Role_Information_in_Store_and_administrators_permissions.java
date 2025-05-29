package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Response;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.StoreManagerPermission;



@SpringBootTest(classes = FakezoneApplication.class)

public class StoreOwner_Request_Role_Information_in_Store_and_administrators_permissions {

    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    int storeId;
    int storeOwnerId;
    int newManagerID;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        testHelper = new TestHelper(systemService);

        // Initialize the system with a store owner and a product
        Response<UserDTO> StoreOwnerResult = testHelper.register_and_login();
        assertTrue(StoreOwnerResult.isSuccess());   
         
        storeOwnerId = StoreOwnerResult.getData().getUserId();
        // StoreOwner is registered and logged in

        Response<Integer> storeResult = systemService.addStore(storeOwnerId, "Store1");
        assertTrue(storeResult.isSuccess());
        storeId = storeResult.getData(); 
        // StoreOwner is the owner of Store1

        Response<UserDTO> newManagerResult = testHelper.register_and_login2();
        newManagerID = newManagerResult.getData().getUserId();
        int newManagerID = newManagerResult.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        perms.add(StoreManagerPermission.PURCHASE_POLICY);
        perms.add(StoreManagerPermission.DISCOUNT_POLICY);
        Response<Void>  addManagerResult = systemService.addStoreManager(storeId, storeOwnerId, newManagerID, perms);
        assertEquals("Store manager added successfully", addManagerResult.getMessage());
        assertTrue(addManagerResult.isSuccess());
    }

    @Test
    void testRequestRoleInformation_Success() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, storeOwnerId);
        assertEquals("Store roles retrieved successfully", result.getMessage());
        assertTrue(result.isSuccess());
        
        StoreRolesDTO storeRoles = result.getData();
        assertNotNull(storeRoles);
        assertEquals(storeId, storeRoles.getStoreId());

        assertTrue(storeRoles.getStoreOwners().contains(storeOwnerId));
        assertTrue(storeRoles.getStoreManagers().containsKey(newManagerID));

    }

    @Test
    void testRequestRoleInformation_InvalidStoreId_Failue() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(-1, storeOwnerId);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: Store not found", result.getMessage());
    }

    @Test
    void testRequestRoleInformation_InvalidUserId_Failure() {
        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, -1);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: User not found", result.getMessage());
    }

    @Test
    void testRequestRoleInformation_UserNotStoreOwner_Failure() {
        // Create a new user and try to get store roles
        Response<UserDTO> newUserResult = testHelper.register_and_login3();
        assertTrue(newUserResult.isSuccess());
        int newUserId = newUserResult.getData().getUserId();

        Response<StoreRolesDTO> result = systemService.getStoreRoles(storeId, newUserId);
        assertFalse(result.isSuccess());
        assertEquals("Error during getting store roles: User with id: " + newUserId + " has insufficient permissions for store ID: " + storeId, result.getMessage());
    }
 
    @Test
    void testRequestRoleInformation_Request_administrators_permissions_Success() {
        
        //no function for that yet
        
    }



}
