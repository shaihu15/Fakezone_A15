package UnitTesting;

import DomainLayer.Model.User;
import InfrastructureLayer.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import ApplicationLayer.Interfaces.IUserService;
import ApplicationLayer.Services.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceRemoveFromBasketTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService; 

    private final int USER_ID = 123;
    private final int STORE_ID = 10;
    private final int PRODUCT_ID = 20;

    @Test
    public void whenUserNotFound_thenThrowsIllegalArgumentException() {
        // arrange
        when(userRepository.findAllById(USER_ID)).thenReturn(Optional.empty());

        // act & assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID));
        assertEquals("User not found", ex.getMessage());

        verify(userRepository).findAllById(USER_ID);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void whenUserPresent_thenCallsDomainRemoveFromBasket() {
        // arrange
        User mockUser = mock(User.class);
        when(userRepository.findAllById(USER_ID)).thenReturn(Optional.of(mockUser));

        // act
        userService.removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID);

        // assert
        InOrder inOrder = inOrder(userRepository, mockUser);
        inOrder.verify(userRepository).findAllById(USER_ID);
        inOrder.verify(mockUser).removeFromBasket(STORE_ID, PRODUCT_ID);
        verifyNoMoreInteractions(userRepository, mockUser);
    }

    @Test
    public void whenDomainRemoveThrows_thenPropagatesException() {
        // arrange
        User mockUser = mock(User.class);
        when(userRepository.findAllById(USER_ID)).thenReturn(Optional.of(mockUser));
        doThrow(new RuntimeException("DB failure"))
                .when(mockUser).removeFromBasket(STORE_ID, PRODUCT_ID);

        // act & assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID));
        assertEquals("DB failure", ex.getMessage());

        verify(userRepository).findAllById(USER_ID);
        verify(mockUser).removeFromBasket(STORE_ID, PRODUCT_ID);
        verifyNoMoreInteractions(userRepository, mockUser);
    }
}