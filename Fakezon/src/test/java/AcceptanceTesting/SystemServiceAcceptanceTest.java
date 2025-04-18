package AcceptanceTesting;

import DomainLayer.IRepository.IStoreRepository;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SystemServiceAcceptanceTest {
    private SystemService systemService;
    private IStoreRepository storeRepository;
    private Store store1;
    private int store1Id = 1;
    private int founder1Id = 10;
    private Store store2;
    private int store2Id = 2;
    private int founder2Id = 20;


    @BeforeEach
    void setUp() {
        storeRepository = mock(IStoreRepository.class);
        store1 = new Store("Test Store 1", store1Id, founder1Id);
        store2 = new Store("Test Store 2", store2Id, founder2Id);
    }

    
}
