package UnitTesting;

import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemServiceRemoveFromBasketTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SystemService systemService;

    private final int USER_ID = 100;
    private final int PRODUCT_ID = 200;
    private final int STORE_ID = 300;

    @Test
    public void whenUserNotLoggedIn_returnsInvalidInput() {
        // arrange
        when(userService.isUserLoggedIn(USER_ID)).thenReturn(false);

        // act
        Response<Void> resp = systemService.removeFromBasket(USER_ID, PRODUCT_ID, STORE_ID);

        // assert
        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertEquals("User is not logged in", resp.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, resp.getErrorType());
        verify(userService).isUserLoggedIn(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenIsUserLoggedInThrows_returnsInternalError() {
        // arrange
        when(userService.isUserLoggedIn(USER_ID))
                .thenThrow(new RuntimeException("Auth service down"));

        // act
        Response<Void> resp = systemService.removeFromBasket(USER_ID, PRODUCT_ID, STORE_ID);

        // assert
        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertTrue(resp.getMessage().contains("Error during removing from basket"));
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
        verify(userService).isUserLoggedIn(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenRemoveFromBasketThrows_returnsInternalError() {
        // arrange
        when(userService.isUserLoggedIn(USER_ID)).thenReturn(true);
        doThrow(new IllegalStateException("DB delete failed"))
                .when(userService).removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID);

        // act
        Response<Void> resp = systemService.removeFromBasket(USER_ID, PRODUCT_ID, STORE_ID);

        // assert
        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertTrue(resp.getMessage().contains("Error during removing from basket"));
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
        InOrder inOrder = inOrder(userService);
        inOrder.verify(userService).isUserLoggedIn(USER_ID);
        inOrder.verify(userService).removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenUserLoggedInAndRemoveSucceeds_returnsSuccess() {
        // arrange
        when(userService.isUserLoggedIn(USER_ID)).thenReturn(true);
        // removeFromBasket returns void and no exception => success

        // act
        Response<Void> resp = systemService.removeFromBasket(USER_ID, PRODUCT_ID, STORE_ID);

        // assert
        assertTrue(resp.isSuccess());
        assertNull(resp.getData());
        assertEquals("Product removed from basket successfully", resp.getMessage());
        assertNull(resp.getErrorType());
        InOrder inOrder = inOrder(userService);
        inOrder.verify(userService).isUserLoggedIn(USER_ID);
        inOrder.verify(userService).removeFromBasket(USER_ID, STORE_ID, PRODUCT_ID);
        verifyNoMoreInteractions(userService);
    }
}
