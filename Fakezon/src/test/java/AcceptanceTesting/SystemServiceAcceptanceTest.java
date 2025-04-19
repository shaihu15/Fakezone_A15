package AcceptanceTesting;

import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import ApplicationLayer.Services.SystemService;
import DomainLayer.Model.Store;
import DomainLayer.Model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SystemServiceAcceptanceTest {
    private SystemService systemService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IProductRepository productRepository;
    private Store store1;
    private int store1Id = 1;
    private int founder1Id = 10;
    private Store store2;
    private int store2Id = 2;
    private int founder2Id = 20;


    @BeforeEach
    void setUp() {
        storeRepository = Mockito.mock(IStoreRepository.class);
        userRepository = Mockito.mock(IUserRepository.class);
        productRepository = Mockito.mock(IProductRepository.class);
        systemService = new SystemService(storeRepository, userRepository, productRepository);
    }

    
}
