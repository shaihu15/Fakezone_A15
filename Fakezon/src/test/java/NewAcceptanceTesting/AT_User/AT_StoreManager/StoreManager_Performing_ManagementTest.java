package NewAcceptanceTesting.AT_User.AT_StoreManager;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fakezone.fakezone.FakezoneApplication;

import ApplicationLayer.Response;
import ApplicationLayer.DTO.OrderDTO;
import ApplicationLayer.DTO.StoreProductDTO;
import ApplicationLayer.DTO.StoreRolesDTO;
import ApplicationLayer.DTO.UserDTO;

import DomainLayer.Enums.PaymentMethod;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.helpers.StoreMsg;
import NewAcceptanceTesting.TestHelper;
import ApplicationLayer.Services.SystemService;


@SpringBootTest(classes = FakezoneApplication.class)
public class StoreManager_Performing_ManagementTest {

    @Autowired
    private SystemService systemService;
    private TestHelper testHelper;

    private int storeId;
    private int ownerUserId;
    private int managerUserId;
    private int otherRegisteredUserId;
    private int otherOwnerUserId;
    private String productName;
    private String productDescription;
    private String category;
    List<StoreManagerPermission> perms;


    @BeforeEach
    void setUp() {
        systemService.clearAllData();

        testHelper = new TestHelper(systemService);
                Response<UserDTO> ownerUserRes = testHelper.register_and_login();
        assertTrue(ownerUserRes.isSuccess(), "Failed to register and login owner");
        ownerUserId = ownerUserRes.getData().getUserId();

        Response<Integer> storeRes = systemService.addStore(ownerUserId, "ManagerAppointmentTestStore");
        assertTrue(storeRes.isSuccess(), "Failed to add store");
        storeId = storeRes.getData();

        Response<UserDTO> managerUserRes = testHelper.register_and_login2();
        assertTrue(managerUserRes.isSuccess(), "Failed to register and login manager to be appointed");
        managerUserId = managerUserRes.getData().getUserId();

        perms = new ArrayList<>();
        perms.add(StoreManagerPermission.INVENTORY);
        perms.add(StoreManagerPermission.PURCHASE_POLICY);
        perms.add(StoreManagerPermission.DISCOUNT_POLICY);
        perms.add(StoreManagerPermission.REQUESTS_REPLY);
        perms.add(StoreManagerPermission.VIEW_ROLES);
        perms.add(StoreManagerPermission.VIEW_PURCHASES);

        Response<Void> response = systemService.addStoreManager(storeId, ownerUserId, managerUserId, perms);
        assertTrue(response.isSuccess());
        assertEquals("Store manager added successfully", response.getMessage());

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Thread was interrupted during sleep");
        }
        // Verify assignment message is sent
        Response<Map<Integer, StoreMsg>> assignmentMessagesRes = systemService.getAssignmentMessages(managerUserId);
        assertTrue(assignmentMessagesRes.isSuccess(), "Expected to retrieve assignment messages for manager");

          // Manager accepts the assignment
        Response<String> acceptRes = systemService.acceptAssignment(storeId, managerUserId);
        assertTrue(acceptRes.isSuccess(), "Expected manager to successfully accept assignment");

    }

    @AfterEach
    void tearDown() {
        // Remove the manager from the store
        Response<Void> removeManagerRes = systemService.removeStoreManager(storeId, ownerUserId, managerUserId);
        assertTrue(removeManagerRes.isSuccess(), "Failed to remove store manager");

        // Close the store
        Response<String> closeStoreRes = systemService.closeStoreByFounder(storeId, ownerUserId);
        assertTrue(closeStoreRes.isSuccess(), "Failed to close store");

        // Delete the users
        Response<Boolean> deleteOwnerRes = systemService.deleteUser(testHelper.validEmail());
        assertTrue(deleteOwnerRes.isSuccess(), "Failed to delete owner user");

        Response<Boolean> deleteManagerRes = systemService.deleteUser(testHelper.validEmail2());
        assertTrue(deleteManagerRes.isSuccess(), "Failed to delete manager user");
    }


    @Test
    void testOwnerCanViewRoles() {
        Response<StoreRolesDTO> res = systemService.getStoreRoles(storeId, managerUserId);
        assertTrue(res.isSuccess());
    }

    @Test
    void testManagerCangetAllStoreOrders() {
        Response<StoreProductDTO> storePResponse = testHelper.addProductToStore(storeId, ownerUserId); //only one product is added
        assertNotNull(storePResponse.getData());
        int productIdInt = storePResponse.getData().getProductId();
        //the product is added to the store

        Response<UserDTO> resultRegister2 = testHelper.register_and_login3();
        assertTrue(resultRegister2.isSuccess(), "Failed to register and login another user");
        int registeredId = resultRegister2.getData().getUserId();
        // resaigter1 is registered and logged in

        Response<Void> responseAddToBasket = systemService.addToBasket(registeredId, productIdInt, storeId, 1); 
        assertTrue(responseAddToBasket.isSuccess());

        Response<String> responsePurchaseCart = systemService.purchaseCart
                    (registeredId, testHelper.validCountry(), LocalDate.now(), PaymentMethod.CREDIT_CARD,
                    "deliveryMethod","1234567890123456","cardHolder", 
                    "12/25", "123", "123 Main St* City* Country* 0000","Recipient",
                     "Package details");

        assertTrue(responsePurchaseCart.isSuccess());
        assertEquals("Cart purchased successfully", responsePurchaseCart.getMessage());

        Response<List<OrderDTO>> purchaseHistoryRes = systemService.getAllStoreOrders(storeId, managerUserId);
        assertTrue(purchaseHistoryRes.isSuccess());
    }



}
