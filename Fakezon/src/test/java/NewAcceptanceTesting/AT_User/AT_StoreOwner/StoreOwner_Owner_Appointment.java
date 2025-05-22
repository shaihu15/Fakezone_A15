package NewAcceptanceTesting.AT_User.AT_StoreOwner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;
import ApplicationLayer.Enums.ErrorType;

import ApplicationLayer.Services.SystemService;
import DomainLayer.Enums.StoreManagerPermission;

import NewAcceptanceTesting.TestHelper;

@SpringBootTest(classes = FakezoneApplication.class)
public class StoreOwner_Owner_Appointment {

    @Autowired
    private SystemService systemService;

    private TestHelper testHelper;

    private int OwnerUserId;
    private int storeId;
    private int ManagerUserId;

    @BeforeEach
    void setUp() {
        systemService.clearAllData();
        
        testHelper = new TestHelper(systemService);

        Response<UserDTO> OwnerUser = testHelper.register_and_login();
        assertTrue(OwnerUser.isSuccess());
        OwnerUserId = OwnerUser.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(OwnerUserId, "TestStore");
        assertTrue(storeRes.isSuccess());
        storeId = storeRes.getData();

        Response<UserDTO> ManagerUser = testHelper.register_and_login2();
        assertTrue(ManagerUser.isSuccess());
        ManagerUserId = ManagerUser.getData().getUserId();

        List<StoreManagerPermission> perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        Response<Void> addManagerRes = systemService.addStoreManager(storeId, OwnerUserId, ManagerUserId, perms);
        assertTrue(addManagerRes.isSuccess());

        Response<HashMap<Integer, String>> AssignmentMessagesRes = systemService.getAssignmentMessages(ManagerUserId);
        assertTrue(AssignmentMessagesRes.isSuccess());
        assertTrue(AssignmentMessagesRes.getData().keySet().contains(storeId));

        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());
    }

    @Test
    void testAddStoreOwner_Success() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<HashMap<Integer, String>> AssignmentMessagesRes = systemService.getAssignmentMessages(ManagerUserId);
        assertTrue(AssignmentMessagesRes.isSuccess());
        assertTrue(AssignmentMessagesRes.getData().keySet().contains(storeId));
    }

    @Test
    void testAddStoreOwner_ManagerAccept_Success() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> acceptRes = systemService.acceptAssignment(storeId, ManagerUserId);
        assertTrue(acceptRes.isSuccess());

        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertTrue(storeRolesData.getStoreOwners().contains(ManagerUserId));
    }

    @Test
    void testAddStoreOwner_ManagerDecline_Failure() {
        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, ManagerUserId);
        assertTrue(response.isSuccess());
        assertEquals("Store owner added successfully", response.getMessage());

        Response<List<Integer>> pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertTrue(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<String> declineRes = systemService.declineAssignment(storeId, ManagerUserId);
        assertTrue(declineRes.isSuccess());

        pendingOwnerRes = systemService.getPendingOwners(storeId, OwnerUserId);
        assertTrue(pendingOwnerRes.isSuccess());
        assertFalse(pendingOwnerRes.getData().contains(ManagerUserId));

        Response<StoreRolesDTO> storeRolesRes = systemService.getStoreRoles(storeId, OwnerUserId);
        assertTrue(storeRolesRes.isSuccess());
        StoreRolesDTO storeRolesData = storeRolesRes.getData();
        assertFalse(storeRolesData.getStoreOwners().contains(ManagerUserId));
    }

    @Test
    void testAddStoreOwner_FailureInvalidUser() {
        int invalidUserId = -999;

        Response<Void> response = systemService.addStoreOwner(storeId, OwnerUserId, invalidUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store owner"));
    }

    @Test
    void testAddStoreOwner_FailureInvalidStore() {
        int invalidStoreId = -123;

        Response<Void> response = systemService.addStoreOwner(invalidStoreId, OwnerUserId, ManagerUserId);
        assertFalse(response.isSuccess());
        assertEquals(ErrorType.INTERNAL_ERROR, response.getErrorType());
        assertTrue(response.getMessage().contains("Error during adding store owner"));
    }
}