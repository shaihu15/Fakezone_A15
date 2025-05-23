package UnitTesting;

import ApplicationLayer.Response;
import ApplicationLayer.Enums.ErrorType;
import ApplicationLayer.Services.StoreService;
import ApplicationLayer.Services.SystemService;
import ApplicationLayer.Services.UserService;
import DomainLayer.Model.Cart;
import DomainLayer.Model.Product;
import DomainLayer.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class getCartFinalPriceTest {
    
    @Mock private UserService userService;
    @Mock private StoreService storeService;
    @InjectMocks private SystemService systemService;

    private final int USER_ID = 1010;
    private final LocalDate DOB = LocalDate.of(1990, 1, 1);

    private Cart nonEmptyCart;
    private Cart emptyCart;

    @BeforeEach
    public void setUp() {
        // create a cart with one dummy product
        nonEmptyCart = mock(Cart.class);
        Map<Integer,Map<Integer,Integer>> products = new HashMap<>();
        products.put(1010, new HashMap<Integer,Integer>());
        products.get(1010).put(1010,10);
        lenient().when(nonEmptyCart.getAllProducts()).thenReturn(products);

        // empty cart
        emptyCart = mock(Cart.class);
        Map<Integer, Map<Integer, Integer>> emptyProducts = new HashMap<>();
        lenient().when(emptyCart.getAllProducts()).thenReturn(emptyProducts);
    }

    @Test
    public void whenCartEmpty_returnsInvalidInputError() {
        when(userService.getUserCart(USER_ID)).thenReturn(emptyCart);

        Response<Double> resp = systemService.getCartFinalPrice(USER_ID, DOB);

        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertEquals("Cart is empty", resp.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, resp.getErrorType());
    }

    @Test
    public void whenUserNotFound_returnsInvalidInputError() {
        when(userService.getUserCart(USER_ID)).thenReturn(nonEmptyCart);
        when(userService.getAnyUserById(USER_ID)).thenReturn(Optional.empty());

        Response<Double> resp = systemService.getCartFinalPrice(USER_ID, DOB);

        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertEquals("User not found", resp.getMessage());
        assertEquals(ErrorType.INVALID_INPUT, resp.getErrorType());
    }

    @Test
    public void whenGetUserCartThrows_returnsInternalError() {
        when(userService.getUserCart(USER_ID))
            .thenThrow(new RuntimeException("DB down"));

        Response<Double> resp = systemService.getCartFinalPrice(USER_ID, DOB);

        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertTrue(resp.getMessage().contains("Error during final cart price"));
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
    }

    @Test
    public void whenCalcAmountThrows_returnsInternalError() {
        when(userService.getUserCart(USER_ID)).thenReturn(nonEmptyCart);
        when(userService.getAnyUserById(USER_ID)).thenReturn(Optional.of(new User()));
        when(storeService.calcAmount(USER_ID, nonEmptyCart, DOB))
            .thenThrow(new IllegalStateException("Calc failed"));

        Response<Double> resp = systemService.getCartFinalPrice(USER_ID, DOB);

        assertFalse(resp.isSuccess());
        assertNull(resp.getData());
        assertEquals("Calc failed", resp.getMessage());
        assertEquals(ErrorType.INTERNAL_ERROR, resp.getErrorType());
    }

    @Test
    public void whenEverythingOk_returnsSumOfAllStores() {
        when(userService.getUserCart(USER_ID)).thenReturn(nonEmptyCart);
        when(userService.getAnyUserById(USER_ID)).thenReturn(Optional.of(new User()));

        Map<Integer, Double> storeAmounts = new HashMap<>();
        storeAmounts.put(1, 10.0);
        storeAmounts.put(2, 15.5);
        when(storeService.calcAmount(USER_ID, nonEmptyCart, DOB)).thenReturn(storeAmounts);

        Response<Double> resp = systemService.getCartFinalPrice(USER_ID, DOB);

        assertTrue(resp.isSuccess());
        assertEquals(25.5, resp.getData());
        assertEquals("success", resp.getMessage());
        assertNull(resp.getErrorType());
    }
}
