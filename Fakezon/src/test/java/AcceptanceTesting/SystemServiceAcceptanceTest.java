package AcceptanceTesting;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ApplicationLayer.Services.SystemService;
import DomainLayer.IRepository.IProductRepository;
import DomainLayer.IRepository.IStoreRepository;
import DomainLayer.IRepository.IUserRepository;
import DomainLayer.Model.Registered;
import DomainLayer.Model.Store;

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
        storeRepository = mock(IStoreRepository.class);
        userRepository = mock(IUserRepository.class);
        productRepository = mock(IProductRepository.class);
        systemService = new SystemService(storeRepository, userRepository, productRepository);

        store1 = new Store("Test Store 1", founder1Id);
        store2 = new Store("Test Store 2", founder2Id);

    }
    //closeStore_Founder_Success
    @Test
    void UserRegistration_Guest_Success() {
        // Arrange
        String email = "test@gmail.com";
        String password = "password123";
        String dobInput = "1990-01-01";
        LocalDate dob = LocalDate.parse(dobInput);
        Registered user = new Registered(email, password, dob);
        when(userRepository.findByUserName(email)).thenReturn(Optional.of(user)); // User exists
        
        // Act
        systemService.GuestRegister(email, password, dobInput);
        assertEquals(email, this.userRepository.findByUserName(email).get().getEmail());
    }




}
