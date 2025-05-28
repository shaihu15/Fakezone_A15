package UnitTesting;

import ApplicationLayer.DTO.StoreRolesDTO;
import DomainLayer.Enums.StoreManagerPermission;
import DomainLayer.Model.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreRolesDTOTest {

    private StoreRolesDTO storeRolesDTO;
    private Collection<Integer> storeOwners;
    private HashMap<Integer, List<StoreManagerPermission>> storeManagers;

    @BeforeEach
    void setUp() {
        // Initialize test data
        storeOwners = new ArrayList<>();
        storeOwners.add(1);
        storeOwners.add(2);

        storeManagers = new HashMap<>();
        storeManagers.put(3, Arrays.asList(StoreManagerPermission.DISCOUNT_POLICY, StoreManagerPermission.VIEW_PURCHASES));
        storeManagers.put(4, Arrays.asList(StoreManagerPermission.VIEW_PURCHASES));

        storeRolesDTO = new StoreRolesDTO(1001, "Test Store", 2001, storeOwners, storeManagers);
    }

    @Test
    void constructor_ValidInput_ShouldCreateObject() {
        assertNotNull(storeRolesDTO, "StoreRolesDTO object should be created");
        assertEquals(1001, storeRolesDTO.getStoreId(), "Store ID should match");
        assertEquals("Test Store", storeRolesDTO.getStoreName(), "Store name should match");
        assertEquals(2001, storeRolesDTO.getFounderId(), "Founder ID should match");
        assertEquals(2, storeRolesDTO.getStoreOwners().size(), "Store should have 2 owners");
        assertEquals(2, storeRolesDTO.getStoreManagers().size(), "Store should have 2 managers");
    }

    @Test
    void getStoreOwners_ShouldReturnCorrectOwners() {
        Collection<Integer> retrievedOwners = storeRolesDTO.getStoreOwners();
        assertEquals(2, retrievedOwners.size(), "Store should have 2 owners");
        assertTrue(retrievedOwners.contains(1), "Owner ID 1 should be present");
        assertTrue(retrievedOwners.contains(2), "Owner ID 2 should be present");
    }

    @Test
    void getStoreManagers_ShouldReturnCorrectManagers() {
        HashMap<Integer, List<StoreManagerPermission>> retrievedManagers = storeRolesDTO.getStoreManagers();
        assertEquals(2, retrievedManagers.size(), "Store should have 2 managers");
        assertTrue(retrievedManagers.containsKey(3), "Manager ID 3 should be present");
        assertTrue(retrievedManagers.containsKey(4), "Manager ID 4 should be present");
        assertEquals(2, retrievedManagers.get(3).size(), "Manager 3 should have 2 permissions");
        assertEquals(1, retrievedManagers.get(4).size(), "Manager 4 should have 1 permission");
    }

    @Test
    void getStoreId_ShouldReturnCorrectId() {
        assertEquals(1001, storeRolesDTO.getStoreId(), "Store ID should match");
    }

    @Test
    void getStoreName_ShouldReturnCorrectName() {
        assertEquals("Test Store", storeRolesDTO.getStoreName(), "Store name should match");
    }

    @Test
    void getFounderId_ShouldReturnCorrectFounderId() {
        assertEquals(2001, storeRolesDTO.getFounderId(), "Founder ID should match");
    }

    @Test
    void testDefaultConstructor() {
        StoreRolesDTO dto = new StoreRolesDTO();
        assertEquals(0, dto.getStoreId());
        assertNull(dto.getStoreName());
        assertEquals(0, dto.getFounderId());
        assertNull(dto.getStoreOwners());
        assertNull(dto.getStoreManagers());
    }

    @Test
    void testStoreConstructor() {
        Store mockStore = mock(Store.class);
        when(mockStore.getId()).thenReturn(555);
        when(mockStore.getName()).thenReturn("Mock Store");
        when(mockStore.getStoreFounderID()).thenReturn(777);
        List<Integer> owners = Arrays.asList(10, 20);
        HashMap<Integer, List<StoreManagerPermission>> managers = new HashMap<>();
        managers.put(30, Arrays.asList(StoreManagerPermission.DISCOUNT_POLICY));
        when(mockStore.getStoreOwners(123)).thenReturn(owners);
        when(mockStore.getStoreManagers(123)).thenReturn(managers);

        StoreRolesDTO dto = new StoreRolesDTO(mockStore, 123);

        assertEquals(555, dto.getStoreId());
        assertEquals("Mock Store", dto.getStoreName());
        assertEquals(777, dto.getFounderId());
        assertEquals(owners, dto.getStoreOwners());
        assertEquals(managers, dto.getStoreManagers());
    }
}